package com.archer.task.manager.dto;

import lombok.Data;

import java.util.List;

@Data
public class NewsLinkPageRequest {
    private String keyword;
    private List<String> langs;
    private Integer page = 1;
    private Integer size = 10;
}