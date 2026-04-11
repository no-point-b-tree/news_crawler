package com.archer.task.manager.dto;

import lombok.Data;

@Data
public class CssSelectorAddRequest {
    private String url;
    private String cssSelector;
}