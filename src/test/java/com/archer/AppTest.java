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
import com.archer.task.service.mail.MailService;
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
    private MailService mailService;


    @Test
    void test() throws IOException, InterruptedException, ParseException {
        mailService.sendJsonAttachment("hello","s.json");
    }



}
