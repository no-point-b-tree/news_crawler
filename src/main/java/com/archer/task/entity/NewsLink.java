package com.archer.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
@TableName("news_link")
public class NewsLink {
    
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String domain;

    private String url;

    private String isDelete;

    private String name;

    private String lang;

    private Date createTime;

    private Date updateTime;

}
