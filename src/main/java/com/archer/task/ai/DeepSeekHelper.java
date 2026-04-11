package com.archer.task.ai;

import com.archer.config.DeepSeekConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeepSeekHelper implements AiHelper {

    private final DeepSeekConfig deepSeekConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public String processWithAi(String prompt) {
        // 检查是否启用
        if (!deepSeekConfig.getEnabled()) {
            log.warn("DeepSeek API未启用");
            throw new RuntimeException("DeepSeek API未启用");
        }

        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", deepSeekConfig.getModel());
            
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            requestBody.put("messages", new Map[]{message});
            
            requestBody.put("max_tokens", deepSeekConfig.getMaxTokens());
            requestBody.put("temperature", deepSeekConfig.getTemperature());

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepSeekConfig.getKey());

            // 创建HTTP实体
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("调用DeepSeek API，URL: {}", deepSeekConfig.getUrl());
            log.debug("请求参数: {}", objectMapper.writeValueAsString(requestBody));

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    deepSeekConfig.getUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                log.debug("DeepSeek API响应: {}", responseBody);
                
                // 解析响应，提取AI回复内容
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode choices = rootNode.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode messageNode = choices.get(0).path("message");
                    String content = messageNode.path("content").asText();
                    
                    // 尝试解析为JSON，如果不是JSON则包装为JSON
                    try {
                        // 验证是否是有效的JSON
                        objectMapper.readTree(content);
                        return content;
                    } catch (JsonProcessingException e) {
                        // 如果不是JSON，包装为JSON格式
                        Map<String, String> result = new HashMap<>();
                        result.put("response", content);
                        return objectMapper.writeValueAsString(result);
                    }
                } else {
                    throw new RuntimeException("DeepSeek API响应中没有choices字段");
                }
            } else {
                log.error("DeepSeek API调用失败，状态码: {}", response.getStatusCode());
                throw new RuntimeException("DeepSeek API调用失败，状态码: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("调用DeepSeek API时发生错误", e);
            throw new RuntimeException("调用DeepSeek API失败: " + e.getMessage(), e);
        }
    }

    /**
     * 专门用于处理HTML分析任务的方法
     * @param htmlContent HTML内容
     * @return 分析结果的JSON字符串
     */
    public String analyzeHtmlContent(String htmlContent) {
        String prompt = String.format("""
            请分析以下HTML内容，提取文章卡片、超链接和日期信息，并识别日期对应的Java日期格式pattern。
            要求返回JSON格式，包含以下字段：
            1. cssSelectorGroup: 文章卡片的CSS选择器（超链接和日期共同的前面公用元素的css选择器路径）
            2. hyperLinkCssSelector: 超链接的CSS选择器
            3. dateCssSelector: 日期的CSS选择器
            4. datePattern: 日期对应的Java日期格式pattern（如：yyyy-MM-dd HH:mm:ss）
            
            HTML内容：
            %s
            
            请只返回JSON格式的结果，不要包含其他说明文字。
            """, htmlContent.substring(0, Math.min(htmlContent.length(), 10000))); // 限制HTML长度
        
        return processWithAi(prompt);
    }

    /**
     * 通用文本处理
     * @param text 输入文本
     * @param instruction 处理指令
     * @return 处理结果的JSON字符串
     */
    public String processText(String text, String instruction) {
        String prompt = String.format("""
            %s
            
            输入文本：
            %s
            
            请处理以上文本，并返回JSON格式的结果。
            """, instruction, text);
        
        return processWithAi(prompt);
    }
}
