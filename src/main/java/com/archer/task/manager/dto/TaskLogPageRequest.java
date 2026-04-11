package com.archer.task.manager.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaskLogPageRequest extends PageRequest {
    private String url;
}