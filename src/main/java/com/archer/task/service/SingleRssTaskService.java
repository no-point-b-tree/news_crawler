package com.archer.task.service;

import com.archer.task.entity.Article;
import com.archer.task.entity.News;
import com.archer.task.mapper.NewsMapper;
import com.archer.task.service.impl.LatestSpiderTaskServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 单个RSS任务
 */
@Slf4j
public class SingleRssTaskService implements Runnable {

    private final String rssUrl;

    private final Date startDate;

    private final Date endDate;

    private final LatestSpiderTaskServiceImpl latestSpiderTaskService;

    private final NewsMapper newsMapper;

    private final CountDownLatch countDownLatch;

    public SingleRssTaskService(String rssUrl, Date startDate, Date endDate, LatestSpiderTaskServiceImpl latestSpiderTaskService, NewsMapper newsMapper, CountDownLatch countDownLatch) {
        this.rssUrl = rssUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.latestSpiderTaskService = latestSpiderTaskService;
        this.newsMapper = newsMapper;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        // 一个网站回来的所有文章 插入
        try {
            List<Article> rssResult = latestSpiderTaskService.getRssResult(Collections.singletonList(rssUrl), startDate, endDate);
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
        } catch (Exception e) {
            log.error("rss异常:", e);
        } finally {
            countDownLatch.countDown();
        }
    }
}
