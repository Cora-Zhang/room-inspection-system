package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.entity.InspectionTemplate;
import com.roominspection.backend.entity.InspectionTemplateItem;

import java.util.List;

/**
 * 巡检模板服务接口
 */
public interface InspectionTemplateService {

    /**
     * 分页查询巡检模板列表
     * @param page 页码
     * @param size 每页数量
     * @param templateName 模板名称（可选）
     * @param deviceType 设备类型（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    Page<InspectionTemplate> listTemplates(Integer page, Integer size, String templateName, String deviceType, String status);

    /**
     * 根据ID获取巡检模板
     * @param id 模板ID
     * @return 巡检模板
     */
    InspectionTemplate getTemplateById(Long id);

    /**
     * 根据编码获取巡检模板
     * @param templateCode 模板编码
     * @return 巡检模板
     */
    InspectionTemplate getTemplateByCode(String templateCode);

    /**
     * 获取巡检模板的巡检项列表
     * @param templateId 模板ID
     * @return 巡检项列表
     */
    List<InspectionTemplateItem> getTemplateItems(Long templateId);

    /**
     * 创建巡检模板
     * @param template 巡检模板
     * @param items 巡检项列表
     * @return 创建是否成功
     */
    boolean createTemplate(InspectionTemplate template, List<InspectionTemplateItem> items);

    /**
     * 更新巡检模板
     * @param template 巡检模板
     * @param items 巡检项列表
     * @return 更新是否成功
     */
    boolean updateTemplate(InspectionTemplate template, List<InspectionTemplateItem> items);

    /**
     * 删除巡检模板
     * @param id 模板ID
     * @return 删除是否成功
     */
    boolean deleteTemplate(Long id);

    /**
     * 复制巡检模板
     * @param id 模板ID
     * @param newTemplateName 新模板名称
     * @return 复制后的模板
     */
    InspectionTemplate copyTemplate(Long id, String newTemplateName);

    /**
     * 启用巡检模板
     * @param id 模板ID
     * @return 启用是否成功
     */
    boolean enableTemplate(Long id);

    /**
     * 禁用巡检模板
     * @param id 模板ID
     * @return 禁用是否成功
     */
    boolean disableTemplate(Long id);

    /**
     * 根据设备类型获取推荐模板
     * @param deviceType 设备类型
     * @return 模板列表
     */
    List<InspectionTemplate> getRecommendedTemplates(String deviceType);

    /**
     * 使用模板（增加使用计数）
     * @param id 模板ID
     * @return 是否成功
     */
    boolean useTemplate(Long id);
}
