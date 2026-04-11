package com.archer.task.manager.controller;

import com.archer.task.entity.CssSelectorLinkRelation;
import com.archer.task.manager.dto.CssSelectorRelationRequest;
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

    @GetMapping("/by-css-selector-group")
    public ResponseEntity<?> findByCssSelectorGroup(@RequestParam String cssSelectorGroup) {
        return ResponseEntity.ok(cssSelectorLinkRelationService.findByCssSelectorGroup(cssSelectorGroup));
    }

    @PostMapping
    public ResponseEntity<Boolean> saveOrUpdateRelation(@RequestBody CssSelectorRelationRequest request) {
        boolean result = cssSelectorLinkRelationService.saveOrUpdateRelation(
            request.getUrl(),
            request.getCssSelectorGroup(),
            request.getDateCssSelector(),
            request.getHyperLinkCssSelector(),
            request.getEnabled()
        );
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/by-url")
    public ResponseEntity<Boolean> deleteByUrl(@RequestParam String url) {
        boolean result = cssSelectorLinkRelationService.deleteByUrl(url);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/by-css-selector-group")
    public ResponseEntity<Boolean> deleteByCssSelectorGroup(@RequestParam String cssSelectorGroup) {
        boolean result = cssSelectorLinkRelationService.deleteByCssSelectorGroup(cssSelectorGroup);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/urls/by-css-selector-group")
    public ResponseEntity<?> findUrlsByCssSelectorGroup(@RequestParam String cssSelectorGroup) {
        return ResponseEntity.ok(cssSelectorLinkRelationService.findUrlsByCssSelectorGroup(cssSelectorGroup));
    }
}