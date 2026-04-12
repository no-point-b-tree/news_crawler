package com.archer.task.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文章分析工具类
 * 用于分析文章内容是否与隐私保护、网络安全相关，并生成摘要
 */
@Component
public class ArticleAnalyzer {
    
    private final ObjectMapper objectMapper;
    
    // 隐私保护和网络安全相关关键词（多语言）
    private static final Set<String> PRIVACY_KEYWORDS = new HashSet<>(Arrays.asList(
        // 中文关键词
        "隐私", "隐私保护", "数据保护", "个人信息", "个人数据", "网络安全", "数据安全",
        "信息安全", "加密", "解密", "防火墙", "漏洞", "黑客", "攻击", "防护",
        "GDPR", "CCPA", "PIPL", "数据隐私", "隐私政策", "用户数据", "数据泄露",
        "身份盗窃", "网络犯罪", "恶意软件", "病毒", "木马", "钓鱼", "社会工程",
        
        // 英文关键词
        "privacy", "data protection", "cybersecurity", "network security", "information security",
        "encryption", "decryption", "firewall", "vulnerability", "hacker", "attack", "protection",
        "gdpr", "ccpa", "pipl", "data privacy", "privacy policy", "user data", "data breach",
        "identity theft", "cybercrime", "malware", "virus", "trojan", "phishing", "social engineering",
        
        // 其他语言关键词（示例）
        "プライバシー", "データ保護", "サイバーセキュリティ", // 日语
        "confidentialité", "protection des données", "cybersécurité" // 法语
    ));
    
    public ArticleAnalyzer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * 分析文章并返回JSON结果
     * 
     * @param content 文章内容
     * @return JSON字符串
     */
    public String analyzeArticle(String content) {
        AnalysisResult result = analyzeArticleContent(content);
        
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            // 如果JSON序列化失败，返回简单的JSON字符串
            return String.format("{\"summary\":\"%s\",\"isRelation\":%s}",
                    escapeJson(result.getSummary()), result.isRelation());
        }
    }
    
    /**
     * 分析文章内容
     * 
     * @param content 文章内容
     * @return 分析结果对象
     */
    public AnalysisResult analyzeArticleContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new AnalysisResult("文章内容为空", false);
        }
        
        // 1. 判断是否与隐私保护、网络安全相关
        boolean isRelated = isRelatedToPrivacySecurity(content);
        
        // 2. 生成摘要并翻译为中文
        String summary = generateSummary(content);
        
        return new AnalysisResult(summary, isRelated);
    }
    
    /**
     * 判断文章是否与隐私保护、网络安全相关
     */
    private boolean isRelatedToPrivacySecurity(String content) {
        String lowerContent = content.toLowerCase();
        
        // 检查是否包含关键词
        for (String keyword : PRIVACY_KEYWORDS) {
            if (lowerContent.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        
        // 检查相关短语模式
        String[] patterns = {
            "数据.*保护", "隐私.*保护", "网络.*安全", "信息.*安全",
            "data.*protection", "privacy.*protection", "cyber.*security", "information.*security"
        };
        
        for (String pattern : patterns) {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(content);
            if (m.find()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 生成文章摘要（不超过200字）
     */
    private String generateSummary(String content) {
        // 清理内容：移除多余空格和换行
        String cleanedContent = content.replaceAll("\\s+", " ").trim();
        
        // 如果内容本身就很短，直接返回
        if (cleanedContent.length() <= 200) {
            return cleanedContent;
        }
        
        // 尝试找到合适的截断点（在句号、问号、感叹号后截断）
        int cutPoint = 200;
        for (int i = 190; i < 210 && i < cleanedContent.length(); i++) {
            char c = cleanedContent.charAt(i);
            if (c == '。' || c == '.' || c == '!' || c == '？' || c == '?') {
                cutPoint = i + 1;
                break;
            }
        }
        
        String summary = cleanedContent.substring(0, Math.min(cutPoint, cleanedContent.length()));
        
        // 确保摘要以完整句子结束
        if (!summary.endsWith("。") && !summary.endsWith(".") && !summary.endsWith("!") && !summary.endsWith("？") && !summary.endsWith("?")) {
            summary += "。";
        }
        
        return summary;
    }
    
    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
    
    /**
     * 分析结果类
     */
    public static class AnalysisResult {
        private String summary;
        private boolean isRelation;
        
        public AnalysisResult(String summary, boolean isRelation) {
            this.summary = summary;
            this.isRelation = isRelation;
        }
        
        public String getSummary() {
            return summary;
        }
        
        public void setSummary(String summary) {
            this.summary = summary;
        }
        
        public boolean isRelation() {
            return isRelation;
        }
        
        public void setRelation(boolean relation) {
            isRelation = relation;
        }
    }
    
    /**
     * 快速使用方法示例
     */
    public static void main(String[] args) {
        // 示例：创建一个简单的分析器（实际使用中应该通过Spring注入）
        ObjectMapper mapper = new ObjectMapper();
        ArticleAnalyzer analyzer = new ArticleAnalyzer(mapper);
        
        // 测试文章内容
        String testContent = "随着数字化时代的到来，网络安全和隐私保护变得越来越重要。欧盟最近通过了《通用数据保护条例》（GDPR），要求企业加强对用户个人数据的保护。同时，中国也实施了《个人信息保护法》，明确规定了数据收集、使用和处理的规范。这些法规的出台，标志着全球对隐私保护的重视程度达到了新的高度。企业需要采取技术措施如加密、访问控制和数据脱敏来确保数据安全。";
        
        // 分析文章
        String result = analyzer.analyzeArticle(testContent);
        System.out.println("分析结果:");
        System.out.println(result);
        
        // 或者获取分析结果对象
        AnalysisResult analysisResult = analyzer.analyzeArticleContent(testContent);
        System.out.println("\n摘要: " + analysisResult.getSummary());
        System.out.println("是否相关: " + analysisResult.isRelation());
    }
}