package com.archer.task.util;

import com.archer.task.entity.CssSelectorLinkRelation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * css选择器快速查找
 */
@Slf4j
public class CssSelectorHelper {

    public static final String[] COMMON_DATE_PATTERNS = {
            "yyyy-MM-dd",
            "yyyy年MM月dd日",
            "HH:mm:ss",
            "HH:mm",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "dd/MM/yyyy",
            "MM/dd/yyyy",
            "dd/MM/yyyy",
            "MMM d, yyyy",
            "MMMM d, yyyy",
            "hh:mm a",
            "HH:mm:ss",
            "MM/dd/yyyy hh:mm:ss a",
            "MM/dd/yyyy HH:mm:ss",
            "dd/MM/yyyy hh:mm:ss a",
            "dd/MM/yyyy HH:mm:ss"
    };

    public static List<CssSelectorLinkRelation> findCssSelectorLinkRelation(String htmlContent) throws ParseException {
        if (htmlContent == null) {
            return Collections.emptyList();
        }
        Document root = Jsoup.parse(htmlContent);
        // 先查找所有的A标签
        Elements hyperLinks = root.select("li a");
        // 每个标签 往上找五层class
        List<HyperLinkWrapper> hyperLinkWrappers = new ArrayList<>();
        for (Element hyperLink : hyperLinks) {
            HyperLinkWrapper hyperLinkWrapper = new HyperLinkWrapper();
            hyperLinkWrapper.setElement(hyperLink);
            List<String> classList = new ArrayList<>();
            Element parent = null;
            for (int i = 0; i < 5; i++) {
                parent = Objects.requireNonNullElse(parent, hyperLink).parent();
                if (parent == null) {
                    break;
                }
                String classStr = parent.attr("class");
                classList.add(classStr);
                hyperLinkWrapper.setClassList(classList);
            }
            hyperLinkWrappers.add(hyperLinkWrapper);
            if (classList.size() < 5) {
                log.info("<UNK>");
            }
        }

        // 对于多个class父类一致的 肯定就是一组类似的超链接 目标就是筛选这样的
        List<String> classRecord = new ArrayList<>();
        int linkCount = 0;
        for (int i = 0; i < 5; i++) {
            int l = i + 1;
            Map<String, List<HyperLinkWrapper>> groupMapOne = hyperLinkWrappers.stream().collect(Collectors.groupingBy(ele -> {
                if (l > ele.classList.size()) {
                    return toJSON(ele.getClassList());
                }
                return toJSON(ele.getClassList().subList(0, l));
            }));

            Optional<Map.Entry<String, List<HyperLinkWrapper>>> maxEntry = groupMapOne.entrySet()
                    .stream()
                    .max(Comparator.comparingInt(entry -> entry.getValue().size()));
            if (maxEntry.isPresent()) {
                Map.Entry<String, List<HyperLinkWrapper>> entry = maxEntry.get();
                List<String> arr = toListString(maxEntry.get().getKey());
                // 如果最后一个元素是空的 continue
                if (StringUtils.isBlank(arr.get(arr.size() - 1))) {
                    continue;
                }
                if (entry.getValue().size() > linkCount) {
                    linkCount = entry.getValue().size();
                } else {
                    // 出现更小的情况了
                    classRecord = arr;
                    break;
                }
            }

        }
        String groupSelector = toClassString(classRecord);
        if (StringUtils.isBlank(groupSelector)) {
            return Collections.emptyList();
        }
        Elements es = root.select(groupSelector);
        Element groupSelectorEle = es.last();
        CssSelectorLinkRelation relation = new CssSelectorLinkRelation();
        Element parent = null;
        if (groupSelectorEle == null) {
            return Collections.emptyList();
        }
        for (int i = 0; i < 5; i++) {
            parent = Objects.requireNonNullElse(parent, groupSelectorEle).parent();
            if (parent == null) {
                break;
            }
            Element ele = parent.select("*:matchesOwn(^.*2026.*$)").last();
            if (ele != null) {

                for (String commonDatePattern : COMMON_DATE_PATTERNS) {
                    try {
                        DateUtils.parseDate(ele.text(), Locale.ENGLISH, commonDatePattern);
                        relation.setDatePattern(commonDatePattern);
                        break;
                    } catch (Exception e) {

                    }

                }
                // 转换成功了 记录数据 break
                if (classRecord.size() < i) {
                    break;
                }
                relation.setCssSelectorGroup(toClassString(classRecord.subList(0, classRecord.size() - i)));
                relation.setHyperLinkCssSelector("a[href]");
                if (ele.className() == null) {
                    relation.setDateCssSelector(ele.tagName());
                } else {
                    relation.setDateCssSelector("." + ele.className() + " " + ele.tagName());
                }
                break;


            }
            if (ele != null && relation.getDatePattern() == null) {
                log.error("日期转换失败:{}", ele.text());
            }

        }
        // 行 可以找文本了
        return Collections.singletonList(relation);
    }

    public static String toJSON(Object o) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toClassString(List<String> classRecord) {
        ArrayList<String> list = new ArrayList<>(classRecord);
        Collections.reverse(list);
        StringBuilder groupSelectorSb = new StringBuilder();
        for (String cls : list) {
            if (StringUtils.isNotBlank(cls) && !cls.contains(":") && !cls.contains("_")) {
                groupSelectorSb.append(".").append(cls.trim()).append(" ");
            }
        }
        return groupSelectorSb.toString().trim();
    }

    public static List<String> toListString(String str) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<String> result = mapper.readValue(str, ArrayList.class);
            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    static class HyperLinkWrapper {

        private Element element;

        private List<String> classList;

    }

}
