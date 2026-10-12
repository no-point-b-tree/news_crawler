package com.archer.task.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;

public class TikaUtil {

    private static final Tika tika = new Tika();
    private static final HtmlParser htmlParser = new HtmlParser();

    /**
     * 将HTML内容转换为纯文本
     *
     * @param htmlContent HTML内容字符串
     * @return 转换后的纯文本
     */
    public static String htmlToText(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return "";
        }

        try (InputStream inputStream = new ByteArrayInputStream(htmlContent.getBytes(StandardCharsets.UTF_8))) {
            return tika.parseToString(inputStream);
        } catch (IOException | TikaException e) {
            throw new RuntimeException("Failed to parse HTML content", e);
        }
    }

    /**
     * 使用HTML解析器将HTML内容转换为纯文本（更精确的HTML解析）
     *
     * @param htmlContent HTML内容字符串
     * @return 转换后的纯文本
     */
    public static String htmlToTextWithParser(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return "";
        }

        try (InputStream inputStream = new ByteArrayInputStream(htmlContent.getBytes(StandardCharsets.UTF_8))) {
            BodyContentHandler handler = new BodyContentHandler(-1); // -1表示无限制
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            
            htmlParser.parse(inputStream, handler, metadata, context);
            return handler.toString().trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse HTML content with HTML parser", e);
        }
    }

    /**
     * 将HTML内容转换为紧凑型文本（移除多余的空格和换行）
     *
     * @param htmlContent HTML内容字符串
     * @return 转换后的紧凑型文本
     */
    public static String htmlToCompactText(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return "";
        }

        try (InputStream inputStream = new ByteArrayInputStream(htmlContent.getBytes(StandardCharsets.UTF_8))) {
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            
            htmlParser.parse(inputStream, handler, metadata, context);
            String text = handler.toString();
            
            // 转换为紧凑型文本
            return toCompactText(text);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse HTML content for compact text",e);
        }
    }

    /**
     * 将普通文本转换为紧凑型文本
     *
     * @param text 原始文本
     * @return 紧凑型文本
     */
    public static String toCompactText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        // 1. 移除所有HTML标签（如果还有的话）
        String compact = text.replaceAll("<[^>]+>", " ");
        
        // 2. 将多个连续空白字符（空格、制表符、换行等）替换为单个空格
        compact = compact.replaceAll("\\s+", " ");
        
        // 3. 移除句子开头和结尾的空格
        compact = compact.trim();
        
        // 4. 移除多余的空格在标点符号周围
        compact = compact.replaceAll("\\s+([.,;:!?])", "$1");
        compact = compact.replaceAll("([.,;:!?])\\s+", "$1 ");
        
        // 5. 确保标点符号后面只有一个空格（如果是句子结束）
        compact = compact.replaceAll("([.!?])\\s+", "$1 ");
        
        // 6. 移除双引号周围的空格（保留单引号，因为可能是缩写如 I'm）
        compact = compact.replaceAll("\\s*\"\\s*", "\"");
        
        // 7. 最后再次清理多余空格
        compact = compact.replaceAll("\\s+", " ").trim();
        
        return compact;
    }

    /**
     * 检测内容的MIME类型
     *
     * @param content 内容字符串
     * @return MIME类型
     */
    public static String detectMimeType(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "text/plain";
        }

        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            return tika.detect(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to detect MIME type", e);
        }
    }

    /**
     * 从HTML中提取文本并限制最大长度
     *
     * @param htmlContent HTML内容
     * @param maxLength 最大文本长度（字符数）
     * @return 提取的文本，如果超过最大长度则截断
     */
    public static String htmlToTextWithLimit(String htmlContent, int maxLength) {
        String text = htmlToText(htmlContent);
        if (text.length() > maxLength) {
            return text.substring(0, maxLength) + "...";
        }
        return text;
    }

    /**
     * 清理HTML文本，移除多余的空格和换行
     *
     * @param text 原始文本
     * @return 清理后的文本
     */
    public static String cleanText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        // 移除多余的空格和换行
        String cleaned = text.replaceAll("\\s+", " ").trim();
        // 移除HTML实体
        cleaned = cleaned.replaceAll("&[a-zA-Z0-9#]+;", " ");
        // 再次清理空格
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        
        return cleaned;
    }
}
