package com.archer.task.service.impl;

import com.archer.task.entity.CssSelectorLinkRelation;
import com.archer.task.mapper.CssSelectorLinkRelationMapper;
import com.archer.task.service.CssSelectorLinkRelationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CssSelectorLinkRelationServiceImpl extends ServiceImpl<CssSelectorLinkRelationMapper, CssSelectorLinkRelation> implements CssSelectorLinkRelationService {

    private final CssSelectorLinkRelationMapper cssSelectorLinkRelationMapper;

    @Override
    public List<CssSelectorLinkRelation> findByUrl(String url) {
        LambdaQueryWrapper<CssSelectorLinkRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CssSelectorLinkRelation::getUrl, url)
                   .eq(CssSelectorLinkRelation::getIsDelete, "N");
        return cssSelectorLinkRelationMapper.selectList(queryWrapper);
    }

    @Override
    public List<CssSelectorLinkRelation> findByCssSelector(String cssSelector) {
        LambdaQueryWrapper<CssSelectorLinkRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CssSelectorLinkRelation::getCssSelector, cssSelector)
                   .eq(CssSelectorLinkRelation::getIsDelete, "N");
        return cssSelectorLinkRelationMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateRelation(String url, String cssSelector) {
        LambdaQueryWrapper<CssSelectorLinkRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CssSelectorLinkRelation::getUrl, url)
                   .eq(CssSelectorLinkRelation::getCssSelector, cssSelector)
                   .eq(CssSelectorLinkRelation::getIsDelete, "N");

        CssSelectorLinkRelation existing = cssSelectorLinkRelationMapper.selectOne(queryWrapper);
        if (existing != null) {
            existing.setUpdateTime(new Date());
            return cssSelectorLinkRelationMapper.updateById(existing) > 0;
        }

        CssSelectorLinkRelation relation = new CssSelectorLinkRelation();
        relation.setUrl(url);
        relation.setCssSelector(cssSelector);
        relation.setIsDelete("N");
        relation.setCreateTime(new Date());
        relation.setUpdateTime(new Date());

        return cssSelectorLinkRelationMapper.insert(relation) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByUrl(String url) {
        LambdaQueryWrapper<CssSelectorLinkRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CssSelectorLinkRelation::getUrl, url)
                   .eq(CssSelectorLinkRelation::getIsDelete, "0");

        List<CssSelectorLinkRelation> relations = cssSelectorLinkRelationMapper.selectList(queryWrapper);
        if (relations.isEmpty()) {
            return true;
        }

        for (CssSelectorLinkRelation relation : relations) {
            relation.setIsDelete("1");
            relation.setUpdateTime(new Date());
            cssSelectorLinkRelationMapper.updateById(relation);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByCssSelector(String cssSelector) {
        LambdaQueryWrapper<CssSelectorLinkRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CssSelectorLinkRelation::getCssSelector, cssSelector)
                   .eq(CssSelectorLinkRelation::getIsDelete, "0");

        List<CssSelectorLinkRelation> relations = cssSelectorLinkRelationMapper.selectList(queryWrapper);
        if (relations.isEmpty()) {
            return true;
        }

        for (CssSelectorLinkRelation relation : relations) {
            relation.setIsDelete("1");
            relation.setUpdateTime(new Date());
            cssSelectorLinkRelationMapper.updateById(relation);
        }

        return true;
    }

    @Override
    public List<String> findCssSelectorsByUrl(String url) {
        List<CssSelectorLinkRelation> relations = findByUrl(url);
        return relations.stream()
                       .map(CssSelectorLinkRelation::getCssSelector)
                       .distinct()
                       .collect(Collectors.toList());
    }

    @Override
    public List<String> findUrlsByCssSelector(String cssSelector) {
        List<CssSelectorLinkRelation> relations = findByCssSelector(cssSelector);
        return relations.stream()
                       .map(CssSelectorLinkRelation::getUrl)
                       .distinct()
                       .collect(Collectors.toList());
    }
}