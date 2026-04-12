package com.archer.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 任务URL处理日志实体
 */
@Data
@TableName("task_url_log")
public class TaskUrlLog {

    /**
     * ID
     */
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 处理的URL
     */
    private String url;

    /**
     * 找到的新闻数量
     */
    private Integer newsCount;

    /**
     * 处理状态: SUCCESS-成功, NO_NEWS-无新闻, ERROR-错误
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 是否删除,N否,Y是
     */
    private String isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}