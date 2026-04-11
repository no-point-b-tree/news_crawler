# TikaUtil 功能文档

## 概述
TikaUtil 是一个使用 Apache Tika 库实现的 HTML 转文本工具类，用于将 HTML 内容转换为纯文本格式。

## 功能特性

### 1. HTML 转文本
- `htmlToText(String htmlContent)`: 使用 Tika 的通用解析器将 HTML 转换为文本
- `htmlToTextWithParser(String htmlContent)`: 使用专门的 HTML 解析器进行更精确的转换
- `htmlToCompactText(String htmlContent)`: 将 HTML 转换为紧凑型文本（移除多余空格和换行）
- `htmlToTextWithLimit(String htmlContent, int maxLength)`: 转换并限制文本长度
- `toCompactText(String text)`: 将普通文本转换为紧凑型文本

### 2. 内容检测
- `detectMimeType(String content)`: 检测内容的 MIME 类型

### 3. 文本清理
- `cleanText(String text)`: 清理文本，移除多余空格、换行和 HTML 实体

## 集成位置

### 1. News 实体
- 新增 `tikaContent` 字段，用于存储 Tika 处理后的纯文本内容

### 2. 新闻抓取服务
- **SingleNewsTaskService**: 在创建 News 对象时自动调用 TikaUtil 处理 HTML 内容
- **SingleRssTaskService**: 在 RSS 抓取时同样自动处理 HTML 内容

### 3. 错误处理
- 如果 Tika 处理失败，会回退到简单的正则表达式替换方法
- 记录警告日志但不中断流程

## 使用方法

### 基本使用
```java
// 将 HTML 转换为文本
String html = "<h1>Title</h1><p>Content</p>";
String text = TikaUtil.htmlToText(html);

// 使用 HTML 解析器（更精确）
String text2 = TikaUtil.htmlToTextWithParser(html);

// 转换为紧凑型文本（推荐用于存储）
String compactText = TikaUtil.htmlToCompactText(html);

// 将现有文本转换为紧凑型
String existingText = "This   has   extra   spaces.";
String compact = TikaUtil.toCompactText(existingText);

// 限制文本长度
String limitedText = TikaUtil.htmlToTextWithLimit(html, 100);
```

### 在新闻抓取中的自动集成
```java
// 在 SingleNewsTaskService 和 SingleRssTaskService 中自动调用
// 现在使用紧凑型文本转换
if (article.getContent() != null && !article.getContent().trim().isEmpty()) {
    try {
        String tikaText = TikaUtil.htmlToCompactText(article.getContent());
        news.setTikaContent(tikaText);
    } catch (Exception e) {
        log.warn("Failed to convert HTML to compact text using Tika for URL: {}", article.getUrl(), e);
        // 回退方法：先普通转换，再紧凑处理
        try {
            String fallbackText = TikaUtil.htmlToText(article.getContent());
            news.setTikaContent(TikaUtil.toCompactText(fallbackText));
        } catch (Exception e2) {
            log.warn("Fallback Tika conversion also failed, using basic regex cleanup for URL: {}", article.getUrl(), e2);
            // 最后的回退：基本正则清理
            news.setTikaContent(TikaUtil.toCompactText(
                article.getContent().replaceAll("<[^>]+>", " ")
            ));
        }
    }
}
```

## 依赖配置
在 `pom.xml` 中添加了以下依赖：
```xml
<!-- Apache Tika for HTML/text extraction -->
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-core</artifactId>
    <version>2.9.2</version>
</dependency>
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-parsers-standard-package</artifactId>
    <version>2.9.2</version>
</dependency>
```

## 测试验证
创建了完整的单元测试 `TikaUtilTest`，验证以下功能：
1. HTML 转文本基本功能
2. 空输入处理
3. 文本长度限制
4. MIME 类型检测
5. 文本清理
6. HTML 实体处理
7. 紧凑型文本转换
8. 标点符号处理

## 优势
1. **准确性**: 使用 Apache Tika 库，业界标准的文本提取工具
2. **紧凑性**: 新的紧凑文本功能移除多余空格和换行，节省存储空间
3. **健壮性**: 包含多层错误处理和回退机制
4. **性能**: 静态方法，无需实例化
5. **集成简单**: 自动集成到现有的新闻抓取流程中

## 注意事项
1. Tika 的 `parseToString` 方法可能保留某些 HTML 结构，建议使用 `htmlToTextWithParser` 进行更精确的转换
2. `htmlToCompactText` 方法会移除所有多余空格和换行，生成最紧凑的文本
3. 对于非常大的 HTML 文档，可能需要调整内存设置
4. 错误处理会记录警告但继续执行，确保系统稳定性
5. 紧凑文本转换会正确处理标点符号周围的空格