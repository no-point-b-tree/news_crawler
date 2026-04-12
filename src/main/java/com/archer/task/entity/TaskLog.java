package com.archer.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
@TableName("task_log")
public class TaskLog {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String taskType;

    private String isDelete;

    private Date createTime;

    private Date updateTime;

    private Date startDate;

    private Date endDate;

    private String status;

}
