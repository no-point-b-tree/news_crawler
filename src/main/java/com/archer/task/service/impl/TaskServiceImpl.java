package com.archer.task.service.impl;

import com.archer.task.entity.*;
import com.archer.task.mapper.*;
import com.archer.task.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

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

    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(4, 8, 60, TimeUnit.SECONDS, queue, new BasicThreadFactory.Builder().namingPattern("spider-%d").build());

    @Override
    public void startTask() {
        try {
            TaskLog taskLog = new TaskLog();
            taskLog.setTaskType("自动任务");
            Date current = new Date();
            Date startDate = DateUtils.addDays(current, -1);
            taskLog.setStartDate(startDate);
            taskLog.setEndDate(current);
            taskLogService.save(taskLog);
            QueryWrapper<RssLink> rssLinkQuery = new QueryWrapper<>();
            rssLinkQuery.eq("is_delete", "N");
            List<RssLink> rssLinks = rssLinkMapper.selectList(rssLinkQuery);
            List<String> rssLinkList = rssLinks.stream().map(RssLink::getUrl).toList();
            QueryWrapper<NewsLink> newsQuery = new QueryWrapper<>();
            newsQuery.eq("is_delete", "N");
            List<NewsLink> newsLinks = newsLinkMapper.selectList(newsQuery);
            CountDownLatch countDownLatch = new CountDownLatch(rssLinkList.size() + newsLinks.size());
            for (String rssLink : rssLinkList) {
                SingleRssTaskService task = new SingleRssTaskService(rssLink, startDate, current, taskLog.getId(), LatestSpiderTaskServiceImpl.getInstance(), newsMapper, countDownLatch);
                threadPool.execute(task);
            }
            for (NewsLink newsLink : newsLinks) {
                List<CssSelectorLinkRelation> cssSelectors = cssSelectorLinkRelationService.findByUrl(newsLink.getUrl());
                SingleNewsTaskService task = new SingleNewsTaskService(newsLink, LatestSpiderTaskServiceImpl.getInstance(), cssSelectors
                        , newsMapper, taskLog.getId(), countDownLatch, startDate, current);
                threadPool.execute(task);
            }
            countDownLatch.await();
        } catch (Exception e) {
            log.error("task error", e);
        }


    }

}
