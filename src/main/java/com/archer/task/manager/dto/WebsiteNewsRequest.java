package com.archer.task.manager.dto;

import lombok.Data;

@Data
public class WebsiteNewsRequest {
    private String sourceUrl;
    private Long taskId;
    private Integer page;
    private Integer size;
}