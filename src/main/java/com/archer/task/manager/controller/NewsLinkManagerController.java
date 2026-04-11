package com.archer.task.manager.controller;

import com.archer.task.entity.CssSelectorLinkRelation;
import com.archer.task.entity.NewsLink;
import com.archer.task.entity.TaskLog;
import com.archer.task.manager.dto.CssSelectorPageByUrlRequest;
import com.archer.task.manager.dto.CssSelectorRequest;
import com.archer.task.manager.dto.CssSelectorRelationRequest;
import com.archer.task.manager.dto.NewsLinkPageRequest;
import com.archer.task.manager.dto.PageRequest;
import com.archer.task.manager.dto.TaskLogPageByUrlRequest;
import com.archer.task.manager.dto.UrlRequest;
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

    @PostMapping("/page-with-css-count")
    public ResponseEntity<Page<com.archer.task.manager.vo.NewsLinkWithCssCountVO>> pageNewsLinksWithCssCount(@RequestBody NewsLinkPageRequest request) {
        Page<com.archer.task.manager.vo.NewsLinkWithCssCountVO> result = newsLinkManagerService.pageNewsLinksWithCssCount(
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

    @PostMapping
    public ResponseEntity<Boolean> createNewsLink(@RequestBody NewsLink newsLink) {
        boolean result = newsLinkManagerService.createNewsLink(newsLink);
        return ResponseEntity.ok(result);
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

    @PostMapping("/start-task")
    public ResponseEntity<Boolean> startSingleTask(@RequestBody UrlRequest request) {
        boolean result = newsLinkManagerService.startSingleTask(
            request.getUrl(), 
            request.getStartDate(), 
            request.getEndDate()
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/task-logs/page")
    public ResponseEntity<Page<TaskLog>> pageTaskLogsByUrl(@RequestBody TaskLogPageByUrlRequest request) {
        Page<TaskLog> result = newsLinkManagerService.pageTaskLogsByUrl(request.getUrl(), request.getPage(), request.getSize());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/css-selectors/page")
    public ResponseEntity<Page<CssSelectorLinkRelation>> pageCssSelectorsByUrl(@RequestBody CssSelectorPageByUrlRequest request) {
        Page<CssSelectorLinkRelation> result = newsLinkManagerService.pageCssSelectorsByUrl(request.getUrl(), request.getPage(), request.getSize());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/css-selectors")
    public ResponseEntity<Boolean> addCssSelector(@RequestBody CssSelectorRelationRequest request) {
        boolean result = newsLinkManagerService.addCssSelector(
            request.getUrl(), 
            request.getCssSelectorGroup(),
            request.getDateCssSelector(),
            request.getHyperLinkCssSelector(),
            request.getDatePattern(),
            request.getEnabled()
        );
        return ResponseEntity.ok(result);
    }

    @PutMapping("/css-selectors/{id}")
    public ResponseEntity<Boolean> updateCssSelector(@PathVariable Long id, @RequestBody CssSelectorRelationRequest request) {
        boolean result = newsLinkManagerService.updateCssSelector(
            id,
            request.getCssSelectorGroup(),
            request.getDateCssSelector(),
            request.getHyperLinkCssSelector(),
            request.getDatePattern(),
            request.getEnabled()
        );
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/css-selectors/{id}")
    public ResponseEntity<Boolean> deleteCssSelector(@PathVariable Long id) {
        boolean result = newsLinkManagerService.deleteCssSelector(id);
        return ResponseEntity.ok(result);
    }
}