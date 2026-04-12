package com.archer.task.manager.service;

import com.archer.task.manager.dto.NewsExportRequest;
import com.archer.task.manager.vo.NewsExportVO;

import java.util.List;

/**
 * 新闻导出服务接口
 */
public interface NewsExportService {
    
    /**
     * 获取导出数据列表
     */
    List<NewsExportVO> getExportData(NewsExportRequest request);
    
    /**
     * 导出为JSON格式
     */
    String exportToJson(NewsExportRequest request);
    
    /**
     * 导出为PDF格式
     */
    byte[] exportToPdf(NewsExportRequest request);
    
    /**
     * 获取导出文件名
     */
    String getExportFileName(NewsExportRequest request);
}