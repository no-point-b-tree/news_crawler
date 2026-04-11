package com.archer;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

import com.archer.task.constant.RuleTypeEnum;
import com.archer.task.entity.Article;
import com.archer.task.entity.News;
import com.archer.task.entity.NewsLink;
import com.archer.task.manager.service.NewsLinkManagerService;
import com.archer.task.mapper.NewsLinkMapper;
import com.archer.task.service.LinkManagerService;
import com.archer.task.service.TaskService;
import com.archer.task.service.impl.LatestSpiderTaskServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.archer.task.entity.Hyperlink;
import com.archer.task.service.JsoupExtractor;
import com.archer.task.service.LatestSpiderTaskService;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Playwright.CreateOptions;
import com.microsoft.playwright.options.LoadState;

import jakarta.annotation.Resource;


//@SpringBootTest(classes = App.class)
@Slf4j
class AppTest {

    @Resource
    private LinkManagerService linkManagerService;

    @Resource
    private TaskService taskService;

    @Resource
    private NewsLinkMapper newsLinkMapper;

    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(4, 8, 60, TimeUnit.SECONDS, queue, new BasicThreadFactory.Builder().namingPattern("spider-%d").build());


    @Test
    void test() throws IOException {
        QueryWrapper<NewsLink> wrapper = new QueryWrapper<>();
        wrapper.eq("is_delete", "N");
        List<NewsLink> newsLinks = newsLinkMapper.selectList(wrapper);
        CountDownLatch countDownLatch = new CountDownLatch(newsLinks.size());
        CopyOnWriteArrayList<News> list = new CopyOnWriteArrayList<>();
        for (NewsLink newsLink : newsLinks) {
            String url = newsLink.getUrl();
            LatestSpiderTaskServiceImpl latestSpiderTaskService = new LatestSpiderTaskServiceImpl();
            latestSpiderTaskService.getArticleContent(url);
            Task task = new Task(list, countDownLatch, url, latestSpiderTaskService);
            threadPool.execute(task);
        }
        countDownLatch.countDown();
        List<Map<String, Object>> jsonList = new ArrayList<>();
        for (News news : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("url", news.getUrl());
            map.put("content", news.getContent());
            jsonList.add(map);
        }
        new ObjectMapper().writeValue(new File("D:/tmp/url_index.json"), jsonList);
    }

    class Task implements Runnable {

        private final CopyOnWriteArrayList<News> list;

        private final CountDownLatch countDownLatch;

        private final String url;

        private final LatestSpiderTaskServiceImpl latestSpiderTaskService;

        public Task(CopyOnWriteArrayList<News> list, CountDownLatch countDownLatch, String url, LatestSpiderTaskServiceImpl latestSpiderTaskService) {
            this.list = list;
            this.countDownLatch = countDownLatch;
            this.url = url;
            this.latestSpiderTaskService = latestSpiderTaskService;
        }

        @Override
        public void run() {
            try {
                Article article = latestSpiderTaskService.getArticleContent(url);
                News news = new News();
                news.setTitle(article.getTitle());
                news.setContent(article.getContent());
                news.setUrl(url);
                list.add(news);
            } catch (Exception e) {
                log.error("爬取任务失败", e);
            } finally {
                countDownLatch.countDown();
            }
        }
    }


}
