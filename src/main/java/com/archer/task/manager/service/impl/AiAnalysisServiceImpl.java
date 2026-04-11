package com.archer.task.manager.service.impl;

import com.archer.task.ai.DeepSeekHelper;
import com.archer.task.manager.service.AiAnalysisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private final DeepSeekHelper deepSeekHelper;
    private final ObjectMapper objectMapper;

    @Override
    public String analyzeWebsiteStructure(String url, String htmlContent) {
        log.info("开始分析网站结构，URL: {}", url);
        
        try {
            // 使用DeepSeekHelper分析HTML
            String aiResponse = deepSeekHelper.analyzeHtmlContent(htmlContent);
            
            // 解析AI响应
            JsonNode resultNode = objectMapper.readTree(aiResponse);
            
            // 构建完整的CSS选择器配置
            JsonNode configNode = objectMapper.createObjectNode()
                    .put("url", url)
                    .put("cssSelectorGroup", resultNode.path("cssSelectorGroup").asText())
                    .put("hyperLinkCssSelector", resultNode.path("hyperLinkCssSelector").asText())
                    .put("dateCssSelector", resultNode.path("dateCssSelector").asText())
                    .put("datePattern", resultNode.path("datePattern").asText())
                    .put("enabled", "ON");
            
            return objectMapper.writeValueAsString(configNode);
        } catch (Exception e) {
            log.error("网站结构分析失败，URL: {}", url, e);
            throw new RuntimeException("网站结构分析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> batchAnalyzeWebsites(List<WebsiteInfo> websiteInfos) {
        log.info("开始批量分析网站，共 {} 个", websiteInfos.size());
        
        List<String> results = new ArrayList<>();
        for (WebsiteInfo info : websiteInfos) {
            try {
                String result = analyzeWebsiteStructure(info.getUrl(), info.getHtmlContent());
                results.add(result);
                log.info("网站分析完成: {}", info.getUrl());
            } catch (Exception e) {
                log.error("网站分析失败: {}", info.getUrl(), e);
                try {
                    String errorResult = objectMapper.writeValueAsString(
                            objectMapper.createObjectNode()
                                    .put("url", info.getUrl())
                                    .put("error", e.getMessage())
                    );
                    results.add(errorResult);
                } catch (Exception jsonError) {
                    results.add("{\"url\": \"" + info.getUrl() + "\", \"error\": \"结果序列化失败\"}");
                }
            }
        }
        
        return results;
    }

    @Override
    public String generateCssConfigFromAnalysis(String analysisResult) {
        try {
            JsonNode analysisNode = objectMapper.readTree(analysisResult);
            
            // 这里可以根据分析结果进行进一步处理
            // 例如：验证CSS选择器的有效性，补充默认值等
            
            return objectMapper.writeValueAsString(analysisNode);
        } catch (Exception e) {
            log.error("生成CSS配置失败", e);
            throw new RuntimeException("生成CSS配置失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String validateCssSelector(String cssSelector, String htmlContent) {
        log.info("验证CSS选择器: {}", cssSelector);
        
        String prompt = String.format("""
            请验证以下CSS选择器在HTML内容中是否能正确匹配到元素：
            CSS选择器: %s
            
            HTML内容（片段）：
            %s
            
            请返回JSON格式的验证结果，包含以下字段：
            1. valid: 是否有效（true/false）
            2. matchCount: 匹配到的元素数量
            3. sampleElements: 匹配到的示例元素（前3个）
            4. suggestion: 改进建议（如果有）
            """, cssSelector, htmlContent.substring(0, Math.min(htmlContent.length(), 5000)));
        
        try {
            return deepSeekHelper.processWithAi(prompt);
        } catch (Exception e) {
            log.error("CSS选择器验证失败", e);
            return String.format("{\"valid\": false, \"error\": \"%s\"}", e.getMessage());
        }
    }
}