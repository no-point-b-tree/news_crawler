package com.archer.task.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "schedule.task")
public class ScheduleTaskProperties {

    private String cron = "0 0 1 * * ?";

    private String to;

    private String subject;

    private Mail mail = new Mail();

    @Data
    public static class Mail {
        private String from;
    }
}
