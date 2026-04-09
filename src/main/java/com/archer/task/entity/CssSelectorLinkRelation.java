package com.archer.task.entity;

import lombok.Data;

import java.util.Date;

@Data
public class CssSelectorLinkRelation {


    private Long id;

    private String url;

    private String cssSelector;

    private String isDelete;

    private Date createTime;

    private Date updateTime;

}
