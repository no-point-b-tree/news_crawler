package com.archer.task.service;

import com.archer.task.ai.AiTaskComponent;
import com.archer.task.constant.NewsStatusEnum;
import com.archer.task.entity.*;
import com.archer.task.mapper.NewsMapper;
import com.archer.task.mapper.TaskUrlLogMapper;
import com.archer.task.service.impl.LatestSpiderTaskServiceImpl;
import com.archer.task.util.TikaUtil;
import com.archer.task.util.UrlUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

    private final AiTaskComponent aiTaskComponent;

    private final TaskUrlLogMapper taskUrlLogMapper;

    public SingleNewsTaskService(NewsLink newsLink, LatestSpiderTaskServiceImpl latestSpiderTaskService, List<CssSelectorLinkRelation> cssSelectorLinkRelationList, NewsMapper newsMapper, Long taskId, CountDownLatch countDownLatch, Date startDate, Date endDate, AiTaskComponent aiTaskComponent, TaskUrlLogMapper taskUrlLogMapper) {
        this.newsLink = newsLink;
        this.latestSpiderTaskService = latestSpiderTaskService;
        this.newsMapper = newsMapper;
        this.taskId = taskId;
        this.cssSelectorLinkRelationList = cssSelectorLinkRelationList;
        this.countDownLatch = countDownLatch;
        this.startDate = startDate;
        this.endDate = endDate;
        this.aiTaskComponent = aiTaskComponent;
        this.taskUrlLogMapper = taskUrlLogMapper;
    }

    @Override
    public void run() {
        TaskUrlLog taskUrlLog = null;
        List<News> newsList = new ArrayList<>();
        try {
            // 创建URL处理日志记录
            taskUrlLog = createTaskUrlLog(newsLink.getUrl(), taskId);
            String lang = newsLink.getLang();
            Locale locale = new Locale(lang);
            if (cssSelectorLinkRelationList.isEmpty()) {
                return;
            }
            Article newsPageContent = latestSpiderTaskService.getArticleContent(newsLink.getUrl());
            List<Hyperlink> hyperLinks = JsoupExtractor.extractor(newsPageContent.getContent(), cssSelectorLinkRelationList, locale);

            for (Hyperlink hyperLink : hyperLinks) {
                if (JsoupExtractor.isValidDate(startDate, endDate, hyperLink.getDate())) {
                    if (hyperLink.getHref().startsWith("/")) {
                        // 相对路径转绝对路径
                        hyperLink.setHref(UrlUtils.extractBaseUrl(newsLink.getUrl()) + hyperLink.getHref());
                    }
                    if (newsMapper.getCountByUrl(hyperLink.getHref()) > 0) {
                        // 如果URL已经有新闻
                        continue;
                    }
                    Article article = latestSpiderTaskService.getArticleContent(hyperLink.getHref());
                    article.setPublishDate(hyperLink.getDate());
                    News news = getNews(article);
                    newsMapper.insert(news);
                    newsList.add(news);
                }
            }
            // 更新URL处理日志
            if (newsList.isEmpty()) {
                updateTaskUrlLog(taskUrlLog, 0, "NO_NEWS", "未找到符合条件的新闻");
            } else {
                updateTaskUrlLog(taskUrlLog, newsList.size(), "SUCCESS", null);
            }
        } catch (Exception e) {
            log.error("新闻任务异常", e);
            // 记录错误信息
            if (taskUrlLog != null) {
                updateTaskUrlLog(taskUrlLog, 0, "ERROR", e.getMessage());
            }
        } finally {
            countDownLatch.countDown();
            latestSpiderTaskService.release();
            log.info("剩余任务数量:{}", countDownLatch.getCount());
            aiTaskComponent.startAiTask(newsList, taskId);
        }
    }

    /**
     * 创建任务URL处理日志
     */
    private TaskUrlLog createTaskUrlLog(String url, Long taskId) {
        TaskUrlLog taskUrlLog = new TaskUrlLog();
        taskUrlLog.setTaskId(taskId);
        taskUrlLog.setUrl(url);
        taskUrlLog.setNewsCount(0);
        taskUrlLog.setStatus("PROCESSING");
        taskUrlLog.setIsDelete("N");
        taskUrlLog.setCreateTime(new Date());
        taskUrlLog.setUpdateTime(new Date());

        try {
            taskUrlLogMapper.insert(taskUrlLog);
        } catch (Exception e) {
            log.error("创建任务URL处理日志失败", e);
        }

        return taskUrlLog;
    }

    /**
     * 更新任务URL处理日志
     */
    private void updateTaskUrlLog(TaskUrlLog taskUrlLog, int newsCount, String status, String errorMessage) {
        try {
            taskUrlLog.setNewsCount(newsCount);
            taskUrlLog.setStatus(status);
            taskUrlLog.setErrorMessage(errorMessage);
            taskUrlLog.setUpdateTime(new Date());
            taskUrlLogMapper.updateById(taskUrlLog);
        } catch (Exception e) {
            log.error("更新任务URL处理日志失败", e);
        }
    }

    private News getNews(Article article) {
        News news = new News();
        news.setTitle(article.getTitle());
        news.setContent(article.getContent());
        // 使用TikaUtil将HTML内容转换为紧凑型纯文本
        if (article.getContent() != null && !article.getContent().trim().isEmpty()) {
            try {
                String tikaText = TikaUtil.htmlToCompactText(article.getContent());
                news.setTikaContent(tikaText);
            } catch (Exception e) {
                log.warn("Failed to convert HTML to compact text using Tika for URL: {}", article.getUrl(), e);
                // 如果Tika处理失败，使用简化版本
                try {
                    String fallbackText = TikaUtil.htmlToText(article.getContent());
                    news.setTikaContent(TikaUtil.toCompactText(fallbackText));
                } catch (Exception e2) {
                    log.warn("Fallback Tika conversion also failed, using basic regex cleanup for URL: {}", article.getUrl(), e2);
                    // 最后的回退：基本正则清理
                    news.setTikaContent(TikaUtil.toCompactText(article.getContent().replaceAll("<[^>]+>", " ")));
                }
            }
        }

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
