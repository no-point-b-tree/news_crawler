package com.archer.task.manager.service.impl;

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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
    public boolean startSingleTask(String url) {
        try {
            TaskLog taskLog = new TaskLog();
            taskLog.setTaskType("SINGLE_MANUAL");
            taskLog.setIsDelete("N");
            taskLog.setCreateTime(new Date());
            taskLog.setUpdateTime(new Date());
            taskLogMapper.insert(taskLog);
            Long taskId = taskLog.getId();
            QueryWrapper<NewsLink> wrapper = new QueryWrapper<>();
            wrapper.eq("is_delete", "N");
            wrapper.eq("url", url);
            wrapper.last("limit 1");
            NewsLink newsLink = newsLinkMapper.selectOne(wrapper);
            Date current = new Date();
            Date startDate = DateUtils.addDays(current, -1);
            List<CssSelectorLinkRelation> cssSelectors = cssSelectorLinkRelationService.findByUrl(newsLink.getUrl());
            SingleNewsTaskService singleNewsTaskService = new SingleNewsTaskService(newsLink, new LatestSpiderTaskServiceImpl(), cssSelectors, newsMapper, taskId, new CountDownLatch(1),startDate,current);
            new Thread(singleNewsTaskService).start();
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