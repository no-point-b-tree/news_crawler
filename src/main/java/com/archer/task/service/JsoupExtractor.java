package com.archer.task.service;

import com.archer.task.entity.CssSelectorLinkRelation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.archer.task.entity.Hyperlink;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Slf4j
public class JsoupExtractor {

    public static List<String> MATCH_RULE_LIST = Arrays.asList("\\d+小时前", "\\d+分钟前", "\\d+\\s+mins?\\s+ago", "\\d+\\s+hours?\\s+ago", "\\d+\\s+hrs?\\s+ago");

    public static final boolean LOOK_ALL_LINKS = false;

    public static List<Hyperlink> extractor(String htmlContent, List<CssSelectorLinkRelation> cssSelectorList, Locale locale) {
        if (htmlContent == null) {
            return Collections.emptyList();
        }
        Document doc = Jsoup.parse(htmlContent, "UTF-8");
        // 找出所有文本内容为日期的元素
        Set<String> hrefSet = new HashSet<>();
        List<Hyperlink> hyperlinkListAll = new ArrayList<>();
        if (LOOK_ALL_LINKS) {
            Elements elements = doc.select("a[href]");
            for (Element itemElement : elements) {
                String href = itemElement.attr("href");
                if (hrefSet.add(href)) {
                    String text = itemElement.text();
                    Hyperlink hyperlink = new Hyperlink();
                    hyperlink.setHref(href);
                    hyperlink.setContent(text);
                    hyperlinkListAll.add(hyperlink);
                }
            }
            return hyperlinkListAll;
        }
        if (cssSelectorList != null && !cssSelectorList.isEmpty()) {
            for (CssSelectorLinkRelation cssSelectorLinkRelation : cssSelectorList) {
                String groupSelector = cssSelectorLinkRelation.getCssSelectorGroup();
                Elements groupSelectorElements = doc.select(groupSelector);
                for (Element itemElement : groupSelectorElements) {
                    // 每个卡片的元素 查询一个卡片下的超链接和日期
                    Element link = itemElement.select(cssSelectorLinkRelation.getHyperLinkCssSelector()).first();
                    Elements date = null;
                    if (StringUtils.isNotBlank(cssSelectorLinkRelation.getDateCssSelector())) {
                        // 没有日期选择器 说明全选
                        date = itemElement.select(cssSelectorLinkRelation.getDateCssSelector());
                    }
                    if (link != null) {
                        String href = link.attr("href");
                        if (!hrefSet.contains(href)) {
                            String text = link.text();
                            Hyperlink hyperlink = new Hyperlink();
                            hyperlink.setHref(href);
                            hyperlink.setContent(text);
                            try {
                                // 如果自定义规则解析失败或没有自定义规则，尝试通用规则
                                String dateStr = getDateStr(date);
                                if (hyperlink.getDate() == null) {
                                    for (String rule : MATCH_RULE_LIST) {
                                        if (dateStr.matches(rule)) {
                                            Date now = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
                                            hyperlink.setDate(now);
                                            break;
                                        }
                                    }
                                }
                                // 首先检查是否有自定义的日期匹配规则
                                String datePattern = cssSelectorLinkRelation.getDatePattern();
                                if (datePattern != null && !datePattern.trim().isEmpty() && hyperlink.getDate() == null) {
                                    try {
                                        Date publishDate = DateUtils.parseDate(dateStr, locale, datePattern);
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTime(publishDate);
                                        int year = calendar.get(Calendar.YEAR);
                                        if (year == 1970) {
                                            calendar.set(Calendar.YEAR, LocalDate.now().getYear());
                                        }
                                        publishDate = calendar.getTime();
                                        hyperlink.setDate(publishDate);
                                    } catch (Exception e) {
                                        log.warn("使用自定义日期模式解析失败: {}, pattern: {}", dateStr, datePattern);
                                    }
                                }

                            } catch (Exception e) {
                                log.error("日期转换失败:", e);
                            }
                            if (hyperlink.getDate() != null) {
                                hrefSet.add(hyperlink.getHref());
                                hyperlinkListAll.add(hyperlink);
                            }
                        }
                    }

                }
            }
        }
        log.info("查找到超链接数量: {}", hyperlinkListAll.size());
        return hyperlinkListAll;
    }

    public static boolean isValidDate(Date startDate, Date endDate, Date publishDate) {
        if (JsoupExtractor.LOOK_ALL_LINKS) {
            return true;
        }
        if (endDate == null) {
            endDate = new Date();
        }
        if (startDate == null) {
            startDate = DateUtils.addDays(endDate, -1);
        }
        if (publishDate == null) {
            publishDate = new Date();
        }
        return publishDate.compareTo(startDate) >= 0 && publishDate.compareTo(endDate) <= 0;
    }

    public static String getDateStr(Elements elements) {
        if (elements == null) {
            return "1 hour ago";
        }
        StringBuilder builder = new StringBuilder();
        for (Element element : elements) {
            builder.append(element.ownText().trim()).append(" ");
        }
        return builder.toString().trim();
    }

}
