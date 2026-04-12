package com.archer.task.util;

import com.archer.task.manager.vo.NewsExportVO;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * PDF导出工具类
 */
@Component
@Slf4j
public class PdfExportUtil {
    
    // 字体定义
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
    
    /**
     * 生成新闻PDF
     */
    public static byte[] generateNewsPdf(List<NewsExportVO> newsList, String fileName) throws Exception {
        if (newsList == null || newsList.isEmpty()) {
            throw new IllegalArgumentException("没有数据可导出");
        }
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 创建文档
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            
            // 添加页眉页脚
            writer.setPageEvent(new PdfPageEvent());
            
            document.open();
            
            // 添加标题
            addTitle(document, "新闻导出报告", newsList.size());
            
            // 添加统计信息
            addStatistics(document, newsList);
            
            // 添加新闻列表
            addNewsList(document, newsList);
            
            // 添加导出信息
            addExportInfo(document);
            
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("生成PDF失败", e);
            throw e;
        }
    }
    
    /**
     * 添加标题
     */
    private static void addTitle(Document document, String title, int count) throws DocumentException {
        Paragraph titlePara = new Paragraph(title, TITLE_FONT);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingAfter(20);
        document.add(titlePara);
        
        Paragraph countPara = new Paragraph(String.format("共 %d 条新闻", count), HEADER_FONT);
        countPara.setAlignment(Element.ALIGN_CENTER);
        countPara.setSpacingAfter(30);
        document.add(countPara);
    }
    
    /**
     * 添加统计信息
     */
    private static void addStatistics(Document document, List<NewsExportVO> newsList) throws DocumentException {
        // 统计相关新闻数量
        long relevantCount = newsList.stream()
                .filter(news -> "IS_RELEVANT".equals(news.getStatus()))
                .count();
        
        // 统计来源类型
        long rssCount = newsList.stream()
                .filter(news -> news.getSourceUrl() != null && 
                        (news.getSourceUrl().contains("rss") || 
                         news.getSourceUrl().contains("feed") || 
                         news.getSourceUrl().contains("xml")))
                .count();
        long newsCount = newsList.size() - rssCount;
        
        // 创建表格
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);
        
        // 设置列宽
        float[] columnWidths = {1f, 2f};
        table.setWidths(columnWidths);
        
        // 添加表头
        addTableHeader(table, "统计项", "数值");
        
        // 添加数据行
        addTableRow(table, "总新闻数", String.valueOf(newsList.size()));
        addTableRow(table, "相关新闻数", String.valueOf(relevantCount));
        addTableRow(table, "RSS来源数", String.valueOf(rssCount));
        addTableRow(table, "新闻链接数", String.valueOf(newsCount));
        
        document.add(table);
    }
    
    /**
     * 添加新闻列表
     */
    private static void addNewsList(Document document, List<NewsExportVO> newsList) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("新闻列表", HEADER_FONT);
        sectionTitle.setSpacingBefore(20);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);
        
        int index = 1;
        for (NewsExportVO news : newsList) {
            addNewsItem(document, news, index++);
            
            // 每5条新闻后添加分页
            if (index % 5 == 0 && index < newsList.size()) {
                document.newPage();
            }
        }
    }
    
    /**
     * 添加单个新闻项
     */
    private static void addNewsItem(Document document, NewsExportVO news, int index) throws DocumentException {
        // 新闻序号和标题
        Paragraph itemTitle = new Paragraph();
        itemTitle.add(new Chunk(String.format("%d. ", index), HEADER_FONT));
        
        String displayTitle = "无标题";
        if (news.getZhTitle() != null && !news.getZhTitle().isEmpty()) {
            displayTitle = news.getZhTitle();
        } else if (news.getTitle() != null && !news.getTitle().isEmpty()) {
            displayTitle = news.getTitle();
        }
        
        if (displayTitle.length() > 100) {
            displayTitle = displayTitle.substring(0, 100) + "...";
        }
        itemTitle.add(new Chunk(displayTitle, HEADER_FONT));
        itemTitle.setSpacingAfter(5);
        document.add(itemTitle);
        
        // 创建详细信息表格
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(5);
        table.setSpacingAfter(15);
        
        float[] columnWidths = {3f, 7f};
        table.setWidths(columnWidths);
        
        // 添加新闻信息
        addInfoRow(table, "发布时间:", formatDate(news.getPublishTime()));
        addInfoRow(table, "来源类型:", news.getDisplaySourceType());
        addInfoRow(table, "完整链接:", news.getUrl() != null ? news.getUrl() : "无");
        
        // 摘要（可能跨行）
        String summary = news.getSummary();
        PdfPCell summaryLabelCell = new PdfPCell(new Phrase("摘要:", NORMAL_FONT));
        summaryLabelCell.setBorder(Rectangle.NO_BORDER);
        summaryLabelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        summaryLabelCell.setVerticalAlignment(Element.ALIGN_TOP);
        
        String summaryText = "无";
        if (summary != null && !summary.isEmpty()) {
            if (summary.length() > 200) {
                summaryText = summary.substring(0, 200) + "...";
            } else {
                summaryText = summary;
            }
        }
        PdfPCell summaryValueCell = new PdfPCell(new Phrase(summaryText, NORMAL_FONT));
        summaryValueCell.setBorder(Rectangle.NO_BORDER);
        summaryValueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        
        table.addCell(summaryLabelCell);
        table.addCell(summaryValueCell);
        
        document.add(table);
        
        // 添加分隔线
        Paragraph separator = new Paragraph();
        separator.add(new Chunk("________________________________________________________________________"));
        separator.setSpacingBefore(5);
        separator.setSpacingAfter(10);
        document.add(separator);
    }
    
    /**
     * 添加导出信息
     */
    private static void addExportInfo(Document document) throws DocumentException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        String exportTime = sdf.format(new Date());
        
        Paragraph exportInfo = new Paragraph(String.format("导出时间: %s", exportTime), SMALL_FONT);
        exportInfo.setAlignment(Element.ALIGN_RIGHT);
        exportInfo.setSpacingBefore(20);
        document.add(exportInfo);
    }
    
    /**
     * 添加表格表头
     */
    private static void addTableHeader(PdfPTable table, String header1, String header2) {
        PdfPCell cell1 = new PdfPCell(new Phrase(header1, HEADER_FONT));
        PdfPCell cell2 = new PdfPCell(new Phrase(header2, HEADER_FONT));
        
        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell2.setBackgroundColor(BaseColor.LIGHT_GRAY);
        
        table.addCell(cell1);
        table.addCell(cell2);
    }
    
    /**
     * 添加表格行
     */
    private static void addTableRow(PdfPTable table, String label, String value) {
        table.addCell(new Phrase(label, NORMAL_FONT));
        table.addCell(new Phrase(value, NORMAL_FONT));
    }
    
    /**
     * 添加信息行
     */
    private static void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, NORMAL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    /**
     * 格式化日期
     */
    private static String formatDate(Date date) {
        if (date == null) {
            return "未知";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
    
    /**
     * PDF页眉页脚事件
     */
    private static class PdfPageEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                // 添加页脚
                PdfPTable footer = new PdfPTable(1);
                footer.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
                
                // 页码
                Paragraph pageNum = new Paragraph(
                        String.format("第 %d 页 / 共 %d 页", writer.getPageNumber(), writer.getPageNumber()),
                        SMALL_FONT);
                pageNum.setAlignment(Element.ALIGN_CENTER);
                
                PdfPCell cell = new PdfPCell(pageNum);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                footer.addCell(cell);
                
                // 将页脚定位到页面底部
                footer.writeSelectedRows(0, -1, 
                        document.leftMargin(), 
                        document.bottomMargin() - 10, 
                        writer.getDirectContent());
                
            } catch (Exception e) {
                log.error("添加页脚失败", e);
            }
        }
    }
}