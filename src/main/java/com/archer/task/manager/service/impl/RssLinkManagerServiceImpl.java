package com.archer.task.manager.service.impl;

import com.archer.task.ai.AiTaskComponent;
import com.archer.task.constant.TaskStatusEnum;
import com.archer.task.entity.RssLink;
import com.archer.task.entity.TaskLog;
import com.archer.task.manager.service.RssLinkManagerService;
import com.archer.task.mapper.RssLinkMapper;
import com.archer.task.mapper.TaskLogMapper;
import com.archer.task.service.SingleRssTaskService;
import com.archer.task.service.impl.LatestSpiderTaskServiceImpl;
import com.archer.task.mapper.NewsMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class RssLinkManagerServiceImpl extends ServiceImpl<RssLinkMapper, RssLink> implements RssLinkManagerService {

    @Resource
    private RssLinkMapper rssLinkMapper;

    @Resource
    private NewsMapper newsMapper;

    @Resource
    private TaskLogMapper taskLogMapper;

    @Resource
    private AiTaskComponent aiTaskComponent;

    @Value("${spring.profiles.active}")
    private String active;

    @Override
    public Page<RssLink> pageRssLinks(String keyword, Integer page, Integer size) {
        Page<RssLink> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<RssLink> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(RssLink::getIsDelete, "N");

        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(RssLink::getUrl, keyword)
                    .or()
                    .like(RssLink::getDomain, keyword)
            );
        }

        queryWrapper.orderByDesc(RssLink::getCreateTime);

        return rssLinkMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRssLink(RssLink rssLink) {
        if (rssLink.getUrl() == null || rssLink.getUrl().trim().isEmpty()) {
            return false;
        }
        rssLink.setIsDelete("N");
        rssLink.setCreateTime(new Date());
        rssLink.setUpdateTime(new Date());
        return rssLinkMapper.insert(rssLink) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRssLink(RssLink rssLink) {
        if (rssLink.getId() == null) {
            return false;
        }
        rssLink.setUpdateTime(new Date());
        return rssLinkMapper.updateById(rssLink) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRssLink(Long id) {
        RssLink rssLink = rssLinkMapper.selectById(id);
        if (rssLink == null) {
            return false;
        }
        rssLink.setIsDelete("Y");
        rssLink.setUpdateTime(new Date());
        return rssLinkMapper.updateById(rssLink) > 0;
    }

    @Override
    public boolean startRssTask(String url, String startDateStr, String endDateStr) {
        try {
            // 创建任务日志记录
            TaskLog taskLog = new TaskLog();
            taskLog.setTaskType("RSS_MANUAL");
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
            CountDownLatch aiCl = new CountDownLatch(1);
            aiTaskComponent.setTaskCountDown(taskLog.getId(), aiCl);
            SingleRssTaskService task = new SingleRssTaskService(
                    url,
                    startDate,
                    endDate,
                    taskLog.getId(),
                    LatestSpiderTaskServiceImpl.getInstance(active),
                    newsMapper,
                    aiCl, aiTaskComponent
            );
            new Thread(task).start();
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
            log.error("启动RSS任务失败: {}", url, e);
            return false;
        }
    }
}