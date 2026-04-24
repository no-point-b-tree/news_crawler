package com.archer.task.manager.service.impl;

import com.archer.task.constant.NewsStatusEnum;
import com.archer.task.entity.News;
import com.archer.task.manager.dto.NewsExportRequest;
import com.archer.task.manager.service.NewsExportService;
import com.archer.task.manager.vo.NewsExportVO;
import com.archer.task.mapper.NewsMapper;
import com.archer.task.util.PdfExportUtil;
import com.archer.task.util.WordExportUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 新闻导出服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NewsExportServiceImpl implements NewsExportService {
    
    private final NewsMapper newsMapper;
    private final ObjectMapper objectMapper;
    
    @Override
    public List<NewsExportVO> getExportData(NewsExportRequest request) {
        QueryWrapper<News> wrapper = new QueryWrapper<>();
        
        // 基本条件：未删除
        wrapper.eq("is_delete", "N");
        
        // 状态筛选
        if (request.getOnlyRelevant() != null && request.getOnlyRelevant()) {
            wrapper.eq("status", NewsStatusEnum.IS_RELEVANT.name());
        } else if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            wrapper.eq("status", request.getStatus());
        }
        
        // 任务ID筛选
        if (request.getTaskId() != null) {
            wrapper.eq("task_id", request.getTaskId());
        }
        
        // 时间范围筛选（可选）
        if (request.getStartTime() != null && !request.getStartTime().isEmpty()) {
            wrapper.ge("publish_time", request.getStartTime());
        }
        if (request.getEndTime() != null && !request.getEndTime().isEmpty()) {
            wrapper.le("publish_time", request.getEndTime());
        }
        
        // 关键词搜索
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like("title", request.getKeyword())
                    .or().like("content", request.getKeyword())
                    .or().like("summary", request.getKeyword()));
        }
        
        // 来源类型筛选
        if (request.getSourceType() != null && !request.getSourceType().isEmpty()) {
            if ("RSS".equalsIgnoreCase(request.getSourceType())) {
                wrapper.like("source_url", "rss").or().like("source_url", "feed").or().like("source_url", "xml");
            } else if ("NEWS".equalsIgnoreCase(request.getSourceType())) {
                wrapper.notLike("source_url", "rss")
                      .notLike("source_url", "feed")
                      .notLike("source_url", "xml");
            }
        }
        
        // 按发布时间倒序排列
        wrapper.orderByDesc("publish_time");
        
        // 查询数据
        List<News> newsList = newsMapper.selectList(wrapper);
        
        // 转换为VO
        return newsList.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    public String exportToJson(NewsExportRequest request) {
        List<NewsExportVO> data = getExportData(request);
        
        try {
            // 配置ObjectMapper以美化JSON输出
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            
            // 创建导出结果对象
            ExportResult exportResult = new ExportResult();
            exportResult.setExportTime(new Date());
            exportResult.setTotalCount(data.size());
            exportResult.setData(data);
            exportResult.setRequest(request);
            
            return mapper.writeValueAsString(exportResult);
        } catch (JsonProcessingException e) {
            log.error("JSON导出失败", e);
            throw new RuntimeException("JSON导出失败", e);
        }
    }
    
    @Override
    public byte[] exportToPdf(NewsExportRequest request) {
        List<NewsExportVO> data = getExportData(request);
        
        try {
            // 使用PDF工具类生成PDF
            return PdfExportUtil.generateNewsPdf(data, getExportFileName(request));
        } catch (Exception e) {
            log.error("PDF导出失败", e);
            throw new RuntimeException("PDF导出失败", e);
        }
    }
    
    @Override
    public byte[] exportToWord(NewsExportRequest request) {
        List<NewsExportVO> data = getExportData(request);
        
        try {
            // 使用Word工具类生成Word文档
            return WordExportUtil.generateNewsWord(data, getExportFileName(request));
        } catch (Exception e) {
            log.error("Word导出失败", e);
            throw new RuntimeException("Word导出失败", e);
        }
    }
    
    @Override
    public String getExportFileName(NewsExportRequest request) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        
        String prefix = "新闻导出";
        if (request.getOnlyRelevant() != null && request.getOnlyRelevant()) {
            prefix = "相关新闻导出";
        }
        
        return String.format("%s_%s", prefix, timestamp);
    }
    
    /**
     * 将News实体转换为导出VO
     */
    private NewsExportVO convertToVO(News news) {
        NewsExportVO vo = new NewsExportVO();
        vo.setId(news.getId());
        vo.setTitle(news.getTitle());
        vo.setZhTitle(news.getZhTitle());
        vo.setSummary(news.getSummary());
        vo.setContent(news.getContent());
        vo.setTikaContent(news.getTikaContent());
        vo.setChineseContent(news.getChineseContent());
        vo.setPublishTime(news.getPublishTime());
        vo.setSourceType(news.getSource());
        vo.setUrl(news.getUrl());
        vo.setSourceUrl(news.getSourceUrl());
        vo.setStatus(news.getStatus());
        vo.setCreateTime(news.getCreateTime());
        vo.setTaskId(news.getTaskId());
        return vo;
    }
    
    /**
     * 导出结果包装类
     */
    @Data
    private static class ExportResult {
        private Date exportTime;
        private Integer totalCount;
        private List<NewsExportVO> data;
        private NewsExportRequest request;
    }
}