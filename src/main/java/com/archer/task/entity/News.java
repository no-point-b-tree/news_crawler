package com.archer.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("news")
public class News {

    private Long id;

    private String url;

    // 文章
    private String title;

    // 内容
    private String content;

    private String tiKaContent;

    private String source;

    private String summary;

    private String status;

    // 来源url 新闻页或者rss的feed页
    private String sourceUrl;

    private String isDelete;

    private Date publishTime;

    private Date createTime;

    private Date updateTime;

}
