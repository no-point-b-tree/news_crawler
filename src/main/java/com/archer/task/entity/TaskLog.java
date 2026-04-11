package com.archer.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("task_log")
public class TaskLog {

    private Long id;

    private String taskType;

    private String isDelete;

    private Date createTime;

    private Date updateTime;

    private Date startDate;

    private Date endDate;

}
