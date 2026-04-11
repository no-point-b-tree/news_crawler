package com.archer.task.service;

import com.archer.task.entity.CssSelectorLinkRelation;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.archer.task.entity.Hyperlink;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

@Slf4j
public class JsoupExtractor {

    static List<String> MATCH_RULE_LIST = Arrays.asList("\\d+小时前", "\\d+分钟前", "\\d+\\s+mins?\\s+ago", "\\d+\\s+hours?\\s+ago");

    public static List<Hyperlink> extractor(String htmlContent, List<CssSelectorLinkRelation> cssSelectorList) {
        if (htmlContent == null) {
            return Collections.emptyList();
        }
        Document doc = Jsoup.parse(htmlContent, "UTF-8");
        // 找出所有文本内容为日期的元素
        Set<String> hrefSet = new HashSet<>();
        List<Hyperlink> hyperlinkListAll = new ArrayList<>();
        if (cssSelectorList != null && !cssSelectorList.isEmpty()) {
            for (CssSelectorLinkRelation cssSelectorLinkRelation : cssSelectorList) {
                String groupSelector = cssSelectorLinkRelation.getCssSelectorGroup();
                Elements groupSelectorElements = doc.select(groupSelector);
                for (Element itemElement : groupSelectorElements) {
                    // 每个卡片的元素 查询一个卡片下的超链接和日期
                    Element link = itemElement.select(cssSelectorLinkRelation.getHyperLinkCssSelector()).first();
                    Element date = itemElement.select(cssSelectorLinkRelation.getDateCssSelector()).first();
                    if (link != null && date != null) {
                        String href = link.attr("href");
                        if (hrefSet.add(href)) {
                            String text = link.text();
                            Hyperlink hyperlink = new Hyperlink();
                            hyperlink.setHref(href);
                            hyperlink.setContent(text);
                            try {
                                // 如果自定义规则解析失败或没有自定义规则，尝试通用规则
                                if (hyperlink.getDate() == null) {
                                    for (String rule : MATCH_RULE_LIST) {
                                        if (date.text().matches(rule)) {
                                            hyperlink.setDate(new Date());
                                            break;
                                        }
                                    }
                                }
                                // 首先检查是否有自定义的日期匹配规则
                                String datePattern = cssSelectorLinkRelation.getDatePattern();
                                if (datePattern != null && !datePattern.trim().isEmpty()) {
                                    try {
                                        Date publishDate = DateUtils.parseDate(date.text().trim(), datePattern);
                                        hyperlink.setDate(publishDate);
                                    } catch (Exception e) {
                                        log.warn("使用自定义日期模式解析失败: {}, pattern: {}", date.text(), datePattern);
                                    }
                                }

                            } catch (Exception e) {
                                log.error("日期转换失败:", e);
                            }
                            hyperlinkListAll.add(hyperlink);
                        }
                    }

                }
            }
        }

        return hyperlinkListAll;
    }

}
