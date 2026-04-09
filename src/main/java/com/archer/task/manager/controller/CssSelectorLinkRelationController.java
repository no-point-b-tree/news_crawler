package com.archer.task.manager.controller;

import com.archer.task.entity.CssSelectorLinkRelation;
import com.archer.task.service.CssSelectorLinkRelationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager/css-selector-relations")
@RequiredArgsConstructor
public class CssSelectorLinkRelationController {

    private final CssSelectorLinkRelationService cssSelectorLinkRelationService;

    @GetMapping("/page")
    public ResponseEntity<Page<CssSelectorLinkRelation>> pageCssSelectors(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<CssSelectorLinkRelation> pageParam = new Page<>(page, size);
        Page<CssSelectorLinkRelation> result = cssSelectorLinkRelationService.page(pageParam);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-url")
    public ResponseEntity<?> findByUrl(@RequestParam String url) {
        return ResponseEntity.ok(cssSelectorLinkRelationService.findByUrl(url));
    }

    @GetMapping("/by-css-selector")
    public ResponseEntity<?> findByCssSelector(@RequestParam String cssSelector) {
        return ResponseEntity.ok(cssSelectorLinkRelationService.findByCssSelector(cssSelector));
    }

    @PostMapping
    public ResponseEntity<Boolean> saveOrUpdateRelation(
            @RequestParam String url,
            @RequestParam String cssSelector) {
        boolean result = cssSelectorLinkRelationService.saveOrUpdateRelation(url, cssSelector);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/by-url")
    public ResponseEntity<Boolean> deleteByUrl(@RequestParam String url) {
        boolean result = cssSelectorLinkRelationService.deleteByUrl(url);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/by-css-selector")
    public ResponseEntity<Boolean> deleteByCssSelector(@RequestParam String cssSelector) {
        boolean result = cssSelectorLinkRelationService.deleteByCssSelector(cssSelector);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/css-selectors/by-url")
    public ResponseEntity<?> findCssSelectorsByUrl(@RequestParam String url) {
        return ResponseEntity.ok(cssSelectorLinkRelationService.findCssSelectorsByUrl(url));
    }

    @GetMapping("/urls/by-css-selector")
    public ResponseEntity<?> findUrlsByCssSelector(@RequestParam String cssSelector) {
        return ResponseEntity.ok(cssSelectorLinkRelationService.findUrlsByCssSelector(cssSelector));
    }
}