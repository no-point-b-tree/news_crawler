package com.archer.task.manager.vo;

import lombok.Data;
import java.util.Date;

@Data
public class WebsiteNewsVO {
    private Long id;
    private String title;
    private String content;
    private Date publishTime;
    private Date createTime;
    private String sourceUrl;
    private String status;
    private String url;
}