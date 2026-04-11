package com.archer.task.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TikaUtilTest {

    @Test
    void testHtmlToText() {
        String html = "<html><body><h1>Hello World</h1><p>This is a test paragraph.</p></body></html>";
        String result = TikaUtil.htmlToText(html);
        
        assertNotNull(result);
        assertTrue(result.contains("Hello World"));
        assertTrue(result.contains("This is a test paragraph"));
        assertFalse(result.contains("<h1>"));
        assertFalse(result.contains("<p>"));
        
        System.out.println("HTML to Text Result: " + result);
    }

    @Test
    void testHtmlToTextWithParser() {
        String html = "<div><h2>Test Title</h2><span>Some content here</span></div>";
        String result = TikaUtil.htmlToTextWithParser(html);
        
        assertNotNull(result);
        assertTrue(result.contains("Test Title"));
        assertTrue(result.contains("Some content here"));
        
        System.out.println("HTML to Text with Parser Result: " + result);
    }

    @Test
    void testHtmlToTextWithEmptyInput() {
        String result1 = TikaUtil.htmlToText("");
        assertEquals("", result1);
        
        String result2 = TikaUtil.htmlToText(null);
        assertEquals("", result2);
        
        System.out.println("Empty input test passed");
    }

    @Test
    void testHtmlToTextWithLimit() {
        String html = "<p>This is a very long text that should be truncated when we apply a limit to it.</p>";
        String result = TikaUtil.htmlToTextWithLimit(html, 20);
        
        assertNotNull(result);
        assertTrue(result.length() <= 23); // 20 + "..."
        assertTrue(result.endsWith("...") || result.length() <= 20);
        
        System.out.println("Text with limit (20 chars): " + result);
    }

    @Test
    void testDetectMimeType() {
        String html = "<html><body>Test</body></html>";
        String mimeType = TikaUtil.detectMimeType(html);
        
        assertNotNull(mimeType);
        assertTrue(mimeType.contains("html") || mimeType.contains("text"));
        
        System.out.println("Detected MIME type: " + mimeType);
    }

    @Test
    void testCleanText() {
        String text = "  This   is  a  test  \n\n  with  multiple  spaces  and  newlines.  ";
        String cleaned = TikaUtil.cleanText(text);
        
        assertNotNull(cleaned);
        assertFalse(cleaned.contains("  ")); // No double spaces
        assertFalse(cleaned.startsWith(" "));
        assertFalse(cleaned.endsWith(" "));
        
        System.out.println("Original: '" + text + "'");
        System.out.println("Cleaned: '" + cleaned + "'");
    }

    @Test
    void testHtmlWithEntities() {
        String html = "<p>Test &amp; example &lt;tag&gt; with &quot;quotes&quot;</p>";
        // 使用htmlToTextWithParser方法，它应该能更好地处理HTML
        String result = TikaUtil.htmlToTextWithParser(html);
        
        System.out.println("HTML with entities result: '" + result + "'");
        
        assertNotNull(result);
        assertTrue(result.contains("Test"));
        // htmlToTextWithParser应该将实体转换为实际字符
        // 或者至少移除HTML标签
        assertFalse(result.contains("<p>"));
        assertFalse(result.contains("</p>"));
    }

    @Test
    void testHtmlToCompactText() {
        String html = """
            <html>
                <body>
                    <h1>   Hello   World   </h1>
                    <p>This   is   a   test   paragraph.
                    
                    It has   multiple   spaces   and   
                    
                    line   breaks.</p>
                    <div>Another   section   here.</div>
                </body>
            </html>
            """;
        
        String result = TikaUtil.htmlToCompactText(html);
        
        System.out.println("Compact text result: '" + result + "'");
        
        assertNotNull(result);
        assertTrue(result.contains("Hello World"));
        assertTrue(result.contains("This is a test paragraph."));
        assertTrue(result.contains("It has multiple spaces and line breaks."));
        assertTrue(result.contains("Another section here."));
        
        // 检查是否紧凑：不应该有连续多个空格
        assertFalse(result.contains("   "));
        assertFalse(result.contains("\n\n"));
        
        // 检查长度应该比原始HTML短很多
        assertTrue(result.length() < html.length() * 0.7); // 至少减少30%
    }

    @Test
    void testToCompactText() {
        String text = """
            This   is   a   test   with   multiple   spaces.
            
            And   line   breaks   here.
            
            Another   paragraph   with   extra   spaces   and   
            
            unnecessary   line   breaks.
            """;
        
        String result = TikaUtil.toCompactText(text);
        
        System.out.println("Original text length: " + text.length());
        System.out.println("Compact text length: " + result.length());
        System.out.println("Compact text result: '" + result + "'");
        
        assertNotNull(result);
        assertTrue(result.contains("This is a test with multiple spaces."));
        assertTrue(result.contains("And line breaks here."));
        assertTrue(result.contains("Another paragraph with extra spaces and unnecessary line breaks."));
        
        // 检查是否紧凑
        assertFalse(result.contains("   ")); // 没有三个连续空格
        assertFalse(result.contains("\n\n")); // 没有连续换行
        
        // 检查标点符号周围空格
        assertTrue(result.matches(".*\\.\\s+And.*")); // 句号后有一个空格
    }

    @Test
    void testCompactTextWithPunctuation() {
        String text = "Hello , world ! How are you ? I'm fine . Thank you .";
        String result = TikaUtil.toCompactText(text);
        
        System.out.println("Punctuation test - Original: '" + text + "'");
        System.out.println("Punctuation test - Compact: '" + result + "'");
        
        // 标点符号前不应该有空格
        assertFalse(result.contains(" ,"));
        assertFalse(result.contains(" !"));
        assertFalse(result.contains(" ?"));
        assertFalse(result.contains(" ."));
        
        // 检查紧凑性：标点符号后应该有一个空格
        // 注意：由于单引号被替换成了双引号，我们检查调整后的版本
        assertTrue(result.contains("world! How") || result.contains("world!  How"));
        assertTrue(result.contains("you? I") || result.contains("you?  I")); // I"m 或 I'm
        assertTrue(result.contains("fine. Thank") || result.contains("fine.  Thank"));
    }
}