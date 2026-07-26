package com.archer.task.service.impl;

import com.archer.task.ai.AiTaskComponent;
import com.archer.task.constant.TaskStatusEnum;
import com.archer.task.entity.*;
import com.archer.task.mapper.*;
import com.archer.task.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    @Resource
    private RssLinkMapper rssLinkMapper;

    @Resource
    private NewsLinkMapper newsLinkMapper;

    @Resource
    private MatchRuleMapper matchRuleMapper;

    @Resource
    private NewsMapper newsMapper;

    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    @Resource
    private TaskLogService taskLogService;

    @Resource
    private CssSelectorLinkRelationService cssSelectorLinkRelationService;

    @Resource
    private AiTaskComponent aiTaskComponent;

    @Resource
    private TaskUrlLogMapper taskUrlLogMapper;

    @Resource
    private WebAccessLogMapper webAccessLogMapper;

    @Value("${spring.profiles.active}")
    private String active;


    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(6, 8, 60, TimeUnit.SECONDS, queue, new BasicThreadFactory.Builder().namingPattern("spider-%d").build());

    @Override
    public void startTask() {
        try {
            TaskLog taskLog = new TaskLog();
            taskLog.setTaskType("自动任务");
            LocalDate now = LocalDate.now();
            Date current = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date startDate = DateUtils.addDays(current, -1);
            taskLog.setStartDate(startDate);
            taskLog.setEndDate(current);
            taskLog.setStatus(TaskStatusEnum.RUNNING.name());
            taskLog.setCreateTime(new Date());
            taskLog.setUpdateTime(new Date());
            taskLog.setIsDelete("N");
            taskLogService.save(taskLog);
            QueryWrapper<RssLink> rssLinkQuery = new QueryWrapper<>();
            rssLinkQuery.eq("is_delete", "N");
            List<RssLink> rssLinks = rssLinkMapper.selectList(rssLinkQuery);
            List<String> rssLinkList = rssLinks.stream().map(RssLink::getUrl).toList();
            QueryWrapper<NewsLink> newsQuery = new QueryWrapper<>();
            newsQuery.eq("is_delete", "N");
            List<NewsLink> newsLinks = newsLinkMapper.selectList(newsQuery);
            CountDownLatch countDownLatch = new CountDownLatch(rssLinkList.size() + newsLinks.size());
            CountDownLatch aiCountDown = new CountDownLatch(rssLinkList.size() + newsLinks.size());
            aiTaskComponent.setTaskCountDown(taskLog.getId(), aiCountDown);
            for (String rssLink : rssLinkList) {
                SingleRssTaskService task = new SingleRssTaskService(rssLink, startDate, current, taskLog.getId(), LatestSpiderTaskServiceImpl.getInstance(active), newsMapper, countDownLatch, aiTaskComponent);
                threadPool.execute(task);
            }
            for (NewsLink newsLink : newsLinks) {
                List<CssSelectorLinkRelation> cssSelectors = cssSelectorLinkRelationService.findByUrl(newsLink.getUrl());
                SingleNewsTaskService task = new SingleNewsTaskService(newsLink, LatestSpiderTaskServiceImpl.getInstance(active), cssSelectors, newsMapper, taskLog.getId(), countDownLatch, startDate, current, aiTaskComponent, taskUrlLogMapper, webAccessLogMapper);
                threadPool.execute(task);
            }
            countDownLatch.await();
            aiCountDown.await();
            UpdateWrapper<TaskLog> taskUpdate = new UpdateWrapper<>();
            taskUpdate.eq("id", taskLog.getId());
            taskUpdate.set("status", TaskStatusEnum.FINISHED.name());
            taskUpdate.set("update_time", new Date());
            taskLogService.update(taskUpdate);
        } catch (Exception e) {
            log.error("task error", e);
        }
    }

    @Override
    public void startTask(Date startDate, Date endDate) {
        startTask(startDate, endDate, null);
    }

    @Override
    public void startTask(Date startDate, Date endDate, String taskType) {
        try {
            TaskLog taskLog = new TaskLog();
            taskLog.setTaskType(taskType != null && !taskType.trim().isEmpty() ? taskType : "手动任务");
            taskLog.setStartDate(startDate);
            taskLog.setEndDate(endDate);
            taskLog.setStatus(TaskStatusEnum.RUNNING.name());
            taskLog.setCreateTime(new Date());
            taskLog.setUpdateTime(new Date());
            taskLog.setIsDelete("N");
            taskLogService.save(taskLog);

            QueryWrapper<RssLink> rssLinkQuery = new QueryWrapper<>();
            rssLinkQuery.eq("is_delete", "N");
            List<RssLink> rssLinks = rssLinkMapper.selectList(rssLinkQuery);
            List<String> rssLinkList = rssLinks.stream().map(RssLink::getUrl).toList();
            QueryWrapper<NewsLink> newsQuery = new QueryWrapper<>();
            newsQuery.eq("is_delete", "N");
            List<NewsLink> newsLinks = newsLinkMapper.selectList(newsQuery);

            // 根据taskType过滤任务
            List<String> filteredRssLinkList = rssLinkList;
            List<NewsLink> filteredNewsLinks = newsLinks;

            if (taskType != null && !taskType.trim().isEmpty()) {
                if ("RSS".equalsIgnoreCase(taskType)) {
                    filteredNewsLinks = new ArrayList<>(); // 只执行RSS任务
                } else if ("NEWS".equalsIgnoreCase(taskType)) {
                    filteredRssLinkList = new ArrayList<>(); // 只执行新闻任务
                }
            }

            CountDownLatch countDownLatch = new CountDownLatch(filteredRssLinkList.size() + filteredNewsLinks.size());
            CountDownLatch aiCountDown = new CountDownLatch(filteredRssLinkList.size() + filteredNewsLinks.size());
            aiTaskComponent.setTaskCountDown(taskLog.getId(), aiCountDown);
            for (String rssLink : filteredRssLinkList) {
                SingleRssTaskService task = new SingleRssTaskService(rssLink, startDate, endDate, taskLog.getId(), LatestSpiderTaskServiceImpl.getInstance(active), newsMapper, countDownLatch, aiTaskComponent);
                threadPool.execute(task);
            }

            for (NewsLink newsLink : filteredNewsLinks) {
                List<CssSelectorLinkRelation> cssSelectors = cssSelectorLinkRelationService.findByUrl(newsLink.getUrl());
                SingleNewsTaskService task = new SingleNewsTaskService(newsLink, LatestSpiderTaskServiceImpl.getInstance(active), cssSelectors, newsMapper, taskLog.getId(), countDownLatch, startDate, endDate, aiTaskComponent, taskUrlLogMapper, webAccessLogMapper);
                threadPool.execute(task);
            }

            countDownLatch.await();
            aiCountDown.await();

            taskLog.setStatus(TaskStatusEnum.FINISHED.name());
            taskLog.setUpdateTime(new Date());
            taskLogService.updateById(taskLog);

        } catch (Exception e) {
            log.error("手动任务执行错误", e);
        }
    }

}
