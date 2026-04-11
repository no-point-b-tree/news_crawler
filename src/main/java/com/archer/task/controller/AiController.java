package com.archer.task.controller;

import com.archer.task.ai.DeepSeekHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final DeepSeekHelper deepSeekHelper;

    /**
     * 处理文本并返回AI分析结果
     */
    @PostMapping("/process")
    public String processText(@RequestBody ProcessRequest request) {
        log.info("收到AI处理请求，文本长度: {}", request.getText().length());
        try {
            return deepSeekHelper.processWithAi(request.getText());
        } catch (Exception e) {
            log.error("AI处理失败", e);
            return String.format("{\"error\": \"%s\"}", e.getMessage());
        }
    }

    /**
     * 分析HTML内容，提取CSS选择器配置
     */
    @PostMapping("/analyze-html")
    public String analyzeHtml(@RequestBody AnalyzeHtmlRequest request) {
        log.info("收到HTML分析请求，URL: {}, HTML长度: {}", request.getUrl(), request.getHtml().length());
        try {
            // 构建包含URL信息的提示
            String prompt = String.format("""
                请分析以下URL的HTML内容，提取文章卡片、超链接和日期信息：
                URL: %s
                
                HTML内容：
                %s
                
                要求返回JSON格式，包含以下字段：
                1. url: 原始URL
                2. cssSelectorGroup: 文章卡片的CSS选择器
                3. hyperLinkCssSelector: 超链接的CSS选择器
                4. dateCssSelector: 日期的CSS选择器
                5. datePattern: 日期对应的Java日期格式pattern
                6. enabled: 是否启用（固定为"ON"）
                
                请只返回JSON格式的结果。
                """, request.getUrl(), request.getHtml().substring(0, Math.min(request.getHtml().length(), 8000)));
            
            return deepSeekHelper.processWithAi(prompt);
        } catch (Exception e) {
            log.error("HTML分析失败", e);
            return String.format("{\"error\": \"%s\"}", e.getMessage());
        }
    }

    /**
     * 批量分析HTML内容
     */
    @PostMapping("/batch-analyze")
    public String batchAnalyze(@RequestBody BatchAnalyzeRequest request) {
        log.info("收到批量分析请求，共 {} 个HTML内容", request.getItems().size());
        try {
            StringBuilder results = new StringBuilder("[");
            for (int i = 0; i < request.getItems().size(); i++) {
                AnalyzeHtmlRequest item = request.getItems().get(i);
                try {
                    String result = analyzeHtml(item);
                    results.append(result);
                    if (i < request.getItems().size() - 1) {
                        results.append(",");
                    }
                    log.info("第 {} 个HTML分析完成", i + 1);
                } catch (Exception e) {
                    log.error("第 {} 个HTML分析失败: {}", i + 1, e.getMessage());
                    results.append(String.format("{\"url\": \"%s\", \"error\": \"%s\"}", item.getUrl(), e.getMessage()));
                    if (i < request.getItems().size() - 1) {
                        results.append(",");
                    }
                }
            }
            results.append("]");
            return results.toString();
        } catch (Exception e) {
            log.error("批量分析失败", e);
            return String.format("{\"error\": \"%s\"}", e.getMessage());
        }
    }

    // 请求DTO类
    @lombok.Data
    public static class ProcessRequest {
        private String text;
    }

    @lombok.Data
    public static class AnalyzeHtmlRequest {
        private String url;
        private String html;
    }

    @lombok.Data
    public static class BatchAnalyzeRequest {
        private java.util.List<AnalyzeHtmlRequest> items;
    }
}