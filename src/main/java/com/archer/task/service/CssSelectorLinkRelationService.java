package com.archer.task.service;

import com.archer.task.entity.CssSelectorLinkRelation;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface CssSelectorLinkRelationService extends IService<CssSelectorLinkRelation> {

    List<CssSelectorLinkRelation> findByUrl(String url);

    List<CssSelectorLinkRelation> findByCssSelectorGroup(String cssSelectorGroup);

    boolean saveOrUpdateRelation(String url, String cssSelectorGroup, String dateCssSelector, String hyperLinkCssSelector, String datePattern, String enabled);

    boolean deleteByUrl(String url);

    boolean deleteByCssSelectorGroup(String cssSelectorGroup);

    List<String> findUrlsByCssSelectorGroup(String cssSelectorGroup);

    Map<String, Object> batchImport(MultipartFile file);

    byte[] downloadTemplate();
}