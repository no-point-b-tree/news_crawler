package com.archer.task.service;

import com.archer.task.entity.Article;
import com.archer.task.entity.Hyperlink;
import com.archer.task.entity.News;
import com.archer.task.entity.NewsLink;
import com.archer.task.mapper.NewsMapper;
import com.archer.task.service.impl.LatestSpiderTaskServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 单个新闻首页任务
 */
@Slf4j
public class SingleNewsTaskService implements Runnable {

    private final NewsLink newsLink;

    private final LatestSpiderTaskServiceImpl latestSpiderTaskService;

    private final List<String> ruleList;

    private final NewsMapper newsMapper;

    private final CountDownLatch countDownLatch;

    public SingleNewsTaskService(NewsLink newsLink, LatestSpiderTaskServiceImpl latestSpiderTaskService, List<String> ruleList, NewsMapper newsMapper, CountDownLatch countDownLatch) {
        this.newsLink = newsLink;
        this.latestSpiderTaskService = latestSpiderTaskService;
        this.ruleList = ruleList;
        this.newsMapper = newsMapper;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            Article newsPageContent = latestSpiderTaskService.getArticleContent(newsLink.getUrl());
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
        } catch (Exception e) {
            log.error("新闻任务异常", e);
        } finally {
            countDownLatch.countDown();
        }
    }

}
