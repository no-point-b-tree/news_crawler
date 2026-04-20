package com.archer;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.*;

import com.archer.task.ai.DeepSeekHelper;
import com.archer.task.entity.*;
import com.archer.task.manager.service.NewsLinkManagerService;
import com.archer.task.manager.service.RssLinkManagerService;
import com.archer.task.mapper.NewsLinkMapper;
import com.archer.task.mapper.NewsMapper;
import com.archer.task.service.LatestSpiderTaskService;
import com.archer.task.service.TaskService;
import com.archer.task.service.impl.LatestSpiderTaskServiceImpl;
import com.archer.task.util.TemplateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;


@SpringBootTest(classes = App.class)
@Slf4j
class AppTest {


    @Resource
    private TaskService taskService;

    @Resource
    private NewsLinkManagerService newsLinkManagerService;

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    @Resource
    private RssLinkManagerService rssLinkManagerService;


    @Test
    void test() throws IOException, InterruptedException, ParseException {
        // 先测试简单的模板渲染
        QueryWrapper<RssLink> wrapper = new QueryWrapper<>();
        wrapper.gt("create_time", DateUtils.parseDate("2026-04-16", "yyyy-MM-dd"));
        List<RssLink> rssLinkList = rssLinkManagerService.list(wrapper);
        CountDownLatch countDownLatch = new CountDownLatch(rssLinkList.size());
        for (RssLink link : rssLinkList) {
            executor.execute(new Task(link, rssLinkManagerService, countDownLatch));
        }
        countDownLatch.await();
    }

    static class Task implements Runnable {

        private RssLink rssLink;

        private RssLinkManagerService rssLinkManagerService;

        private CountDownLatch countDownLatch;

        public Task(RssLink rssLink, RssLinkManagerService rssLinkManagerService, CountDownLatch countDownLatch) {
            this.rssLink = rssLink;
            this.rssLinkManagerService = rssLinkManagerService;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            LatestSpiderTaskServiceImpl task = LatestSpiderTaskServiceImpl.getInstance("dev");
            try {
                List<Article> urlList = task.parseRss(rssLink.getUrl());
                if (urlList.isEmpty()) {
                    UpdateWrapper<RssLink> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.set("is_delete", "Y");
                    updateWrapper.eq("id", rssLink.getId());
                    rssLinkManagerService.update(updateWrapper);
                }
                log.info("更新了rss:{}", urlList.size());
            } finally {
                task.release();
                countDownLatch.countDown();
                log.info("countDownLatch:{}", countDownLatch.getCount());
            }
        }
    }


}
