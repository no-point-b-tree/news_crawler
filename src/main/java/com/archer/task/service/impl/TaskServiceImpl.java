package com.archer.task.service.impl;

import com.archer.task.constant.RuleTypeEnum;
import com.archer.task.entity.*;
import com.archer.task.mapper.MatchRuleMapper;
import com.archer.task.mapper.NewsLinkMapper;
import com.archer.task.mapper.NewsMapper;
import com.archer.task.mapper.RssLinkMapper;
import com.archer.task.service.SingleNewsTaskService;
import com.archer.task.service.SingleRssTaskService;
import com.archer.task.service.TaskService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.time.DateFormatUtils;
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
    private LatestSpiderTaskServiceImpl latestSpiderTaskService;

    @Resource
    private MatchRuleMapper matchRuleMapper;

    @Resource
    private NewsMapper newsMapper;

    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(4, 8, 60, TimeUnit.SECONDS, queue, new BasicThreadFactory.Builder().namingPattern("spider-%d").build());

    @Override
    public void startTask() {
        try {
            QueryWrapper<RssLink> rssLinkQuery = new QueryWrapper<>();
            rssLinkQuery.eq("is_delete", "N");
            List<RssLink> rssLinks = rssLinkMapper.selectList(rssLinkQuery);
            List<String> rssLinkList = rssLinks.stream().map(RssLink::getUrl).toList();
            QueryWrapper<NewsLink> newsQuery = new QueryWrapper<>();
            newsQuery.eq("is_delete", "N");
            List<NewsLink> newsLinks = newsLinkMapper.selectList(newsQuery);
            CountDownLatch countDownLatch = new CountDownLatch(rssLinkList.size() + newsLinks.size());
            Date current = new Date();
            Date startDate = DateUtils.addDays(current, -1);
            List<String> ruleList = getRuleList(Arrays.asList(startDate, current));
            for (String rssLink : rssLinkList) {
                SingleRssTaskService task = new SingleRssTaskService(rssLink, startDate, current, latestSpiderTaskService, newsMapper, countDownLatch);
                threadPool.execute(task);
            }
            for (NewsLink newsLink : newsLinks) {
                SingleNewsTaskService task = new SingleNewsTaskService(newsLink, latestSpiderTaskService, ruleList, newsMapper, countDownLatch);
                threadPool.execute(task);
            }
            countDownLatch.await();
        } catch (Exception e) {
            log.error("task error", e);
        }


    }

    @Override
    public List<String> getRuleList(List<Date> dateList) {
        List<MatchRule> matchRules = matchRuleMapper.selectList(null);
        List<String> ruleList = new ArrayList<>();
        for (MatchRule matchRule : matchRules) {
            if (matchRule.getRuleType().equals(RuleTypeEnum.REGEX.name())) {
                ruleList.add(matchRule.getRule());
            } else if (matchRule.getRuleType().equals(RuleTypeEnum.DATE.name())) {
                for (Date date : dateList) {
                    ruleList.add(DateFormatUtils.format(date, matchRule.getRule(), Locale.ENGLISH));
                }
            }
        }
        return ruleList;
    }
}
