package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.entity.InspectionTemplate;
import com.roominspection.backend.entity.InspectionTemplateItem;
import com.roominspection.backend.mapper.InspectionTemplateItemMapper;
import com.roominspection.backend.mapper.InspectionTemplateMapper;
import com.roominspection.backend.service.InspectionTemplateService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 巡检模板服务实现类
 */
@Service
public class InspectionTemplateServiceImpl implements InspectionTemplateService {

    @Autowired
    private InspectionTemplateMapper inspectionTemplateMapper;

    @Autowired
    private InspectionTemplateItemMapper inspectionTemplateItemMapper;

    @Override
    public Page<InspectionTemplate> listTemplates(Integer page, Integer size, String templateName, String deviceType, String status) {
        Page<InspectionTemplate> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<InspectionTemplate> queryWrapper = new LambdaQueryWrapper<>();

        if (templateName != null && !templateName.isEmpty()) {
            queryWrapper.like(InspectionTemplate::getTemplateName, templateName);
        }

        if (deviceType != null && !deviceType.isEmpty()) {
            queryWrapper.eq(InspectionTemplate::getDeviceType, deviceType);
        }

        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(InspectionTemplate::getStatus, status);
        }

        queryWrapper.orderByDesc(InspectionTemplate::getCreateTime);
        return inspectionTemplateMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public InspectionTemplate getTemplateById(Long id) {
        return inspectionTemplateMapper.selectById(id);
    }

    @Override
    public InspectionTemplate getTemplateByCode(String templateCode) {
        LambdaQueryWrapper<InspectionTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InspectionTemplate::getTemplateCode, templateCode);
        return inspectionTemplateMapper.selectOne(queryWrapper);
    }

    @Override
    public List<InspectionTemplateItem> getTemplateItems(Long templateId) {
        LambdaQueryWrapper<InspectionTemplateItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InspectionTemplateItem::getTemplateId, templateId);
        queryWrapper.orderByAsc(InspectionTemplateItem::getSortOrder);
        return inspectionTemplateItemMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createTemplate(InspectionTemplate template, List<InspectionTemplateItem> items) {
        // 生成模板编码
        if (template.getTemplateCode() == null || template.getTemplateCode().isEmpty()) {
            template.setTemplateCode("TPL_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        }

        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        template.setUseCount(0L);

        int result = inspectionTemplateMapper.insert(template);

        // 创建巡检项
        if (result > 0 && items != null && !items.isEmpty()) {
            for (InspectionTemplateItem item : items) {
                item.setTemplateId(template.getId());
                item.setCreateTime(LocalDateTime.now());
                item.setUpdateTime(LocalDateTime.now());
                inspectionTemplateItemMapper.insert(item);
            }
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTemplate(InspectionTemplate template, List<InspectionTemplateItem> items) {
        template.setUpdateTime(LocalDateTime.now());
        int result = inspectionTemplateMapper.updateById(template);

        // 删除原有巡检项
        if (result > 0) {
            LambdaQueryWrapper<InspectionTemplateItem> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(InspectionTemplateItem::getTemplateId, template.getId());
            inspectionTemplateItemMapper.delete(deleteWrapper);

            // 创建新巡检项
            if (items != null && !items.isEmpty()) {
                for (InspectionTemplateItem item : items) {
                    item.setTemplateId(template.getId());
                    item.setCreateTime(LocalDateTime.now());
                    item.setUpdateTime(LocalDateTime.now());
                    inspectionTemplateItemMapper.insert(item);
                }
            }
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTemplate(Long id) {
        // 删除巡检项
        LambdaQueryWrapper<InspectionTemplateItem> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(InspectionTemplateItem::getTemplateId, id);
        inspectionTemplateItemMapper.delete(deleteWrapper);

        // 删除模板
        return inspectionTemplateMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InspectionTemplate copyTemplate(Long id, String newTemplateName) {
        InspectionTemplate originalTemplate = inspectionTemplateMapper.selectById(id);
        if (originalTemplate == null) {
            return null;
        }

        // 复制模板
        InspectionTemplate newTemplate = new InspectionTemplate();
        BeanUtils.copyProperties(originalTemplate, newTemplate, "id", "templateCode", "createTime", "updateTime", "useCount");
        newTemplate.setTemplateName(newTemplateName);
        newTemplate.setTemplateCode("TPL_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        newTemplate.setCreateTime(LocalDateTime.now());
        newTemplate.setUpdateTime(LocalDateTime.now());
        newTemplate.setUseCount(0L);
        inspectionTemplateMapper.insert(newTemplate);

        // 复制巡检项
        List<InspectionTemplateItem> originalItems = getTemplateItems(id);
        if (originalItems != null && !originalItems.isEmpty()) {
            for (InspectionTemplateItem originalItem : originalItems) {
                InspectionTemplateItem newItem = new InspectionTemplateItem();
                BeanUtils.copyProperties(originalItem, newItem, "id", "createTime", "updateTime");
                newItem.setTemplateId(newTemplate.getId());
                newItem.setCreateTime(LocalDateTime.now());
                newItem.setUpdateTime(LocalDateTime.now());
                inspectionTemplateItemMapper.insert(newItem);
            }
        }

        return newTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableTemplate(Long id) {
        InspectionTemplate template = inspectionTemplateMapper.selectById(id);
        if (template == null) {
            return false;
        }

        template.setStatus("active");
        template.setUpdateTime(LocalDateTime.now());
        return inspectionTemplateMapper.updateById(template) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableTemplate(Long id) {
        InspectionTemplate template = inspectionTemplateMapper.selectById(id);
        if (template == null) {
            return false;
        }

        template.setStatus("inactive");
        template.setUpdateTime(LocalDateTime.now());
        return inspectionTemplateMapper.updateById(template) > 0;
    }

    @Override
    public List<InspectionTemplate> getRecommendedTemplates(String deviceType) {
        LambdaQueryWrapper<InspectionTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InspectionTemplate::getDeviceType, deviceType);
        queryWrapper.eq(InspectionTemplate::getStatus, "active");
        queryWrapper.orderByDesc(InspectionTemplate::getUseCount);
        return inspectionTemplateMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean useTemplate(Long id) {
        InspectionTemplate template = inspectionTemplateMapper.selectById(id);
        if (template == null) {
            return false;
        }

        Long newCount = (template.getUseCount() == null ? 0 : template.getUseCount()) + 1;
        template.setUseCount(newCount);
        return inspectionTemplateMapper.updateById(template) > 0;
    }
}
