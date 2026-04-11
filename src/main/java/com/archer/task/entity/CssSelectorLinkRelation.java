package com.archer.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
@TableName("css_selector_link_relation")
public class CssSelectorLinkRelation {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String url;

    private String cssSelectorGroup;

    private String dateCssSelector;

    private String hyperLinkCssSelector;

    private String datePattern;

    private String enabled;

    private String isDelete;

    private Date createTime;

    private Date updateTime;

}
