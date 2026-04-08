package com.archer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import com.archer.task.constant.RuleTypeEnum;
import com.archer.task.entity.Article;
import com.archer.task.service.LinkManagerService;
import com.archer.task.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
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


@SpringBootTest(classes = App.class)
@Slf4j
class AppTest {

    @Resource
    private LatestSpiderTaskService latestSpiderTaskService;

    @Resource
    private LinkManagerService linkManagerService;

    @Resource
    private TaskService taskService;


    @Test
    void test() throws IOException {
        taskService.startTask();
//        System.out.println(DateFormatUtils.format(new Date(),"MMMM d, yyyy",Locale.ENGLISH));
//        Article articleContent = latestSpiderTaskService.getArticleContent("https://gbhackers.com/");
//        JsoupExtractor.extractor(articleContent.getContent(),Arrays.asList("MMMM d, yyyy"),2);
//        Date current = new Date();
//        Date startDate = DateUtils.addDays(new Date(), -1);
//        List<Article> rssResult = latestSpiderTaskService.getRssResult(Arrays.asList("https://gbhackers.com/feed/"), startDate, current);
//        log.info("rssResult={}", rssResult.size());
    }


}
