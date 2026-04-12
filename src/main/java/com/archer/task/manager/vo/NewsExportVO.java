package com.archer.task.manager.vo;

import lombok.Data;

import java.util.Date;

/**
 * 新闻导出数据VO
 */
@Data
public class NewsExportVO {
    
    /**
     * 新闻ID
     */
    private Long id;
    
    /**
     * 新闻标题
     */
    private String title;
    
    /**
     * 中文标题（如果有）
     */
    private String zhTitle;
    
    /**
     * 新闻摘要
     */
    private String summary;
    
    /**
     * 发布时间
     */
    private Date publishTime;
    
    /**
     * 来源类型
     */
    private String sourceType;
    
    /**
     * 完整链接
     */
    private String url;
    
    /**
     * 来源URL（新闻页或RSS feed页）
     */
    private String sourceUrl;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 获取显示用的来源类型
     */
    public String getDisplaySourceType() {
        if (sourceUrl != null) {
            if (sourceUrl.contains("rss") || sourceUrl.contains("feed") || sourceUrl.contains("xml")) {
                return "RSS订阅";
            } else {
                return "新闻链接";
            }
        }
        return "未知";
    }
    
    /**
     * 获取状态显示文本
     */
    public String getStatusText() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case "SPIDER_SUCCESS":
                return "爬取成功";
            case "IS_RELEVANT":
                return "具备相关性";
            case "IS_NOT_RELEVANT":
                return "不相关";
            case "AI_DEAL_FAIL":
                return "AI处理失败";
            default:
                return status;
        }
    }
}