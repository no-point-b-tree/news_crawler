package com.archer.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("match_rule")
public class MatchRule {

    private Long id;

    private String rule;

    // date regex两种
    private String ruleType;

    private String isDelete;


    private Date createTime;

    private Date updateTime;
}
