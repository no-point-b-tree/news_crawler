package com.archer.task.service;

import com.archer.task.constant.NewsStatusEnum;
import com.archer.task.entity.*;
import com.archer.task.mapper.NewsMapper;
import com.archer.task.service.impl.LatestSpiderTaskServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

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

    private final NewsMapper newsMapper;

    private final CountDownLatch countDownLatch;

    private final Long taskId;

    private final List<CssSelectorLinkRelation> cssSelectorLinkRelationList;

    private final Date startDate;

    private final Date endDate;

    public SingleNewsTaskService(NewsLink newsLink, LatestSpiderTaskServiceImpl latestSpiderTaskService, List<CssSelectorLinkRelation> cssSelectorLinkRelationList
            , NewsMapper newsMapper, Long taskId, CountDownLatch countDownLatch, Date startDate, Date endDate) {
        this.newsLink = newsLink;
        this.latestSpiderTaskService = latestSpiderTaskService;
        this.newsMapper = newsMapper;
        this.taskId = taskId;
        this.cssSelectorLinkRelationList = cssSelectorLinkRelationList;
        this.countDownLatch = countDownLatch;
        this.startDate = startDate;
        this.endDate = endDate;

    }

    @Override
    public void run() {
        try {
            Article newsPageContent = latestSpiderTaskService.getArticleContent(newsLink.getUrl());
            List<Hyperlink> hyperLinks = JsoupExtractor.extractor(newsPageContent.getContent(), cssSelectorLinkRelationList);
            for (Hyperlink hyperLink : hyperLinks) {
                if (isValidDate(startDate, endDate, hyperLink.getDate())) {
                    Article article = latestSpiderTaskService.getArticleContent(hyperLink.getHref());
                    article.setPublishDate(hyperLink.getDate());
                    News news = getNews(article);
                    newsMapper.insert(news);
                }
            }
        } catch (Exception e) {
            log.error("新闻任务异常", e);
        } finally {
            countDownLatch.countDown();
        }
    }

    public boolean isValidDate(Date startDate, Date endDate, Date publishDate) {
        if (endDate == null) {
            endDate = new Date();
        }
        if (startDate == null) {
            startDate = DateUtils.addDays(endDate, -1);
        }
        if (publishDate == null) {
            publishDate = new Date();
        }
        return publishDate.compareTo(startDate) <= 0 && publishDate.compareTo(endDate) <= 0;
    }

    private News getNews(Article article) {
        News news = new News();
        news.setTitle(article.getTitle());
        news.setContent(article.getContent());
        news.setUrl(article.getUrl());
        news.setIsDelete("N");
        news.setSource("news");
        news.setPublishTime(article.getPublishDate());
        news.setCreateTime(new Date());
        news.setUpdateTime(new Date());
        news.setSourceUrl(newsLink.getUrl());
        news.setTaskId(taskId);
        news.setStatus(NewsStatusEnum.SPIDER_SUCCESS.name());
        return news;
    }

}
