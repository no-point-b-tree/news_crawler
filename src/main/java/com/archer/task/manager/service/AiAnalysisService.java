package com.archer.task.manager.service;

import java.util.List;

public interface AiAnalysisService {

    /**
     * 分析网站结构，提取CSS选择器配置
     * @param url 网站URL
     * @param htmlContent HTML内容
     * @return CSS选择器配置的JSON字符串
     */
    String analyzeWebsiteStructure(String url, String htmlContent);

    /**
     * 批量分析多个网站
     * @param websiteInfos 网站信息列表
     * @return 分析结果列表（JSON字符串）
     */
    List<String> batchAnalyzeWebsites(List<WebsiteInfo> websiteInfos);

    /**
     * 从分析结果生成CSS配置
     * @param analysisResult AI分析结果
     * @return 格式化后的CSS配置JSON
     */
    String generateCssConfigFromAnalysis(String analysisResult);

    /**
     * 验证CSS选择器的有效性
     * @param cssSelector CSS选择器
     * @param htmlContent HTML内容
     * @return 验证结果的JSON字符串
     */
    String validateCssSelector(String cssSelector, String htmlContent);

    /**
     * 网站信息DTO
     */
    class WebsiteInfo {
        private String url;
        private String htmlContent;

        public WebsiteInfo() {
        }

        public WebsiteInfo(String url, String htmlContent) {
            this.url = url;
            this.htmlContent = htmlContent;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getHtmlContent() {
            return htmlContent;
        }

        public void setHtmlContent(String htmlContent) {
            this.htmlContent = htmlContent;
        }
    }
}