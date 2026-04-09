package com.archer.task.service;

import com.archer.task.entity.CssSelectorLinkRelation;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CssSelectorLinkRelationService extends IService<CssSelectorLinkRelation> {

    List<CssSelectorLinkRelation> findByUrl(String url);

    List<CssSelectorLinkRelation> findByCssSelector(String cssSelector);

    boolean saveOrUpdateRelation(String url, String cssSelector);

    boolean deleteByUrl(String url);

    boolean deleteByCssSelector(String cssSelector);

    List<String> findCssSelectorsByUrl(String url);

    List<String> findUrlsByCssSelector(String cssSelector);
}