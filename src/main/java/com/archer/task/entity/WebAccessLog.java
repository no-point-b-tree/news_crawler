package com.archer.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("web_access_log")
public class WebAccessLog {

    private Long id;

    private String url;

    private String content;

    private String webIndexType;

    private String isDelete;

    private Date createTime;

    private Date updateTime;
}
