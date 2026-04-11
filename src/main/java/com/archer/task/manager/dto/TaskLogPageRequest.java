package com.archer.task.manager.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaskLogPageRequest extends PageRequest {
    private String taskType;
    private String startDate;
    private String endDate;
}