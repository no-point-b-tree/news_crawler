package com.archer.task.manager.service;

import com.archer.task.entity.News;
import com.archer.task.manager.service.impl.TaskLogManagerServiceImpl;
import com.archer.task.manager.vo.WebsiteNewsVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskLogManagerServiceTest {

    @Mock
    private com.archer.task.service.TaskLogService taskLogService;

    @Mock
    private com.archer.task.mapper.NewsMapper newsMapper;

    @InjectMocks
    private TaskLogManagerServiceImpl taskLogManagerService;

    private News newsWithTikaContent;
    private News newsWithoutTikaContent;
    private News newsWithHtmlContent;

    @BeforeEach
    void setUp() {
        // 创建有tikaContent的新闻
        newsWithTikaContent = new News();
        newsWithTikaContent.setId(1L);
        newsWithTikaContent.setTitle("测试新闻1");
        newsWithTikaContent.setContent("<h1>HTML内容</h1><p>这是HTML内容。</p>");
        newsWithTikaContent.setTikaContent("这是Tika处理的紧凑文本内容。");
        newsWithTikaContent.setPublishTime(new Date());
        newsWithTikaContent.setCreateTime(new Date());
        newsWithTikaContent.setSourceUrl("http://example.com");
        newsWithTikaContent.setStatus("SUCCESS");
        newsWithTikaContent.setUrl("http://example.com/news/1");

        // 创建没有tikaContent的新闻
        newsWithoutTikaContent = new News();
        newsWithoutTikaContent.setId(2L);
        newsWithoutTikaContent.setTitle("测试新闻2");
        newsWithoutTikaContent.setContent("这是普通文本内容。");
        newsWithoutTikaContent.setTikaContent(null); // 没有tikaContent
        newsWithoutTikaContent.setPublishTime(new Date());
        newsWithoutTikaContent.setCreateTime(new Date());
        newsWithoutTikaContent.setSourceUrl("http://example.com");
        newsWithoutTikaContent.setStatus("SUCCESS");
        newsWithoutTikaContent.setUrl("http://example.com/news/2");

        // 创建有HTML内容的新闻（没有tikaContent）
        newsWithHtmlContent = new News();
        newsWithHtmlContent.setId(3L);
        newsWithHtmlContent.setTitle("测试新闻3");
        newsWithHtmlContent.setContent("<div class=\"article\"><h2>标题</h2><p>段落内容</p></div>");
        newsWithHtmlContent.setTikaContent(""); // 空字符串
        newsWithHtmlContent.setPublishTime(new Date());
        newsWithHtmlContent.setCreateTime(new Date());
        newsWithHtmlContent.setSourceUrl("http://example.com");
        newsWithHtmlContent.setStatus("SUCCESS");
        newsWithHtmlContent.setUrl("http://example.com/news/3");
    }

    @Test
    void testGetWebsiteNewsWithTikaContent() {
        // 模拟数据库返回
        Page<News> newsPage = new Page<>();
        newsPage.setRecords(Arrays.asList(newsWithTikaContent));
        newsPage.setTotal(1);
        newsPage.setCurrent(1);
        newsPage.setSize(10);

        when(newsMapper.selectPage(any(Page.class), any())).thenReturn(newsPage);

        // 调用方法
        IPage<WebsiteNewsVO> result = taskLogManagerService.getWebsiteNews(
            "http://example.com", 1L, 1, 10
        );

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        
        WebsiteNewsVO vo = result.getRecords().get(0);
        assertEquals("测试新闻1", vo.getTitle());
        // 应该使用tikaContent而不是原始content
        assertEquals("这是Tika处理的紧凑文本内容。", vo.getContent());
        assertNotEquals("<h1>HTML内容</h1><p>这是HTML内容。</p>", vo.getContent());
    }

    @Test
    void testGetWebsiteNewsWithoutTikaContent() {
        // 模拟数据库返回
        Page<News> newsPage = new Page<>();
        newsPage.setRecords(Arrays.asList(newsWithoutTikaContent));
        newsPage.setTotal(1);
        newsPage.setCurrent(1);
        newsPage.setSize(10);

        when(newsMapper.selectPage(any(Page.class), any())).thenReturn(newsPage);

        // 调用方法
        IPage<WebsiteNewsVO> result = taskLogManagerService.getWebsiteNews(
            "http://example.com", 1L, 1, 10
        );

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        
        WebsiteNewsVO vo = result.getRecords().get(0);
        assertEquals("测试新闻2", vo.getTitle());
        // 没有tikaContent，应该使用原始content
        assertEquals("这是普通文本内容。", vo.getContent());
    }

    @Test
    void testGetWebsiteNewsWithEmptyTikaContent() {
        // 模拟数据库返回
        Page<News> newsPage = new Page<>();
        newsPage.setRecords(Arrays.asList(newsWithHtmlContent));
        newsPage.setTotal(1);
        newsPage.setCurrent(1);
        newsPage.setSize(10);

        when(newsMapper.selectPage(any(Page.class), any())).thenReturn(newsPage);

        // 调用方法
        IPage<WebsiteNewsVO> result = taskLogManagerService.getWebsiteNews(
            "http://example.com", 1L, 1, 10
        );

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        
        WebsiteNewsVO vo = result.getRecords().get(0);
        assertEquals("测试新闻3", vo.getTitle());
        // tikaContent是空字符串，应该使用原始content
        assertEquals("<div class=\"article\"><h2>标题</h2><p>段落内容</p></div>", vo.getContent());
    }

    @Test
    void testGetWebsiteNewsMixedContent() {
        // 模拟数据库返回混合数据
        Page<News> newsPage = new Page<>();
        newsPage.setRecords(Arrays.asList(
            newsWithTikaContent,
            newsWithoutTikaContent,
            newsWithHtmlContent
        ));
        newsPage.setTotal(3);
        newsPage.setCurrent(1);
        newsPage.setSize(10);

        when(newsMapper.selectPage(any(Page.class), any())).thenReturn(newsPage);

        // 调用方法
        IPage<WebsiteNewsVO> result = taskLogManagerService.getWebsiteNews(
            "http://example.com", 1L, 1, 10
        );

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.getRecords().size());
        
        // 检查每个记录的内容
        List<WebsiteNewsVO> records = result.getRecords();
        
        // 第一个：有tikaContent
        assertEquals("这是Tika处理的紧凑文本内容。", records.get(0).getContent());
        
        // 第二个：没有tikaContent
        assertEquals("这是普通文本内容。", records.get(1).getContent());
        
        // 第三个：空tikaContent
        assertEquals("<div class=\"article\"><h2>标题</h2><p>段落内容</p></div>", records.get(2).getContent());
    }
}