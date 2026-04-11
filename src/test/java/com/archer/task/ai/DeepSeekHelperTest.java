package com.archer.task.ai;

import com.archer.config.DeepSeekConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class DeepSeekHelperTest {

    @Autowired
    private DeepSeekHelper deepSeekHelper;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private DeepSeekConfig deepSeekConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testProcessWithAi() throws Exception {
        // 模拟配置
        when(deepSeekConfig.getUrl()).thenReturn("https://api.deepseek.com/v1/chat/completions");
        when(deepSeekConfig.getKey()).thenReturn("test-api-key");

        // 模拟API响应
        String mockResponse = """
            {
                "id": "chatcmpl-123",
                "object": "chat.completion",
                "created": 1677652288,
                "model": "deepseek-chat",
                "choices": [{
                    "index": 0,
                    "message": {
                        "role": "assistant",
                        "content": "{\\"result\\": \\"测试响应\\"}"
                    },
                    "finish_reason": "stop"
                }],
                "usage": {
                    "prompt_tokens": 9,
                    "completion_tokens": 12,
                    "total_tokens": 21
                }
            }
            """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // 测试调用
        String result = deepSeekHelper.processWithAi("测试提示");
        System.out.println("AI响应: " + result);
    }

    @Test
    void testAnalyzeHtmlContent() throws Exception {
        // 模拟配置
        when(deepSeekConfig.getUrl()).thenReturn("https://api.deepseek.com/v1/chat/completions");
        when(deepSeekConfig.getKey()).thenReturn("test-api-key");

        // 模拟HTML分析响应
        String mockResponse = """
            {
                "id": "chatcmpl-456",
                "object": "chat.completion",
                "created": 1677652288,
                "model": "deepseek-chat",
                "choices": [{
                    "index": 0,
                    "message": {
                        "role": "assistant",
                        "content": "{\\"cssSelectorGroup\\": \\"div.article-item\\", \\"hyperLinkCssSelector\\": \\"a.title\\", \\"dateCssSelector\\": \\"span.time\\", \\"datePattern\\": \\"yyyy-MM-dd HH:mm:ss\\"}"
                    },
                    "finish_reason": "stop"
                }],
                "usage": {
                    "prompt_tokens": 100,
                    "completion_tokens": 50,
                    "total_tokens": 150
                }
            }
            """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // 测试HTML分析
        String html = "<div class=\"article-item\"><a class=\"title\" href=\"/article/1\">文章标题</a><span class=\"time\">2024-01-01 10:00:00</span></div>";
        String result = deepSeekHelper.analyzeHtmlContent(html);
        System.out.println("HTML分析结果: " + result);
    }
}