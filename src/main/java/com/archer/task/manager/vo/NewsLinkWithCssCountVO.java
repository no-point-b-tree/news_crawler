package com.archer.task.manager.vo;

import com.archer.task.entity.NewsLink;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NewsLinkWithCssCountVO extends NewsLink {
    
    /**
     * CSS选择器数量
     */
    private Integer cssSelectorCount;
    
    /**
     * 启用的CSS选择器数量
     */
    private Integer enabledCssSelectorCount;
    
    /**
     * 禁用的CSS选择器数量
     */
    private Integer disabledCssSelectorCount;
}