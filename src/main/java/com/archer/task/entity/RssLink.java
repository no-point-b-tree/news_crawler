package com.archer.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
@TableName("rss_link")
public class RssLink {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String domain;

    private String url;

    private String isDelete;

    private Date createTime;

    private Date updateTime;

}
