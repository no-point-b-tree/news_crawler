package com.archer.task.service;

import com.archer.task.ai.AiTaskComponent;
import com.archer.task.constant.NewsStatusEnum;
import com.archer.task.entity.Article;
import com.archer.task.entity.News;
import com.archer.task.mapper.NewsMapper;
import com.archer.task.service.impl.LatestSpiderTaskServiceImpl;
import com.archer.task.util.TikaUtil;
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

    private final Long taskId;

    private final AiTaskComponent aiTaskComponent;


    public SingleRssTaskService(String rssUrl, Date startDate, Date endDate, Long taskId, LatestSpiderTaskServiceImpl latestSpiderTaskService, NewsMapper newsMapper, CountDownLatch countDownLatch,AiTaskComponent aiTaskComponent) {
        this.rssUrl = rssUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.taskId = taskId;
        this.latestSpiderTaskService = latestSpiderTaskService;
        this.newsMapper = newsMapper;
        this.countDownLatch = countDownLatch;
        this.aiTaskComponent = aiTaskComponent;
    }

    @Override
    public void run() {
        // 一个网站回来的所有文章 插入
        try {
            List<Article> rssResult = latestSpiderTaskService.getRssResult(Collections.singletonList(rssUrl), startDate, endDate);
            for (Article article : rssResult) {
                if (newsMapper.getCountByUrl(article.getUrl()) > 0) {
                    continue;
                }
                News news = new News();
                news.setTitle(article.getTitle());
                news.setContent(article.getContent());

                // 使用TikaUtil将HTML内容转换为紧凑型纯文本
                if (article.getContent() != null && !article.getContent().trim().isEmpty()) {
                    try {
                        String tikaText = TikaUtil.htmlToCompactText(article.getContent());
                        news.setTikaContent(tikaText);
                    } catch (Exception e) {
                        log.warn("Failed to convert HTML to compact text using Tika for RSS URL: {}", article.getUrl(), e);
                        // 如果Tika处理失败，使用简化版本
                        try {
                            String fallbackText = TikaUtil.htmlToText(article.getContent());
                            news.setTikaContent(TikaUtil.toCompactText(fallbackText));
                        } catch (Exception e2) {
                            log.warn("Fallback Tika conversion also failed, using basic regex cleanup for RSS URL: {}", article.getUrl(), e2);
                            // 最后的回退：基本正则清理
                            news.setTikaContent(TikaUtil.toCompactText(
                                    article.getContent().replaceAll("<[^>]+>", " ")
                            ));
                        }
                    }
                }

                news.setUrl(article.getUrl());
                news.setIsDelete("N");
                news.setSource("rss");
                news.setPublishTime(article.getPublishDate());
                news.setCreateTime(new Date());
                news.setUpdateTime(new Date());
                news.setSourceUrl(rssUrl);
                news.setTaskId(taskId);
                news.setStatus(NewsStatusEnum.SPIDER_SUCCESS.name());
                newsMapper.insert(news);
                aiTaskComponent.startAiTask(news);
            }
        } catch (Exception e) {
            log.error("rss异常:", e);
        } finally {
            countDownLatch.countDown();
            log.info("剩余任务数量:{}",countDownLatch.getCount());
        }
    }
}
