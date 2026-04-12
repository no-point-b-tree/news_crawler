package com.archer.task.util;

import com.archer.task.manager.vo.NewsExportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Word导出工具类
 */
@Component
@Slf4j
public class WordExportUtil {
    
    /**
     * 生成新闻Word文档
     */
    public static byte[] generateNewsWord(List<NewsExportVO> newsList, String fileName) throws Exception {
        if (newsList == null || newsList.isEmpty()) {
            throw new IllegalArgumentException("没有数据可导出");
        }
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 创建Word文档
            XWPFDocument document = new XWPFDocument();
            
            // 设置页面布局（避免不必要的分页）
            setPageLayout(document);
            
            // 添加标题
            addTitle(document, "新闻导出报告", newsList.size());
            
            // 添加统计信息
            addStatistics(document, newsList);
            
            // 添加导航目录
            addNavigation(document, newsList);
            
            // 添加新闻列表（紧凑布局）
            addNewsList(document, newsList);
            
            // 添加导出信息
            addExportInfo(document);
            
            // 保存文档
            document.write(baos);
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("生成Word失败", e);
            throw e;
        }
    }
    
    /**
     * 添加标题
     */
    private static void addTitle(XWPFDocument document, String title, int count) {
        // 主标题
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        titlePara.setSpacingAfter(200); // 减少间距
        
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText(title);
        titleRun.setFontSize(18); // 稍微减小字体
        titleRun.setBold(true);
        titleRun.setFontFamily("微软雅黑");
        
        // 副标题（数量）
        XWPFParagraph countPara = document.createParagraph();
        countPara.setAlignment(ParagraphAlignment.CENTER);
        countPara.setSpacingAfter(300); // 减少间距
        
        XWPFRun countRun = countPara.createRun();
        countRun.setText(String.format("共 %d 条新闻", count));
        countRun.setFontSize(12);
        countRun.setBold(true);
        countRun.setFontFamily("微软雅黑");
    }
    
    /**
     * 添加统计信息
     */
    private static void addStatistics(XWPFDocument document, List<NewsExportVO> newsList) {
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
        XWPFTable table = document.createTable(5, 2);
        table.setWidth("80%");
        
        // 设置表格样式
        CTTbl ctTbl = table.getCTTbl();
        CTTblPr tblPr = ctTbl.getTblPr() == null ? ctTbl.addNewTblPr() : ctTbl.getTblPr();
        tblPr.addNewTblW().setW(BigInteger.valueOf(8000)); // 80%宽度
        tblPr.addNewJc().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STJcTable.CENTER); // 居中
        
        // 设置表格边框
        CTTblBorders borders = tblPr.addNewTblBorders();
        borders.addNewTop().setVal(STBorder.SINGLE);
        borders.addNewBottom().setVal(STBorder.SINGLE);
        borders.addNewLeft().setVal(STBorder.SINGLE);
        borders.addNewRight().setVal(STBorder.SINGLE);
        borders.addNewInsideH().setVal(STBorder.SINGLE);
        borders.addNewInsideV().setVal(STBorder.SINGLE);
        
        // 表头
        setTableCell(table, 0, 0, "统计项", true);
        setTableCell(table, 0, 1, "数值", true);
        
        // 数据行
        setTableCell(table, 1, 0, "总新闻数");
        setTableCell(table, 1, 1, String.valueOf(newsList.size()));
        
        setTableCell(table, 2, 0, "相关新闻数");
        setTableCell(table, 2, 1, String.valueOf(relevantCount));
        
        setTableCell(table, 3, 0, "RSS来源数");
        setTableCell(table, 3, 1, String.valueOf(rssCount));
        
        setTableCell(table, 4, 0, "新闻链接数");
        setTableCell(table, 4, 1, String.valueOf(newsCount));
        
        // 添加空行
        document.createParagraph().setSpacingAfter(200);
    }
    
    /**
     * 添加导航目录
     */
    private static void addNavigation(XWPFDocument document, List<NewsExportVO> newsList) {
        // 目录标题
        XWPFParagraph navTitle = document.createParagraph();
        navTitle.setSpacingBefore(300);
        navTitle.setSpacingAfter(150);
        
        XWPFRun navTitleRun = navTitle.createRun();
        navTitleRun.setText("新闻目录");
        navTitleRun.setFontSize(16);
        navTitleRun.setBold(true);
        navTitleRun.setFontFamily("微软雅黑");
        
        // 创建目录列表（使用段落而不是表格，更紧凑）
        for (int i = 0; i < newsList.size(); i++) {
            NewsExportVO news = newsList.get(i);
            String title = getDisplayTitle(news);
            
            XWPFParagraph navItem = document.createParagraph();
            navItem.setSpacingBefore(50);
            navItem.setSpacingAfter(50);
            navItem.setIndentationLeft(100); // 缩进
            
            XWPFRun navRun = navItem.createRun();
            navRun.setText(String.format("%d. %s", i + 1, title));
            navRun.setFontSize(11);
            navRun.setFontFamily("宋体");
            
            // 添加超链接到对应的新闻项
            String bookmarkName = "news_" + (i + 1);
            CTHyperlink hyperlink = navItem.getCTP().addNewHyperlink();
            hyperlink.setAnchor(bookmarkName);
            hyperlink.setId("rId" + (i + 1000)); // 使用唯一ID
            
            // 设置超链接样式
            navRun.setColor("0000FF"); // 蓝色
            navRun.setUnderline(UnderlinePatterns.SINGLE);
        }
        
        // 添加空行
        document.createParagraph().setSpacingAfter(300);
    }
    
    /**
     * 添加新闻列表
     */
    private static void addNewsList(XWPFDocument document, List<NewsExportVO> newsList) {
        // 章节标题
        XWPFParagraph sectionTitle = document.createParagraph();
        sectionTitle.setSpacingBefore(300);
        sectionTitle.setSpacingAfter(150);
        
        XWPFRun sectionRun = sectionTitle.createRun();
        sectionRun.setText("新闻详情");
        sectionRun.setFontSize(16);
        sectionRun.setBold(true);
        sectionRun.setFontFamily("微软雅黑");
        
        // 添加新闻项（紧凑布局，不分页）
        for (int i = 0; i < newsList.size(); i++) {
            addNewsItem(document, newsList.get(i), i + 1, newsList.size());
        }
    }
    
    /**
     * 添加单个新闻项
     */
    private static void addNewsItem(XWPFDocument document, NewsExportVO news, int index, int totalCount) {
        // 添加书签用于导航
        String bookmarkName = "news_" + index;
        
        // 新闻标题（带书签）
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setSpacingBefore(200); // 进一步减少间距
        titlePara.setSpacingAfter(100);
        
        // 添加书签
        CTP ctp = titlePara.getCTP();
        CTPPr ppr = ctp.getPPr() == null ? ctp.addNewPPr() : ctp.getPPr();
        CTBookmark bookmark = ctp.addNewBookmarkStart();
        bookmark.setName(bookmarkName);
        bookmark.setId(BigInteger.valueOf(index));
        
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText(String.format("%d. %s", index, getDisplayTitle(news)));
        titleRun.setFontSize(13); // 稍微减小字体
        titleRun.setBold(true);
        titleRun.setFontFamily("微软雅黑");
        
        // 结束书签
        ctp.addNewBookmarkEnd().setId(BigInteger.valueOf(index));
        
        // 创建信息表格（2行4列）- 更紧凑
        XWPFTable infoTable = document.createTable(2, 4);
        infoTable.setWidth("100%");
        
        // 设置表格宽度
        CTTbl ctTbl = infoTable.getCTTbl();
        CTTblPr tblPr = ctTbl.getTblPr() == null ? ctTbl.addNewTblPr() : ctTbl.getTblPr();
        tblPr.addNewTblW().setW(BigInteger.valueOf(10000)); // 100%宽度
        
        // 设置表格边框（更细的线）
        CTTblBorders borders = tblPr.addNewTblBorders();
        borders.addNewTop().setVal(STBorder.SINGLE);
        borders.addNewBottom().setVal(STBorder.SINGLE);
        borders.addNewLeft().setVal(STBorder.SINGLE);
        borders.addNewRight().setVal(STBorder.SINGLE);
        borders.addNewInsideH().setVal(STBorder.SINGLE);
        borders.addNewInsideV().setVal(STBorder.SINGLE);
        
        // 第一行：发布时间和来源
        setCompactTableCell(infoTable, 0, 0, "发布时间:", true);
        setCompactTableCell(infoTable, 0, 1, formatDate(news.getPublishTime()), false);
        setCompactTableCell(infoTable, 0, 2, "来源:", true);
        setCompactTableCell(infoTable, 0, 3, news.getSourceDisplay(), false);
        
        // 第二行：链接类型和状态
        setCompactTableCell(infoTable, 1, 0, "链接类型:", true);
        setCompactTableCell(infoTable, 1, 1, news.getDisplaySourceType(), false);
        setCompactTableCell(infoTable, 1, 2, "状态:", true);
        setCompactTableCell(infoTable, 1, 3, news.getStatusText(), false);
        
        // 摘要区域（合并标题和内容）
        XWPFParagraph summaryPara = document.createParagraph();
        summaryPara.setSpacingBefore(80);
        summaryPara.setSpacingAfter(80);
        
        XWPFRun summaryRun = summaryPara.createRun();
        String summaryText = getSummaryText(news);
        summaryRun.setText("摘要: " + summaryText);
        summaryRun.setFontSize(10); // 减小字体
        
        // 链接区域（合并标题和内容）
        XWPFParagraph linkPara = document.createParagraph();
        linkPara.setSpacingAfter(150); // 进一步减少间距
        
        XWPFRun linkRun = linkPara.createRun();
        String url = news.getUrl() != null ? news.getUrl() : "无链接";
        linkRun.setText("链接: ");
        linkRun.setFontSize(10);
        
        // 添加超链接部分
        if (news.getUrl() != null && !news.getUrl().isEmpty()) {
            XWPFHyperlinkRun hyperlinkRun = linkPara.createHyperlinkRun(news.getUrl());
            hyperlinkRun.setText(url);
            hyperlinkRun.setColor("0000FF"); // 蓝色
            hyperlinkRun.setUnderline(UnderlinePatterns.SINGLE);
        } else {
            linkRun.setText("链接: " + url);
        }
        
        // 添加更细的分隔线（只在新闻之间添加，不在最后一个新闻后添加）
        if (index < totalCount) {
            XWPFParagraph separator = document.createParagraph();
            XWPFRun separatorRun = separator.createRun();
            separatorRun.setText("─".repeat(60)); // 更简洁的分隔线
            separatorRun.setFontSize(7);
            separatorRun.setColor("CCCCCC");
        }
    }
    
    /**
     * 添加导出信息
     */
    private static void addExportInfo(XWPFDocument document) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        String exportTime = sdf.format(new Date());
        
        XWPFParagraph exportPara = document.createParagraph();
        exportPara.setAlignment(ParagraphAlignment.RIGHT);
        exportPara.setSpacingBefore(200);
        
        XWPFRun exportRun = exportPara.createRun();
        exportRun.setText(String.format("导出时间: %s", exportTime));
        exportRun.setFontSize(9);
        exportRun.setColor("666666");
    }
    
    /**
     * 设置表格单元格
     */
    private static void setTableCell(XWPFTable table, int row, int col, String text) {
        setTableCell(table, row, col, text, false);
    }
    
    /**
     * 设置表格单元格（可设置加粗）
     */
    private static void setTableCell(XWPFTable table, int row, int col, String text, boolean bold) {
        XWPFTableCell cell = table.getRow(row).getCell(col);
        if (cell == null) {
            cell = table.getRow(row).addNewTableCell();
        }
        
        // 清除原有内容
        cell.removeParagraph(0);
        
        XWPFParagraph para = cell.addParagraph();
        para.setAlignment(ParagraphAlignment.LEFT);
        
        XWPFRun run = para.createRun();
        run.setText(text != null ? text : "");
        run.setFontSize(11);
        run.setFontFamily("宋体");
        if (bold) {
            run.setBold(true);
        }
        
        // 设置单元格垂直居中
        CTTc ctTc = cell.getCTTc();
        CTTcPr tcPr = ctTc.getTcPr() == null ? ctTc.addNewTcPr() : ctTc.getTcPr();
        tcPr.addNewVAlign().setVal(STVerticalJc.CENTER);
    }
    
    /**
     * 设置紧凑表格单元格（用于新闻项表格，更小字体和间距）
     */
    private static void setCompactTableCell(XWPFTable table, int row, int col, String text, boolean bold) {
        setCompactTableCell(table, row, col, text, bold, false);
    }
    
    /**
     * 设置紧凑表格单元格（重载版本，支持自动加粗判断）
     */
    private static void setCompactTableCell(XWPFTable table, int row, int col, String text) {
        // 默认不加粗
        setCompactTableCell(table, row, col, text, false, false);
    }
    
    /**
     * 设置紧凑表格单元格（完整版本）
     */
    private static void setCompactTableCell(XWPFTable table, int row, int col, String text, boolean bold, boolean isHeader) {
        XWPFTableCell cell = table.getRow(row).getCell(col);
        if (cell == null) {
            cell = table.getRow(row).addNewTableCell();
        }
        
        // 清除原有内容
        cell.removeParagraph(0);
        
        XWPFParagraph para = cell.addParagraph();
        para.setAlignment(ParagraphAlignment.LEFT);
        para.setSpacingBefore(0);
        para.setSpacingAfter(0);
        
        XWPFRun run = para.createRun();
        run.setText(text != null ? text : "");
        run.setFontSize(10); // 更小字体
        run.setFontFamily("宋体");
        if (bold) {
            run.setBold(true);
        }
        
        // 设置单元格垂直居中
        CTTc ctTc = cell.getCTTc();
        CTTcPr tcPr = ctTc.getTcPr() == null ? ctTc.addNewTcPr() : ctTc.getTcPr();
        tcPr.addNewVAlign().setVal(STVerticalJc.CENTER);
        
        // 减少单元格内边距
        CTTblWidth cellWidth = tcPr.addNewTcW();
        cellWidth.setW(BigInteger.valueOf(1000)); // 固定宽度
    }
    
    /**
     * 获取显示标题
     */
    private static String getDisplayTitle(NewsExportVO news) {
        String displayTitle = "无标题";
        if (news.getZhTitle() != null && !news.getZhTitle().isEmpty()) {
            displayTitle = news.getZhTitle();
        } else if (news.getTitle() != null && !news.getTitle().isEmpty()) {
            displayTitle = news.getTitle();
        }
        
        // 标题过长时截断
        if (displayTitle.length() > 120) {
            displayTitle = displayTitle.substring(0, 120) + "...";
        }
        return displayTitle;
    }
    
    /**
     * 获取摘要文本
     */
    private static String getSummaryText(NewsExportVO news) {
        String summary = news.getSummary();
        if (summary == null || summary.isEmpty()) {
            return "无摘要内容";
        }
        
        // 摘要过长时截断
        if (summary.length() > 500) {
            summary = summary.substring(0, 500) + "...";
        }
        return summary;
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
     * 设置页面布局
     */
    private static void setPageLayout(XWPFDocument document) {
        // 获取文档的段落属性
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        
        // 设置页面边距（减少边距以增加内容空间）
        CTPageMar pageMar = sectPr.addNewPgMar();
        pageMar.setLeft(BigInteger.valueOf(720));  // 0.5英寸
        pageMar.setRight(BigInteger.valueOf(720)); // 0.5英寸
        pageMar.setTop(BigInteger.valueOf(720));   // 0.5英寸
        pageMar.setBottom(BigInteger.valueOf(720)); // 0.5英寸
        
        // 设置页面大小（A4）
        CTPageSz pageSz = sectPr.addNewPgSz();
        pageSz.setW(BigInteger.valueOf(11906));  // A4宽度 21cm
        pageSz.setH(BigInteger.valueOf(16838));  // A4高度 29.7cm
        pageSz.setOrient(STPageOrientation.PORTRAIT); // 纵向
        
        // 设置行间距（紧凑）
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            CTP ctp = paragraph.getCTP();
            CTPPr ppr = ctp.getPPr() == null ? ctp.addNewPPr() : ctp.getPPr();
            CTSpacing spacing = ppr.getSpacing() == null ? ppr.addNewSpacing() : ppr.getSpacing();
            spacing.setLine(BigInteger.valueOf(240)); // 1.2倍行距
        }
    }
}