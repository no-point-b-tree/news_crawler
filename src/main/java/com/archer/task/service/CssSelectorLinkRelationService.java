package com.archer.task.service;

import com.archer.task.entity.CssSelectorLinkRelation;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CssSelectorLinkRelationService extends IService<CssSelectorLinkRelation> {

    List<CssSelectorLinkRelation> findByUrl(String url);

    List<CssSelectorLinkRelation> findByCssSelectorGroup(String cssSelectorGroup);

    boolean saveOrUpdateRelation(String url, String cssSelectorGroup, String dateCssSelector, String hyperLinkCssSelector, String datePattern, String enabled);

    boolean deleteByUrl(String url);

    boolean deleteByCssSelectorGroup(String cssSelectorGroup);

    List<String> findUrlsByCssSelectorGroup(String cssSelectorGroup);
}