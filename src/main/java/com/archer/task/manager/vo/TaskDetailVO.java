package com.archer.task.manager.vo;

import lombok.Data;
import java.util.List;

@Data
public class TaskDetailVO {
    private Long taskId;
    private String taskType;
    private List<WebsiteSummary> websites;
    
    @Data
    public static class WebsiteSummary {
        private String name;
        private String url;
        private Integer newsCount;
    }
}