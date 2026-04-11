package com.archer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek.api")
public class DeepSeekConfig {
    private String url;
    private String key;
    private String model = "deepseek-chat";
    private Integer maxTokens = 2000;
    private Double temperature = 0.7;
    private Integer timeout = 30000;
    private Boolean enabled = true;
}