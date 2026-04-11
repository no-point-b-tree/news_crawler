package com.archer.task.service.impl;

import com.archer.task.constant.RuleTypeEnum;
import com.archer.task.entity.MatchRule;
import com.archer.task.entity.NewsLink;
import com.archer.task.entity.RssLink;
import com.archer.task.mapper.MatchRuleMapper;
import com.archer.task.mapper.NewsLinkMapper;
import com.archer.task.mapper.RssLinkMapper;
import com.archer.task.service.LinkManagerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

@Service
public class LinkManagerServiceImpl implements LinkManagerService {

    @Resource
    private RssLinkMapper rssLinkMapper;

    @Resource
    private NewsLinkMapper newsLinkMapper;

    @Resource
    private MatchRuleMapper matchRuleMapper;

    @Override
    @Transactional
    public void saveRssLink(List<String> urlList) {
        for (String url : urlList) {
            RssLink rssLink = new RssLink();
            rssLink.setCreateTime(new Date());
            rssLink.setUpdateTime(new Date());
            rssLink.setIsDelete("N");
            rssLink.setUrl(url);
            rssLink.setDomain(extractDomain(url));
            rssLinkMapper.insert(rssLink);
        }
    }

    @Override
    @Transactional
    public void saveNewsLink(List<String> urlList, Integer minLevel) {
        for (String url : urlList) {
            NewsLink newsLink = new NewsLink();
            newsLink.setCreateTime(new Date());
            newsLink.setUpdateTime(new Date());
            newsLink.setIsDelete("N");
            newsLink.setUrl(url);
            newsLink.setDomain(extractDomain(url));
            newsLinkMapper.insert(newsLink);
        }
    }

    @Override
    public String extractDomain(String urlStr) {
        if (urlStr == null || urlStr.isBlank()) {
            return null;
        }
        try {
            URI uri = new URI(urlStr);
            String host = uri.getHost();
            // 如果 host 为 null（例如相对路径或协议缺失），尝试补全协议后再次解析
            if (host == null && !urlStr.startsWith("http://") && !urlStr.startsWith("https://")) {
                uri = new URI("http://" + urlStr);
                host = uri.getHost();
            }
            return host;
        } catch (URISyntaxException e) {
            // 可以根据实际需求记录日志
            return null;
        }
    }

    @Override
    @Transactional
    public void saveMatchRule(List<String> ruleList, RuleTypeEnum ruleType) {
        for (String rule : ruleList) {
            MatchRule matchRule = new MatchRule();
            matchRule.setCreateTime(new Date());
            matchRule.setUpdateTime(new Date());
            matchRule.setIsDelete("N");
            matchRule.setRule(rule);
            matchRule.setRuleType(ruleType.name());
            matchRuleMapper.insert(matchRule);
        }
    }
}
