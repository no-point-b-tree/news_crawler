package com.archer.task.manager.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class NewsLinkPageRequest extends PageRequest {
    private String keyword;
    private List<String> langs;
}