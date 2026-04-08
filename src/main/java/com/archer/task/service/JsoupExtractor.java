package com.archer.task.service;

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


    public static List<Hyperlink> extractor(String htmlContent, List<String> patternList, Integer minLevel) {
        if (htmlContent == null) {
            return Collections.emptyList();
        }
        Document doc = Jsoup.parse(htmlContent, "UTF-8");
        // 找出所有文本内容为日期的元素
        List<Hyperlink> hyperlinkList = new ArrayList<>();
        for (String pattern : patternList) {
            Elements dateElements = doc.select("*:matches(^" + pattern + "$)");
            for (Element dateElem : dateElements) {
                // 向上找5层父节点
                Element parent = dateElem.parent();
                int level = 1;
                Set<String> hrefSet = new HashSet<>();
                while (parent != null && level <= 5) {
                    // 在当前父节点下找所有超链接 包括后代
                    Elements links = parent.select("a");
                    for (Element link : links) {
                        String href = link.attr("href");
                        if (hrefSet.add(href)) {
                            if (minLevel != null && minLevel > level) {
                                continue;
                            }
                            String text = link.text();
                            Hyperlink hyperlink = new Hyperlink();
                            hyperlink.setHref(href);
                            hyperlink.setContent(text);
                            hyperlink.setPattern(pattern);
                            hyperlinkList.add(hyperlink);
                        }
                    }
                    if (!hyperlinkList.isEmpty()) {
                        log.info("层级:{}", level);
                        break;
                    }
                    // 继续向上
                    parent = parent.parent();
                    level++;
                }
            }
        }
        return hyperlinkList;
    }
}
