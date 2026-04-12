package com.archer.task.manager.dto;

import lombok.Data;

/**
 * 新闻导出请求DTO
 */
@Data
public class NewsExportRequest {
    
    /**
     * 任务ID（可选）
     */
    private Long taskId;
    
    /**
     * 开始时间（可选）- 字符串格式避免序列化问题
     */
    private String startTime;
    
    /**
     * 结束时间（可选）- 字符串格式避免序列化问题
     */
    private String endTime;
    
    /**
     * 状态筛选（可选）
     */
    private String status;
    
    /**
     * 导出格式：pdf 或 json
     */
    private String exportFormat;
    
    /**
     * 是否只导出相关新闻（IS_RELEVANT状态）
     */
    private Boolean onlyRelevant = true;
    
    /**
     * 关键词搜索（可选）
     */
    private String keyword;
    
    /**
     * 来源类型筛选（可选）
     */
    private String sourceType;
}