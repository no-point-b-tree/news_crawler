package com.archer.task.manager.service;

import com.archer.task.entity.RssLink;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface RssLinkManagerService extends IService<RssLink> {

    Page<RssLink> pageRssLinks(String keyword, Integer page, Integer size);

    boolean createRssLink(RssLink rssLink);

    boolean updateRssLink(RssLink rssLink);

    boolean deleteRssLink(Long id);

    boolean startRssTask(String url);
}