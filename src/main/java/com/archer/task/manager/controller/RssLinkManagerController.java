package com.archer.task.manager.controller;

import com.archer.task.entity.RssLink;
import com.archer.task.manager.dto.PageRequest;
import com.archer.task.manager.dto.RssLinkPageRequest;
import com.archer.task.manager.dto.UrlRequest;
import com.archer.task.manager.service.RssLinkManagerService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager/rss-links")
@RequiredArgsConstructor
public class RssLinkManagerController {

    private final RssLinkManagerService rssLinkManagerService;

    @PostMapping("/page")
    public ResponseEntity<Page<RssLink>> pageRssLinks(@RequestBody RssLinkPageRequest request) {
        Page<RssLink> result = rssLinkManagerService.pageRssLinks(
            request.getKeyword(), 
            request.getPage(), 
            request.getSize()
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Boolean> createRssLink(@RequestBody RssLink rssLink) {
        boolean result = rssLinkManagerService.createRssLink(rssLink);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateRssLink(@PathVariable Long id, @RequestBody RssLink rssLink) {
        rssLink.setId(id);
        boolean result = rssLinkManagerService.updateRssLink(rssLink);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteRssLink(@PathVariable Long id) {
        boolean result = rssLinkManagerService.deleteRssLink(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/start-task")
    public ResponseEntity<Boolean> startRssTask(@RequestBody UrlRequest request) {
        boolean result = rssLinkManagerService.startRssTask(
            request.getUrl(), 
            request.getStartDate(), 
            request.getEndDate()
        );
        return ResponseEntity.ok(result);
    }
}