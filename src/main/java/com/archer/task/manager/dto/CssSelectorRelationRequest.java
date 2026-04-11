package com.archer.task.manager.dto;

import lombok.Data;

@Data
public class CssSelectorRelationRequest {
    private String url;
    private String cssSelectorGroup;
    private String dateCssSelector;
    private String hyperLinkCssSelector;
    private String enabled;
}