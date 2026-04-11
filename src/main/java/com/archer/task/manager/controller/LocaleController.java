package com.archer.task.manager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locale")
public class LocaleController {

    /**
     * 获取所有Java Locale支持的语言
     */
    @GetMapping("/languages")
    public ResponseEntity<List<LocaleInfo>> getAllLanguages() {
        // 获取所有可用的Locale
        Locale[] availableLocales = Locale.getAvailableLocales();
        
        // 提取唯一的语言代码和显示名称
        Map<String, String> languageMap = new TreeMap<>();
        for (Locale locale : availableLocales) {
            String language = locale.getLanguage();
            String displayLanguage = locale.getDisplayLanguage(Locale.ENGLISH);
            
            if (!language.isEmpty() && !displayLanguage.isEmpty()) {
                languageMap.put(language, displayLanguage);
            }
        }
        
        // 转换为LocaleInfo列表
        List<LocaleInfo> languages = languageMap.entrySet().stream()
                .map(entry -> new LocaleInfo(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(LocaleInfo::getDisplayName))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(languages);
    }

    /**
     * 获取所有Java Locale支持的国家/地区
     */
    @GetMapping("/countries")
    public ResponseEntity<List<LocaleInfo>> getAllCountries() {
        // 获取所有可用的Locale
        Locale[] availableLocales = Locale.getAvailableLocales();
        
        // 提取唯一的国家代码和显示名称
        Map<String, String> countryMap = new TreeMap<>();
        for (Locale locale : availableLocales) {
            String country = locale.getCountry();
            String displayCountry = locale.getDisplayCountry(Locale.ENGLISH);
            
            if (!country.isEmpty() && !displayCountry.isEmpty()) {
                countryMap.put(country, displayCountry);
            }
        }
        
        // 转换为LocaleInfo列表
        List<LocaleInfo> countries = countryMap.entrySet().stream()
                .map(entry -> new LocaleInfo(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(LocaleInfo::getDisplayName))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(countries);
    }

    /**
     * 获取完整的Locale列表（语言+国家）
     */
    @GetMapping("/locales")
    public ResponseEntity<List<LocaleInfo>> getAllLocales() {
        // 获取所有可用的Locale
        Locale[] availableLocales = Locale.getAvailableLocales();
        
        // 转换为LocaleInfo列表
        List<LocaleInfo> locales = Arrays.stream(availableLocales)
                .filter(locale -> !locale.getLanguage().isEmpty() && !locale.getDisplayLanguage().isEmpty())
                .map(locale -> {
                    String code = locale.toString();
                    String displayName = locale.getDisplayName(Locale.ENGLISH);
                    return new LocaleInfo(code, displayName);
                })
                .sorted(Comparator.comparing(LocaleInfo::getDisplayName))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(locales);
    }

    /**
     * 获取常用的语言列表（精简版）
     */
    @GetMapping("/common-languages")
    public ResponseEntity<List<LocaleInfo>> getCommonLanguages() {
        List<LocaleInfo> commonLanguages = new ArrayList<>();
        
        // 添加常用语言
        commonLanguages.add(new LocaleInfo("zh", "Chinese"));
        commonLanguages.add(new LocaleInfo("en", "English"));
        commonLanguages.add(new LocaleInfo("ja", "Japanese"));
        commonLanguages.add(new LocaleInfo("ko", "Korean"));
        commonLanguages.add(new LocaleInfo("fr", "French"));
        commonLanguages.add(new LocaleInfo("de", "German"));
        commonLanguages.add(new LocaleInfo("es", "Spanish"));
        commonLanguages.add(new LocaleInfo("ru", "Russian"));
        commonLanguages.add(new LocaleInfo("ar", "Arabic"));
        commonLanguages.add(new LocaleInfo("pt", "Portuguese"));
        commonLanguages.add(new LocaleInfo("it", "Italian"));
        commonLanguages.add(new LocaleInfo("nl", "Dutch"));
        commonLanguages.add(new LocaleInfo("sv", "Swedish"));
        commonLanguages.add(new LocaleInfo("pl", "Polish"));
        commonLanguages.add(new LocaleInfo("tr", "Turkish"));
        commonLanguages.add(new LocaleInfo("vi", "Vietnamese"));
        commonLanguages.add(new LocaleInfo("th", "Thai"));
        commonLanguages.add(new LocaleInfo("id", "Indonesian"));
        commonLanguages.add(new LocaleInfo("ms", "Malay"));
        commonLanguages.add(new LocaleInfo("hi", "Hindi"));
        
        // 按显示名称排序
        commonLanguages.sort(Comparator.comparing(LocaleInfo::getDisplayName));
        
        return ResponseEntity.ok(commonLanguages);
    }

    /**
     * 验证语言代码是否有效
     */
    @GetMapping("/validate/{languageCode}")
    public ResponseEntity<Boolean> validateLanguageCode(String languageCode) {
        if (languageCode == null || languageCode.isEmpty()) {
            return ResponseEntity.ok(false);
        }
        
        // 检查是否是有效的语言代码
        Locale[] availableLocales = Locale.getAvailableLocales();
        boolean isValid = Arrays.stream(availableLocales)
                .anyMatch(locale -> locale.getLanguage().equals(languageCode));
        
        return ResponseEntity.ok(isValid);
    }

    /**
     * Locale信息DTO
     */
    public static class LocaleInfo {
        private String code;
        private String displayName;

        public LocaleInfo() {
        }

        public LocaleInfo(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }
}