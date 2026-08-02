package com.archer.task.service.impl;

import com.archer.task.entity.CssSelectorLinkRelation;
import com.archer.task.mapper.CssSelectorLinkRelationMapper;
import com.archer.task.service.CssSelectorLinkRelationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.*;
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
                .eq(CssSelectorLinkRelation::getIsDelete, "N").eq(CssSelectorLinkRelation::getEnabled, "ON");
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
    public boolean saveOrUpdateRelation(String url, String cssSelectorGroup, String dateCssSelector, String hyperLinkCssSelector, String datePattern, String enabled) {
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
            existing.setDatePattern(datePattern);
            existing.setEnabled(enabled);
            existing.setUpdateTime(new Date());
            return cssSelectorLinkRelationMapper.updateById(existing) > 0;
        }

        CssSelectorLinkRelation relation = new CssSelectorLinkRelation();
        relation.setUrl(url);
        relation.setCssSelectorGroup(cssSelectorGroup);
        relation.setDateCssSelector(dateCssSelector);
        relation.setHyperLinkCssSelector(hyperLinkCssSelector);
        relation.setDatePattern(datePattern);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchImport(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int skipCount = 0;
        List<String> errors = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String url = getCellValue(row, 0, formatter);
                String cssSelectorGroup = getCellValue(row, 1, formatter);
                String dateCssSelector = getCellValue(row, 2, formatter);
                String hyperLinkCssSelector = getCellValue(row, 3, formatter);
                String datePattern = getCellValue(row, 4, formatter);
                String enabled = getCellValue(row, 5, formatter);

                if (url == null || url.isBlank()) {
                    skipCount++;
                    continue;
                }
                url = url.trim();
                cssSelectorGroup = cssSelectorGroup != null ? cssSelectorGroup.trim() : "";
                dateCssSelector = dateCssSelector != null ? dateCssSelector.trim() : null;
                hyperLinkCssSelector = hyperLinkCssSelector != null ? hyperLinkCssSelector.trim() : null;
                datePattern = datePattern != null ? datePattern.trim() : null;
                enabled = (enabled != null && !enabled.isBlank()) ? enabled.trim() : "ON";

                try {
                    CssSelectorLinkRelation existing = null;
                    if (!cssSelectorGroup.isEmpty()) {
                        LambdaQueryWrapper<CssSelectorLinkRelation> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(CssSelectorLinkRelation::getUrl, url)
                                .eq(CssSelectorLinkRelation::getCssSelectorGroup, cssSelectorGroup)
                                .eq(CssSelectorLinkRelation::getIsDelete, "N");
                        existing = cssSelectorLinkRelationMapper.selectOne(queryWrapper);
                    }

                    if (existing != null) {
                        existing.setDateCssSelector(dateCssSelector);
                        existing.setHyperLinkCssSelector(hyperLinkCssSelector);
                        existing.setDatePattern(datePattern);
                        existing.setEnabled(enabled);
                        existing.setUpdateTime(new Date());
                        cssSelectorLinkRelationMapper.updateById(existing);
                    } else {
                        CssSelectorLinkRelation relation = new CssSelectorLinkRelation();
                        relation.setUrl(url);
                        relation.setCssSelectorGroup(cssSelectorGroup);
                        relation.setDateCssSelector(dateCssSelector);
                        relation.setHyperLinkCssSelector(hyperLinkCssSelector);
                        relation.setDatePattern(datePattern);
                        relation.setEnabled(enabled);
                        relation.setIsDelete("N");
                        relation.setCreateTime(new Date());
                        relation.setUpdateTime(new Date());
                        cssSelectorLinkRelationMapper.insert(relation);
                    }
                    successCount++;
                } catch (Exception e) {
                    errors.add("第" + (i + 1) + "行: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("CSS选择器批量导入失败", e);
            result.put("success", false);
            result.put("message", "文件解析失败: " + e.getMessage());
            return result;
        }

        result.put("success", true);
        result.put("successCount", successCount);
        result.put("skipCount", skipCount);
        result.put("errors", errors);
        return result;
    }

    @Override
    public byte[] downloadTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("CSS选择器");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = {"URL（必填）", "CSS选择器组", "日期选择器", "超链接选择器", "日期匹配规则", "启用状态（ON/OFF）"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("生成CSS选择器模板失败", e);
            return new byte[0];
        }
    }

    private String getCellValue(Row row, int colIndex, DataFormatter formatter) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        String value = formatter.formatCellValue(cell);
        return value != null ? value.trim() : null;
    }
}