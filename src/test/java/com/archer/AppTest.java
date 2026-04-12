package com.archer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

import com.archer.task.constant.RuleTypeEnum;
import com.archer.task.entity.*;
import com.archer.task.manager.service.NewsLinkManagerService;
import com.archer.task.manager.service.RssLinkManagerService;
import com.archer.task.mapper.NewsLinkMapper;
import com.archer.task.mapper.RssLinkMapper;
import com.archer.task.service.LinkManagerService;
import com.archer.task.service.TaskService;
import com.archer.task.service.impl.LatestSpiderTaskServiceImpl;
import com.archer.task.util.TikaUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.archer.task.service.JsoupExtractor;
import com.archer.task.service.LatestSpiderTaskService;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Playwright.CreateOptions;
import com.microsoft.playwright.options.LoadState;

import jakarta.annotation.Resource;


@SpringBootTest(classes = App.class)
@Slf4j
class AppTest {

    @Resource
    private LinkManagerService linkManagerService;

    @Resource
    private TaskService taskService;

    @Resource
    private NewsLinkMapper newsLinkMapper;

    @Resource
    private RssLinkManagerService rssLinkManagerService;

    @Resource
    private RssLinkMapper rssLinkMapper;

    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, queue, new BasicThreadFactory.Builder().namingPattern("spider-%d").build());


    @Test
    void test() throws IOException, InterruptedException {
        // 快速删除无用的rss
        List<RssLink> rssLinkList = rssLinkManagerService.list();
        CountDownLatch cd = new CountDownLatch(rssLinkList.size());
        for (RssLink rssLink : rssLinkList) {
            threadPool.execute(new Task(rssLink, cd, rssLinkManagerService));
        }
        cd.await();
    }

    @Slf4j
    static class Task implements Runnable {


        private RssLink rssLink;

        private CountDownLatch countDownLatch;

        private RssLinkManagerService rssLinkManagerService;

        public Task(RssLink rssLink, CountDownLatch countDownLatch, RssLinkManagerService rssLinkManagerService) {
            this.rssLink = rssLink;
            this.countDownLatch = countDownLatch;
            this.rssLinkManagerService = rssLinkManagerService;
        }

        @Override
        public void run() {
            LatestSpiderTaskServiceImpl latestSpiderTaskServiceImpl = LatestSpiderTaskServiceImpl.getInstance();
            try {
                List<Article> articles = latestSpiderTaskServiceImpl.parseRss(rssLink.getUrl());
                if (articles == null || articles.isEmpty()) {
                    rssLinkManagerService.deleteRssLink(rssLink.getId());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                latestSpiderTaskServiceImpl.release();
                countDownLatch.countDown();
                log.info("剩余处理数量:{}", countDownLatch.getCount());
            }
        }
    }


}
