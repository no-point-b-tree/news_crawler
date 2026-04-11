package com.archer.task.manager.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RssLinkPageRequest extends PageRequest {
    private String keyword;
}