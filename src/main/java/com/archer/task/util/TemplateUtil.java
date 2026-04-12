package com.archer.task.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板工具类
 * 使用 ${variable} 语法进行变量替换
 */
@Component
public class TemplateUtil {
    
    private final ObjectMapper objectMapper;
    
    public TemplateUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * 渲染字符串模板
     * 
     * @param template 模板字符串，使用 ${variable} 语法
     * @param model 数据模型
     * @return 渲染后的字符串
     */
    public String renderString(String template, Map<String, Object> model) {
        if (template == null || model == null) {
            return template;
        }
        
        String result = template;
        
        // 替换所有 ${variable} 格式的变量
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(template);
        
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            Object value = model.get(variableName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }
    
    /**
     * 渲染字符串模板（使用对象）
     */
    public String renderString(String template, Object object) {
        Map<String, Object> model = beanToMap(object);
        return renderString(template, model);
    }
    
    /**
     * 渲染文件模板
     * 
     * @param templateName 模板文件名（在resources/templates/目录下）
     * @param model 数据模型
     * @return 渲染后的字符串
     * @throws IOException 如果文件读取失败
     */
    public String renderTemplate(String templateName, Map<String, Object> model) throws IOException {
        String templateContent = loadTemplateFromClasspath("templates/" + templateName + ".txt");
        return renderString(templateContent, model);
    }
    
    /**
     * 渲染文件模板（使用对象）
     */
    public String renderTemplate(String templateName, Object object) throws IOException {
        Map<String, Object> model = beanToMap(object);
        return renderTemplate(templateName, model);
    }
    
    /**
     * 从classpath加载模板
     */
    private String loadTemplateFromClasspath(String path) throws IOException {
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IOException("模板文件未找到: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
    /**
     * 从文件系统加载模板
     */
    public String renderFromFile(String filePath, Map<String, Object> model) throws IOException {
        String templateContent = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
        return renderString(templateContent, model);
    }
    
    /**
     * 从文件系统加载模板（使用对象）
     */
    public String renderFromFile(String filePath, Object object) throws IOException {
        Map<String, Object> model = beanToMap(object);
        return renderFromFile(filePath, model);
    }
    
    /**
     * 将对象转换为Map
     */
    private Map<String, Object> beanToMap(Object object) {
        if (object instanceof Map) {
            return (Map<String, Object>) object;
        }
        
        try {
            return objectMapper.convertValue(object, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            // 如果Jackson转换失败，使用反射简单处理
            return simpleBeanToMap(object);
        }
    }
    
    /**
     * 简单的Bean到Map转换（使用反射）
     */
    private Map<String, Object> simpleBeanToMap(Object object) {
        Map<String, Object> map = new HashMap<>();
        
        try {
            Class<?> clazz = object.getClass();
            var fields = clazz.getDeclaredFields();
            
            for (var field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(object));
            }
            
            // 也检查getter方法
            var methods = clazz.getDeclaredMethods();
            for (var method : methods) {
                String methodName = method.getName();
                if (methodName.startsWith("get") && methodName.length() > 3 
                    && method.getParameterCount() == 0
                    && !methodName.equals("getClass")) {
                    
                    String fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                    try {
                        Object value = method.invoke(object);
                        map.put(fieldName, value);
                    } catch (Exception e) {
                        // 忽略无法调用的getter
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("无法将对象转换为Map: " + object.getClass().getName(), e);
        }
        
        return map;
    }
    
    /**
     * 高级功能：支持简单的条件表达式
     * 语法: ${condition ? 'trueValue' : 'falseValue'}
     */
    public String renderWithCondition(String template, Map<String, Object> model) {
        // 先替换条件表达式
        String step1 = template;
        Pattern conditionPattern = Pattern.compile("\\$\\{([^?]+)\\?\\s*'([^']*)'\\s*:\\s*'([^']*)'\\}");
        Matcher conditionMatcher = conditionPattern.matcher(template);
        
        StringBuffer sb = new StringBuffer();
        while (conditionMatcher.find()) {
            String condition = conditionMatcher.group(1).trim();
            String trueValue = conditionMatcher.group(2);
            String falseValue = conditionMatcher.group(3);
            
            // 简单条件判断：检查变量是否存在且不为空
            Object conditionValue = model.get(condition);
            boolean isTrue = conditionValue != null && 
                            (!(conditionValue instanceof String) || !((String) conditionValue).isEmpty());
            
            String replacement = isTrue ? trueValue : falseValue;
            conditionMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        conditionMatcher.appendTail(sb);
        
        // 然后替换普通变量
        return renderString(sb.toString(), model);
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 创建测试对象
        ObjectMapper mapper = new ObjectMapper();
        TemplateUtil util = new TemplateUtil(mapper);
        
        // 测试1: 基本变量替换
        String template1 = "Hello ${name}! Your age is ${age}.";
        Map<String, Object> model1 = new HashMap<>();
        model1.put("name", "John");
        model1.put("age", 30);
        
        String result1 = util.renderString(template1, model1);
        System.out.println("测试1:");
        System.out.println("模板: " + template1);
        System.out.println("结果: " + result1);
        System.out.println();
        
        // 测试2: 条件表达式
        String template2 = "Status: ${hasError ? 'Error' : 'OK'}";
        Map<String, Object> model2 = new HashMap<>();
        model2.put("hasError", true);
        
        String result2 = util.renderWithCondition(template2, model2);
        System.out.println("测试2:");
        System.out.println("模板: " + template2);
        System.out.println("结果: " + result2);
        System.out.println();
        
        // 测试3: 使用对象
        class User {
            private String username;
            private String email;
            
            public User(String username, String email) {
                this.username = username;
                this.email = email;
            }
            
            public String getUsername() { return username; }
            public String getEmail() { return email; }
        }
        
        String template3 = "User: ${username}, Email: ${email}";
        User user = new User("alice", "alice@example.com");
        
        String result3 = util.renderString(template3, user);
        System.out.println("测试3:");
        System.out.println("模板: " + template3);
        System.out.println("结果: " + result3);
    }
}