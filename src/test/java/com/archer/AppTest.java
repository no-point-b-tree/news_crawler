package com.archer;

import java.io.*;
import java.util.*;

import com.archer.task.ai.DeepSeekHelper;
import com.archer.task.entity.*;
import com.archer.task.mapper.NewsMapper;
import com.archer.task.util.TemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;


@SpringBootTest(classes = App.class)
@Slf4j
class AppTest {

    @Resource
    private NewsMapper newsMapper;

    @Resource
    private TemplateUtil templateUtil;

    @Resource
    private DeepSeekHelper deepSeekHelper;


    @Test
    void test() throws IOException, InterruptedException {
        // 先测试简单的模板渲染

        News news = newsMapper.selectById(2043231860783005698L);
        String c = templateUtil.renderTemplate("summary", Collections.singletonMap("content", news.getTikaContent()));
        System.out.println(c);
        String result = deepSeekHelper.processWithAi(c);
        System.out.println(result);
        //
    }


}
