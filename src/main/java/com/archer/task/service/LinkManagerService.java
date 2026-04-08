package com.archer.task.service;

import com.archer.task.constant.RuleTypeEnum;

import java.util.List;

public interface LinkManagerService {

    void saveRssLink(List<String> urlList);

    void saveNewsLink(List<String> urlList,Integer minLevel);

    String extractDomain(String url);

    void saveMatchRule(List<String> ruleList, RuleTypeEnum ruleType);

}
