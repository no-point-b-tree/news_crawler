package com.archer.task.manager.controller;

import com.archer.task.manager.dto.NewsExportRequest;
import com.archer.task.manager.service.NewsExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

/**
 * 新闻导出控制器
 */
@RestController
@RequestMapping("/api/manager/news-export")
@RequiredArgsConstructor
@Slf4j
public class NewsExportController {
    
    private final NewsExportService newsExportService;
    
    /**
     * 导出新闻数据
     */
    @PostMapping("/export")
    public ResponseEntity<?> exportNews(@RequestBody NewsExportRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body("请求参数不能为空");
            }
            
            if (request.getExportFormat() == null || request.getExportFormat().isEmpty()) {
                return ResponseEntity.badRequest().body("导出格式不能为空");
            }
            
            String fileName = newsExportService.getExportFileName(request);
            
            if ("json".equalsIgnoreCase(request.getExportFormat())) {
                // 导出为JSON
                String jsonContent = newsExportService.exportToJson(request);
                
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                String.format("attachment; filename=\"%s.json\"", fileName))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(jsonContent);
                        
            } else if ("pdf".equalsIgnoreCase(request.getExportFormat())) {
                // 导出为PDF
                byte[] pdfContent = newsExportService.exportToPdf(request);
                
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                String.format("attachment; filename=\"%s.pdf\"", fileName))
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdfContent);
                        
            } else {
                return ResponseEntity.badRequest().body("不支持的导出格式: " + request.getExportFormat());
            }
            
        } catch (Exception e) {
            log.error("导出失败", e);
            return ResponseEntity.internalServerError().body("导出失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取导出数据预览（不下载文件）
     */
    @PostMapping("/preview")
    public ResponseEntity<?> previewExportData(@RequestBody NewsExportRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body("请求参数不能为空");
            }
            
            // 获取数据但不生成文件
            String jsonContent = newsExportService.exportToJson(request);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonContent);
                    
        } catch (Exception e) {
            log.error("预览失败", e);
            return ResponseEntity.internalServerError().body("预览失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试导出服务
     */
    @GetMapping("/test")
    public ResponseEntity<?> testExport() {
        try {
            NewsExportRequest request = new NewsExportRequest();
            request.setOnlyRelevant(true);
            request.setExportFormat("json");
            
            String result = newsExportService.exportToJson(request);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("导出服务测试成功，数据条数: " + 
                          (result.contains("\"totalCount\"") ? "已获取" : "未知"));
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("导出服务测试失败: " + e.getMessage());
        }
    }
}