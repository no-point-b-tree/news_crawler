# 文章分析系统使用指南

## 概述
本系统提供两个核心工具：
1. **TemplateUtil** - 模板渲染工具
2. **ArticleAnalyzer** - 文章分析工具

## 问题解决：${content} 渲染失败

### 可能的原因
1. **数据模型中没有content字段**
2. **对象没有正确的getter方法**
3. **TemplateUtil注入或使用方式错误**

### 正确使用方法

#### 方法1：使用Map作为数据模型
```java
@Autowired
private TemplateUtil templateUtil;

public String processArticle(String articleContent) {
    // 创建数据模型
    Map<String, Object> model = new HashMap<>();
    model.put("content", articleContent);
    
    // 定义模板
    String template = "我的正文如下:\n${content}";
    
    // 渲染模板
    return templateUtil.renderString(template, model);
}
```

#### 方法2：使用对象作为数据模型
```java
// 定义文章类
public class Article {
    private String content;
    
    // 必须有无参构造函数
    public Article() {}
    
    public Article(String content) {
        this.content = content;
    }
    
    // 必须有getter方法
    public String getContent() {
        return content;
    }
    
    // 最好也有setter方法
    public void setContent(String content) {
        this.content = content;
    }
}

// 使用对象
public String processArticle(String articleContent) {
    @Autowired
    private TemplateUtil templateUtil;
    
    Article article = new Article(articleContent);
    String template = "我的正文如下:\n${content}";
    
    return templateUtil.renderString(template, article);
}
```

## 文章分析功能

### 使用ArticleAnalyzer
```java
@Autowired
private ArticleAnalyzer articleAnalyzer;

public String analyzeArticle(String content) {
    // 直接获取JSON结果
    String jsonResult = articleAnalyzer.analyzeArticle(content);
    
    // 或者获取分析结果对象
    ArticleAnalyzer.AnalysisResult result = articleAnalyzer.analyzeArticleContent(content);
    
    System.out.println("摘要: " + result.getSummary());
    System.out.println("是否相关: " + result.isRelation());
    
    return jsonResult;
}
```

### 完整示例：结合两个工具
```java
@Service
public class ArticleAnalysisService {
    
    @Autowired
    private TemplateUtil templateUtil;
    
    @Autowired
    private ArticleAnalyzer articleAnalyzer;
    
    /**
     * 处理文章分析请求
     */
    public String processArticleAnalysis(String articleContent) {
        try {
            // 1. 使用TemplateUtil渲染模板（如果需要）
            Map<String, Object> model = new HashMap<>();
            model.put("content", articleContent);
            
            String analysisTemplate = "正在分析文章...\n" +
                                    "文章长度: ${#strings.length(content)} 字符\n" +
                                    "分析时间: ${currentTime}";
            
            model.put("currentTime", new Date());
            String renderedTemplate = templateUtil.renderString(analysisTemplate, model);
            System.out.println(renderedTemplate);
            
            // 2. 使用ArticleAnalyzer分析文章
            String analysisResult = articleAnalyzer.analyzeArticle(articleContent);
            
            return analysisResult;
            
        } catch (Exception e) {
            // 错误处理
            return "{\"summary\":\"分析失败: " + e.getMessage() + "\",\"isRelation\":false}";
        }
    }
    
    /**
     * 批量处理文章
     */
    public List<String> batchAnalyzeArticles(List<String> articles) {
        return articles.stream()
                      .map(this::processArticleAnalysis)
                      .collect(Collectors.toList());
    }
}
```

## 测试用例

### 测试文章1：隐私保护相关
```java
String article1 = "欧盟《通用数据保护条例》（GDPR）于2018年5月25日正式生效。" +
                 "该条例规定了企业处理欧盟公民个人数据的要求，包括数据主体的权利、" +
                 "数据控制者和处理者的义务等。违反GDPR的企业可能面临高达全球年营业额4%的罚款。";
```

### 测试文章2：网络安全相关
```java
String article2 = "近年来，网络安全威胁日益严重。勒索软件攻击、数据泄露事件频发，" +
                 "给企业和个人带来巨大损失。企业需要建立完善的安全防护体系，" +
                 "包括防火墙、入侵检测系统、数据加密等措施。";
```

### 测试文章3：不相关主题
```java
String article3 = "春天来了，万物复苏。公园里的花儿都开了，人们纷纷外出踏青。" +
                 "这是一个适合户外活动的好季节，大家应该多参加体育锻炼，增强体质。";
```

## 输出示例

### 相关文章的输出
```json
{
    "summary": "欧盟《通用数据保护条例》（GDPR）于2018年5月25日正式生效。该条例规定了企业处理欧盟公民个人数据的要求，包括数据主体的权利、数据控制者和处理者的义务等。",
    "isRelation": true
}
```

### 不相关文章的输出
```json
{
    "summary": "春天来了，万物复苏。公园里的花儿都开了，人们纷纷外出踏青。这是一个适合户外活动的好季节。",
    "isRelation": false
}
```

## 故障排除

### 问题1：${content} 渲染为空白
**可能原因**：对象没有正确的getter方法
**解决方案**：确保Article类有`getContent()`方法

### 问题2：TemplateUtil无法注入
**可能原因**：没有添加@Component注解或Spring配置问题
**解决方案**：检查TemplateUtil类是否有`@Component`注解

### 问题3：JSON格式错误
**可能原因**：文章内容包含特殊字符
**解决方案**：ArticleAnalyzer已经处理了JSON转义

### 问题4：摘要过长
**可能原因**：文章没有合适的截断点
**解决方案**：ArticleAnalyzer会自动在200字左右寻找合适的截断点

## 性能优化建议

1. **缓存频繁使用的模板**
2. **批量处理文章时使用并行流**
3. **对于长文章，考虑分段处理**
4. **定期更新关键词库以适应新的安全威胁**

## 扩展功能

### 添加自定义关键词
```java
// 可以扩展ArticleAnalyzer来支持自定义关键词
public class CustomArticleAnalyzer extends ArticleAnalyzer {
    private Set<String> customKeywords = new HashSet<>();
    
    public void addCustomKeyword(String keyword) {
        customKeywords.add(keyword.toLowerCase());
    }
    
    @Override
    protected boolean isRelatedToPrivacySecurity(String content) {
        // 先检查父类的关键词
        boolean parentResult = super.isRelatedToPrivacySecurity(content);
        if (parentResult) {
            return true;
        }
        
        // 再检查自定义关键词
        String lowerContent = content.toLowerCase();
        for (String keyword : customKeywords) {
            if (lowerContent.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
}
```