package com.archer.task.service.impl;

import com.archer.task.constant.RuleTypeEnum;
import com.archer.task.entity.*;
import com.archer.task.mapper.MatchRuleMapper;
import com.archer.task.mapper.NewsLinkMapper;
import com.archer.task.mapper.NewsMapper;
import com.archer.task.mapper.RssLinkMapper;
import com.archer.task.service.JsoupExtractor;
import com.archer.task.service.TaskService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.*;

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

    @Override
    public void startTask() {
        try {
            QueryWrapper<RssLink> rssLinkQuery = new QueryWrapper<>();
            rssLinkQuery.eq("is_delete", "N");
            List<RssLink> rssLinks = rssLinkMapper.selectList(rssLinkQuery);
            List<String> rssLinkList = rssLinks.stream().map(RssLink::getUrl).toList();
            Date current = new Date();
            Date startDate = DateUtils.addDays(current, -1);
            List<Article> rssResult = latestSpiderTaskService.getRssResult(rssLinkList, startDate, current);
            for (Article article : rssResult) {
                News news = new News();
                news.setTitle(article.getTitle());
                news.setContent(article.getContent());
                news.setUrl(article.getUrl());
                news.setIsDelete("N");
                news.setSource("rss");
                news.setPublishTime(article.getPublishDate());
                news.setCreateTime(new Date());
                news.setUpdateTime(new Date());
                newsMapper.insert(news);
            }
            QueryWrapper<NewsLink> newsQuery = new QueryWrapper<>();
            newsQuery.eq("is_delete", "N");
            List<NewsLink> newsLinks = newsLinkMapper.selectList(newsQuery);
            for (NewsLink newsLink : newsLinks) {
                Article newsPageContent = latestSpiderTaskService.getArticleContent(newsLink.getUrl());
                List<String> ruleList = getRuleList(Arrays.asList(startDate, current));
                List<Hyperlink> hyperLinks = JsoupExtractor.extractor(newsPageContent.getContent(), ruleList, newsLink.getMinLevel());
                for (Hyperlink hyperLink : hyperLinks) {
                    Article article = latestSpiderTaskService.getArticleContent(hyperLink.getHref());
                    try {
                        Date date = DateUtils.parseDate(hyperLink.getPattern());
                        article.setPublishDate(date);
                    } catch (Exception e) {
                        article.setPublishDate(new Date());
                    }
                    News news = new News();
                    news.setTitle(article.getTitle());
                    news.setContent(article.getContent());
                    news.setUrl(article.getUrl());
                    news.setIsDelete("N");
                    news.setSource("news");
                    news.setPublishTime(article.getPublishDate());
                    news.setCreateTime(new Date());
                    news.setUpdateTime(new Date());
                    newsMapper.insert(news);
                }
            }
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
