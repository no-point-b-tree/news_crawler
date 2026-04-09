package com.archer.task.manager.controller;

import com.archer.task.entity.CssSelectorLinkRelation;
import com.archer.task.entity.NewsLink;
import com.archer.task.entity.TaskLog;
import com.archer.task.manager.dto.NewsLinkPageRequest;
import com.archer.task.manager.service.NewsLinkManagerService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager/news-links")
@RequiredArgsConstructor
public class NewsLinkManagerController {

    private final NewsLinkManagerService newsLinkManagerService;

    @PostMapping("/page")
    public ResponseEntity<Page<NewsLink>> pageNewsLinks(@RequestBody NewsLinkPageRequest request) {
        Page<NewsLink> result = newsLinkManagerService.pageNewsLinks(
            request.getKeyword(), 
            request.getLangs(), 
            request.getPage(), 
            request.getSize()
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/languages")
    public ResponseEntity<List<String>> getAllLanguages() {
        List<String> languages = newsLinkManagerService.getAllLanguages();
        return ResponseEntity.ok(languages);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateNewsLink(@PathVariable Long id, @RequestBody NewsLink newsLink) {
        newsLink.setId(id);
        boolean result = newsLinkManagerService.updateNewsLink(newsLink);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteNewsLink(@PathVariable Long id) {
        boolean result = newsLinkManagerService.deleteNewsLink(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{url}/start-task")
    public ResponseEntity<Boolean> startSingleTask(@PathVariable String url) {
        boolean result = newsLinkManagerService.startSingleTask(url);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{url}/task-logs")
    public ResponseEntity<Page<TaskLog>> pageTaskLogsByUrl(
            @PathVariable String url,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<TaskLog> result = newsLinkManagerService.pageTaskLogsByUrl(url, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{url}/css-selectors")
    public ResponseEntity<Page<CssSelectorLinkRelation>> pageCssSelectorsByUrl(
            @PathVariable String url,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<CssSelectorLinkRelation> result = newsLinkManagerService.pageCssSelectorsByUrl(url, page, size);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{url}/css-selectors")
    public ResponseEntity<Boolean> addCssSelector(@PathVariable String url, @RequestParam String cssSelector) {
        boolean result = newsLinkManagerService.addCssSelector(url, cssSelector);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/css-selectors/{id}")
    public ResponseEntity<Boolean> updateCssSelector(@PathVariable Long id, @RequestParam String cssSelector) {
        boolean result = newsLinkManagerService.updateCssSelector(id, cssSelector);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/css-selectors/{id}")
    public ResponseEntity<Boolean> deleteCssSelector(@PathVariable Long id) {
        boolean result = newsLinkManagerService.deleteCssSelector(id);
        return ResponseEntity.ok(result);
    }
}