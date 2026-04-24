package com.archer.task.manager.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
     * 新闻内容（原始内容）
     */
    @JsonIgnore
    private String content;
    
    /**
     * Tika处理后的内容（紧凑型文本）
     */
    private String tikaContent;
    
    /**
     * 中文翻译内容
     */
    private String chineseContent;
    
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
        if (sourceType != null && !sourceType.isEmpty()) {
            // 根据sourceType字段判断
            if (sourceType.contains("rss") || sourceType.contains("RSS") || 
                sourceType.contains("feed") || sourceType.contains("xml")) {
                return "RSS订阅";
            } else {
                return "新闻链接";
            }
        } else if (sourceUrl != null) {
            // 如果sourceType为空，再根据sourceUrl判断
            if (sourceUrl.contains("rss") || sourceUrl.contains("feed") || sourceUrl.contains("xml")) {
                return "RSS订阅";
            } else {
                return "新闻链接";
            }
        }
        return "未知";
    }
    
    /**
     * 获取来源显示文本（优先显示sourceType，如果没有则显示sourceUrl的域名）
     */
    public String getSourceDisplay() {
        if (sourceType != null && !sourceType.isEmpty()) {
            return sourceType;
        } else if (sourceUrl != null && !sourceUrl.isEmpty()) {
            // 从URL中提取域名作为来源
            try {
                java.net.URL url = new java.net.URL(sourceUrl);
                String host = url.getHost();
                // 移除www前缀
                if (host.startsWith("www.")) {
                    host = host.substring(4);
                }
                return host;
            } catch (Exception e) {
                return sourceUrl;
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