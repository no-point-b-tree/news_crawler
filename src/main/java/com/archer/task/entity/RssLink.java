package com.archer.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("rss_link")
public class RssLink {

    private Long id;

    private String domain;

    private String url;

    private String isDelete;

    private Date createTime;

    private Date updateTime;

}
