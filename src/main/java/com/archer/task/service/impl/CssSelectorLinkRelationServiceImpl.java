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
import org.springframework.util.StringUtils;

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
    public List<CssSelectorLinkRelation> findByCssSelectorGroup(String cssSelectorGroup) {
        LambdaQueryWrapper<CssSelectorLinkRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CssSelectorLinkRelation::getCssSelectorGroup, cssSelectorGroup)
                   .eq(CssSelectorLinkRelation::getIsDelete, "N");
        return cssSelectorLinkRelationMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateRelation(String url, String cssSelectorGroup, String dateCssSelector, String hyperLinkCssSelector, String enabled) {
        if (!StringUtils.hasText(url) || !StringUtils.hasText(cssSelectorGroup)) {
            return false;
        }

        LambdaQueryWrapper<CssSelectorLinkRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CssSelectorLinkRelation::getUrl, url)
                   .eq(CssSelectorLinkRelation::getCssSelectorGroup, cssSelectorGroup)
                   .eq(CssSelectorLinkRelation::getIsDelete, "N");

        CssSelectorLinkRelation existing = cssSelectorLinkRelationMapper.selectOne(queryWrapper);
        if (existing != null) {
            existing.setDateCssSelector(dateCssSelector);
            existing.setHyperLinkCssSelector(hyperLinkCssSelector);
            existing.setEnabled(enabled);
            existing.setUpdateTime(new Date());
            return cssSelectorLinkRelationMapper.updateById(existing) > 0;
        }

        CssSelectorLinkRelation relation = new CssSelectorLinkRelation();
        relation.setUrl(url);
        relation.setCssSelectorGroup(cssSelectorGroup);
        relation.setDateCssSelector(dateCssSelector);
        relation.setHyperLinkCssSelector(hyperLinkCssSelector);
        relation.setEnabled(StringUtils.hasText(enabled) ? enabled : "ON");
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
                   .eq(CssSelectorLinkRelation::getIsDelete, "N");

        List<CssSelectorLinkRelation> relations = cssSelectorLinkRelationMapper.selectList(queryWrapper);
        if (relations.isEmpty()) {
            return true;
        }

        for (CssSelectorLinkRelation relation : relations) {
            relation.setIsDelete("Y");
            relation.setUpdateTime(new Date());
            cssSelectorLinkRelationMapper.updateById(relation);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByCssSelectorGroup(String cssSelectorGroup) {
        LambdaQueryWrapper<CssSelectorLinkRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CssSelectorLinkRelation::getCssSelectorGroup, cssSelectorGroup)
                   .eq(CssSelectorLinkRelation::getIsDelete, "N");

        List<CssSelectorLinkRelation> relations = cssSelectorLinkRelationMapper.selectList(queryWrapper);
        if (relations.isEmpty()) {
            return true;
        }

        for (CssSelectorLinkRelation relation : relations) {
            relation.setIsDelete("Y");
            relation.setUpdateTime(new Date());
            cssSelectorLinkRelationMapper.updateById(relation);
        }

        return true;
    }

    @Override
    public List<String> findUrlsByCssSelectorGroup(String cssSelectorGroup) {
        List<CssSelectorLinkRelation> relations = findByCssSelectorGroup(cssSelectorGroup);
        return relations.stream()
                       .map(CssSelectorLinkRelation::getUrl)
                       .filter(StringUtils::hasText)
                       .distinct()
                       .collect(Collectors.toList());
    }
}