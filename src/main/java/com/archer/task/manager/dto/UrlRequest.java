package com.archer.task.manager.dto;

import lombok.Data;

@Data
public class UrlRequest {
    private String url;
    private String startDate;
    private String endDate;
}