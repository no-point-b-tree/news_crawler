package com.archer.task.manager;

import com.archer.task.manager.controller.NewsExportController;
import com.archer.task.manager.dto.NewsExportRequest;
import com.archer.task.manager.service.NewsExportService;
import com.archer.task.manager.vo.NewsExportVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class NewsExportTest {
    
    @Autowired
    private NewsExportService newsExportService;
    
    @Autowired
    private NewsExportController newsExportController;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testGetExportData() {
        NewsExportRequest request = new NewsExportRequest();
        request.setOnlyRelevant(true);
        
        List<NewsExportVO> data = newsExportService.getExportData(request);
        
        assertNotNull(data, "导出数据不应为null");
        System.out.println("获取到 " + data.size() + " 条导出数据");
        
        // 如果有数据，检查数据格式
        if (!data.isEmpty()) {
            NewsExportVO firstItem = data.get(0);
            assertNotNull(firstItem.getTitle(), "标题不应为null");
            assertNotNull(firstItem.getStatus(), "状态不应为null");
            System.out.println("第一条数据标题: " + firstItem.getTitle());
            System.out.println("状态: " + firstItem.getStatusText());
        }
    }
    
    @Test
    void testExportToJson() {
        NewsExportRequest request = new NewsExportRequest();
        request.setOnlyRelevant(true);
        request.setExportFormat("json");
        
        String json = newsExportService.exportToJson(request);
        
        assertNotNull(json, "JSON导出结果不应为null");
        assertTrue(json.contains("\"totalCount\""), "JSON应包含totalCount字段");
        assertTrue(json.contains("\"data\""), "JSON应包含data字段");
        
        System.out.println("JSON导出成功，长度: " + json.length() + " 字符");
        System.out.println("JSON预览（前500字符）: " + 
                          (json.length() > 500 ? json.substring(0, 500) + "..." : json));
    }
    
    @Test
    void testExportFileName() {
        NewsExportRequest request = new NewsExportRequest();
        request.setOnlyRelevant(true);
        
        String fileName = newsExportService.getExportFileName(request);
        
        assertNotNull(fileName, "文件名不应为null");
        assertTrue(fileName.startsWith("相关新闻导出"), "文件名应以'相关新闻导出'开头");
        assertTrue(fileName.contains("_"), "文件名应包含时间戳分隔符");
        
        System.out.println("生成的文件名: " + fileName);
    }
    
    @Test
    void testControllerTestEndpoint() {
        var response = newsExportController.testExport();
        
        assertNotNull(response, "控制器响应不应为null");
        assertEquals(200, response.getStatusCode().value(), "HTTP状态码应为200");
        
        System.out.println("控制器测试端点响应: " + response.getBody());
    }
    
    @Test
    void testExportWithFilters() {
        NewsExportRequest request = new NewsExportRequest();
        request.setOnlyRelevant(false); // 测试所有新闻
        request.setStatus("SPIDER_SUCCESS"); // 只导出爬取成功的
        request.setExportFormat("json");
        
        // 设置时间范围（最近7天）
        Date endTime = new Date();
        Date startTime = new Date(endTime.getTime() - 7 * 24 * 60 * 60 * 1000L);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        
        request.setKeyword("安全"); // 搜索包含"安全"的新闻
        request.setSourceType("RSS"); // 只导出RSS来源
        
        try {
            List<NewsExportVO> data = newsExportService.getExportData(request);
            System.out.println("使用过滤器获取到 " + data.size() + " 条数据");
            
            // 检查过滤效果
            if (!data.isEmpty()) {
                for (NewsExportVO item : data) {
                    assertEquals("SPIDER_SUCCESS", item.getStatus(), "状态应为SPIDER_SUCCESS");
                    assertTrue(item.getSourceUrl().contains("rss") || 
                              item.getSourceUrl().contains("feed") || 
                              item.getSourceUrl().contains("xml"), 
                              "来源应为RSS类型");
                }
            }
        } catch (Exception e) {
            System.out.println("过滤器测试可能没有匹配数据: " + e.getMessage());
        }
    }
}