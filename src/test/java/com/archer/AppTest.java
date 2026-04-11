package com.archer;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

import com.archer.task.constant.RuleTypeEnum;
import com.archer.task.entity.Article;
import com.archer.task.entity.NewsLink;
import com.archer.task.service.LinkManagerService;
import com.archer.task.service.TaskService;
import lombok.extern.slf4j.Slf4j;
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


@SpringBootTest(classes = App.class)
@Slf4j
class AppTest {

    @Resource
    private LinkManagerService linkManagerService;

    @Resource
    private TaskService taskService;


    @Test
    void test() throws IOException {
//        taskService.startTask();
//        System.out.println(DateFormatUtils.format(new Date(),"MMMM d, yyyy",Locale.ENGLISH));
//        Article articleContent = latestSpiderTaskService.getArticleContent("https://www.secrss.com/");
//        Document doc = Jsoup.parse(articleContent.getContent(), "UTF-8");
//        Elements select = doc.select(".list-item .title a[href]");
//        List<Hyperlink> hyperlinks = JsoupExtractor.extractor(articleContent.getContent(), Arrays.asList("\\d+小时前"), 3);
//        System.out.println(hyperlinks);
//        Date current = new Date();
//        Date startDate = DateUtils.addDays(new Date(), -1);
//        List<Article> rssResult = latestSpiderTaskService.getRssResult(Arrays.asList("https://gbhackers.com/feed/"), startDate, current);
//        log.info("rssResult={}", rssResult.size());
    }


}
