package com.archer.task.manager.dto;

import lombok.Data;

/**
 * 执行所有任务请求DTO
 */
@Data
public class StartAllTasksRequest {
    
    /**
     * 开始时间
     */
    private String startDate;
    
    /**
     * 结束时间
     */
    private String endDate;
    
    /**
     * 任务类型（可选）
     */
    private String taskType;
}