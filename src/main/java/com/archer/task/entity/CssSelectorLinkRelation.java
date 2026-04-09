package com.archer.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class CssSelectorLinkRelation {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String url;

    private String cssSelector;

    private String isDelete;

    private Date createTime;

    private Date updateTime;

}
