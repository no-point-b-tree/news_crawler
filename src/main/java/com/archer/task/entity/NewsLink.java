package com.archer.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("news_link")
public class NewsLink {

    private Long id;

    private String domain;

    private String url;

    private String isDelete;

    private Integer minLevel;

    private String name;

    private String lang;

    private Date createTime;

    private Date updateTime;

}
