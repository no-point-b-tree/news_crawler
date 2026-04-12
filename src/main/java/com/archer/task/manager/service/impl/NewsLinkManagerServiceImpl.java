package com.archer.task.manager.service.impl;

import com.archer.task.ai.AiTaskComponent;
import com.archer.task.constant.TaskStatusEnum;
import com.archer.task.entity.CssSelectorLinkRelation;
import com.archer.task.entity.NewsLink;
import com.archer.task.entity.TaskLog;
import com.archer.task.manager.service.NewsLinkManagerService;
import com.archer.task.mapper.CssSelectorLinkRelationMapper;
import com.archer.task.mapper.NewsLinkMapper;
import com.archer.task.mapper.NewsMapper;
import com.archer.task.mapper.TaskLogMapper;
import com.archer.task.service.CssSelectorLinkRelationService;
import com.archer.task.service.SingleNewsTaskService;
import com.archer.task.service.TaskService;
import com.archer.task.service.impl.LatestSpiderTaskServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NewsLinkManagerServiceImpl extends ServiceImpl<NewsLinkMapper, NewsLink> implements NewsLinkManagerService {

    @Resource
    private NewsLinkMapper newsLinkMapper;

    @Resource
    private TaskLogMapper taskLogMapper;

    @Resource
    private CssSelectorLinkRelationMapper cssSelectorLinkRelationMapper;

    @Resource
    private TaskService taskService;

    @Resource
    private CssSelectorLinkRelationService cssSelectorLinkRelationService;

    @Resource
    private NewsMapper newsMapper;

    @Resource
    private AiTaskComponent aiTaskComponent;

    @Override
    public Page<NewsLink> pageNewsLinks(String keyword, List<String> langs, Integer page, Integer size) {
        Page<NewsLink> pageParam = new Page<>(page == null ? 1 : page, size == null ? 10 : size);
        LambdaQueryWrapper<NewsLink> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NewsLink::getIsDelete, "N");

        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(NewsLink::getUrl, keyword)
                    .or()
                    .like(NewsLink::getName, keyword));
        }

        if (!CollectionUtils.isEmpty(langs)) {
            queryWrapper.in(NewsLink::getLang, langs);
        }

        queryWrapper.orderByDesc(NewsLink::getCreateTime);

        return newsLinkMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public Page<com.archer.task.manager.vo.NewsLinkWithCssCountVO> pageNewsLinksWithCssCount(String keyword, List<String> langs, Integer page, Integer size) {
        // 先获取分页的新闻链接
        Page<NewsLink> newsLinkPage = pageNewsLinks(keyword, langs, page, size);

        // 转换为包含CSS数量的VO
        Page<com.archer.task.manager.vo.NewsLinkWithCssCountVO> resultPage = new Page<>(
                newsLinkPage.getCurrent(),
                newsLinkPage.getSize(),
                newsLinkPage.getTotal()
        );
        resultPage.setRecords(newsLinkPage.getRecords().stream().map(newsLink -> {
            com.archer.task.manager.vo.NewsLinkWithCssCountVO vo = new com.archer.task.manager.vo.NewsLinkWithCssCountVO();

            // 复制NewsLink属性
            vo.setId(newsLink.getId());
            vo.setDomain(newsLink.getDomain());
            vo.setUrl(newsLink.getUrl());
            vo.setIsDelete(newsLink.getIsDelete());
            vo.setName(newsLink.getName());
            vo.setLang(newsLink.getLang());
            vo.setCreateTime(newsLink.getCreateTime());
            vo.setUpdateTime(newsLink.getUpdateTime());

            // 查询CSS选择器数量
            LambdaQueryWrapper<CssSelectorLinkRelation> cssQueryWrapper = new LambdaQueryWrapper<>();
            cssQueryWrapper.eq(CssSelectorLinkRelation::getUrl, newsLink.getUrl())
                    .eq(CssSelectorLinkRelation::getIsDelete, "N");

            List<CssSelectorLinkRelation> cssSelectors = cssSelectorLinkRelationMapper.selectList(cssQueryWrapper);

            // 计算总数
            vo.setCssSelectorCount(cssSelectors.size());

            // 计算启用和禁用的数量
            long enabledCount = cssSelectors.stream()
                    .filter(css -> "ON".equals(css.getEnabled()))
                    .count();
            long disabledCount = cssSelectors.stream()
                    .filter(css -> "OFF".equals(css.getEnabled()))
                    .count();

            vo.setEnabledCssSelectorCount((int) enabledCount);
            vo.setDisabledCssSelectorCount((int) disabledCount);

            return vo;
        }).toList());

        return resultPage;
    }

    @Override
    public List<String> getAllLanguages() {
        LambdaQueryWrapper<NewsLink> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(NewsLink::getLang)
                .eq(NewsLink::getIsDelete, "N")
                .isNotNull(NewsLink::getLang)
                .groupBy(NewsLink::getLang);
        List<NewsLink> newsLinks = newsLinkMapper.selectList(queryWrapper);
        return newsLinks.stream()
                .map(NewsLink::getLang)
                .distinct()
                .toList();
    }

    @Override
    public List<com.archer.task.manager.controller.LocaleController.LocaleInfo> getLocaleLanguages() {
        // 获取Java Locale支持的语言
        Locale[] availableLocales = Locale.getAvailableLocales();

        // 提取唯一的语言代码和显示名称
        Map<String, String> languageMap = new TreeMap<>();
        for (Locale locale : availableLocales) {
            String language = locale.getLanguage();
            String displayLanguage = locale.getDisplayLanguage(Locale.ENGLISH);

            if (!language.isEmpty() && !displayLanguage.isEmpty()) {
                languageMap.put(language, displayLanguage);
            }
        }

        // 转换为LocaleInfo列表
        return languageMap.entrySet().stream()
                .map(entry -> new com.archer.task.manager.controller.LocaleController.LocaleInfo(
                        entry.getKey(),
                        entry.getValue()))
                .sorted(Comparator.comparing(com.archer.task.manager.controller.LocaleController.LocaleInfo::getDisplayName))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createNewsLink(NewsLink newsLink) {
        if (newsLink.getUrl() == null || newsLink.getUrl().trim().isEmpty()) {
            return false;
        }
        newsLink.setIsDelete("N");
        newsLink.setCreateTime(new Date());
        newsLink.setUpdateTime(new Date());
        return newsLinkMapper.insert(newsLink) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNewsLink(NewsLink newsLink) {
        if (newsLink.getId() == null) {
            return false;
        }
        newsLink.setUpdateTime(new Date());
        return newsLinkMapper.updateById(newsLink) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNewsLink(Long id) {
        NewsLink newsLink = newsLinkMapper.selectById(id);
        if (newsLink == null) {
            return false;
        }
        newsLink.setIsDelete("Y");
        newsLink.setUpdateTime(new Date());
        return newsLinkMapper.updateById(newsLink) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startSingleTask(String url, String startDateStr, String endDateStr) {
        try {
            TaskLog taskLog = new TaskLog();
            taskLog.setTaskType("SINGLE_MANUAL");
            taskLog.setIsDelete("N");
            taskLog.setCreateTime(new Date());
            taskLog.setUpdateTime(new Date());
            taskLog.setStatus(TaskStatusEnum.RUNNING.name());
            // 解析时间范围
            Date startDate;
            Date endDate;
            if (startDateStr != null && !startDateStr.trim().isEmpty() &&
                    endDateStr != null && !endDateStr.trim().isEmpty()) {
                // 使用前端传递的时间范围
                try {
                    startDate = DateUtils.parseDate(startDateStr + " 00:00:00", "yyyy-MM-dd HH:mm:ss");
                    endDate = DateUtils.parseDate(endDateStr + " 23:59:59", "yyyy-MM-dd HH:mm:ss");
                } catch (Exception e) {
                    log.error("Failed to parse date range: startDate={}, endDate={}", startDateStr, endDateStr, e);
                    // 如果解析失败，使用默认值（前一天到今天）
                    Date current = new Date();
                    startDate = DateUtils.addDays(current, -1);
                    endDate = current;
                }
            } else {
                // 使用默认值（前一天到今天）
                Date current = new Date();
                startDate = DateUtils.addDays(current, -1);
                endDate = current;
            }
            taskLog.setStartDate(startDate);
            taskLog.setEndDate(endDate);
            taskLogMapper.insert(taskLog);
            Long taskId = taskLog.getId();
            QueryWrapper<NewsLink> wrapper = new QueryWrapper<>();
            wrapper.eq("is_delete", "N");
            wrapper.eq("url", url);
            wrapper.last("limit 1");
            NewsLink newsLink = newsLinkMapper.selectOne(wrapper);
            List<CssSelectorLinkRelation> cssSelectors = cssSelectorLinkRelationService.findByUrl(newsLink.getUrl());
            CountDownLatch aiCl = new CountDownLatch(1);
            aiTaskComponent.setTaskCountDown(taskLog.getId(), aiCl);
            SingleNewsTaskService singleNewsTaskService = new SingleNewsTaskService(newsLink, LatestSpiderTaskServiceImpl.getInstance(), cssSelectors, newsMapper, taskId, new CountDownLatch(1), startDate, endDate, aiTaskComponent);
            new Thread(singleNewsTaskService).start();
            new Thread(() -> {
                try {
                    aiCl.await();
                    UpdateWrapper<TaskLog> taskUpdate = new UpdateWrapper<>();
                    taskUpdate.eq("id", taskLog.getId());
                    taskUpdate.set("status", TaskStatusEnum.FINISHED.name());
                    taskLogMapper.update(taskUpdate);
                } catch (InterruptedException e) {
                    log.error("Failed to await task countdown");
                }
            }).start();
            return true;
        } catch (Exception e) {
            log.error("Failed to start single task for url: {}", url, e);
            return false;
        }
    }

    @Override
    public Page<TaskLog> pageTaskLogsByUrl(String url, Integer page, Integer size) {
        Page<TaskLog> pageParam = new Page<>(page == null ? 1 : page, size == null ? 10 : size);
        LambdaQueryWrapper<TaskLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskLog::getIsDelete, "N")
                .orderByDesc(TaskLog::getCreateTime);
        return taskLogMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public Page<CssSelectorLinkRelation> pageCssSelectorsByUrl(String url, Integer page, Integer size) {
        Page<CssSelectorLinkRelation> pageParam = new Page<>(page == null ? 1 : page, size == null ? 10 : size);
        LambdaQueryWrapper<CssSelectorLinkRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CssSelectorLinkRelation::getUrl, url)
                .eq(CssSelectorLinkRelation::getIsDelete, "N")
                .orderByDesc(CssSelectorLinkRelation::getCreateTime);
        return cssSelectorLinkRelationMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addCssSelector(String url, String cssSelectorGroup, String dateCssSelector, String hyperLinkCssSelector, String datePattern, String enabled) {
        CssSelectorLinkRelation relation = new CssSelectorLinkRelation();
        relation.setUrl(url);
        relation.setCssSelectorGroup(cssSelectorGroup);
        relation.setDateCssSelector(dateCssSelector);
        relation.setHyperLinkCssSelector(hyperLinkCssSelector);
        relation.setDatePattern(datePattern);
        relation.setEnabled(enabled != null ? enabled : "ON");
        relation.setIsDelete("N");
        relation.setCreateTime(new Date());
        relation.setUpdateTime(new Date());
        return cssSelectorLinkRelationMapper.insert(relation) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCssSelector(Long id, String cssSelectorGroup, String dateCssSelector, String hyperLinkCssSelector, String datePattern, String enabled) {
        CssSelectorLinkRelation relation = cssSelectorLinkRelationMapper.selectById(id);
        if (relation == null) {
            return false;
        }
        relation.setCssSelectorGroup(cssSelectorGroup);
        relation.setDateCssSelector(dateCssSelector);
        relation.setHyperLinkCssSelector(hyperLinkCssSelector);
        relation.setDatePattern(datePattern);
        relation.setEnabled(enabled != null ? enabled : "ON");
        relation.setUpdateTime(new Date());
        return cssSelectorLinkRelationMapper.updateById(relation) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCssSelector(Long id) {
        CssSelectorLinkRelation relation = cssSelectorLinkRelationMapper.selectById(id);
        if (relation == null) {
            return false;
        }
        relation.setIsDelete("Y");
        relation.setUpdateTime(new Date());
        return cssSelectorLinkRelationMapper.updateById(relation) > 0;
    }
}