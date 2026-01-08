package com.roominspection.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.common.ApiVersion;
import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.InspectionTemplate;
import com.roominspection.backend.entity.InspectionTemplateItem;
import com.roominspection.backend.service.InspectionTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 巡检模板管理控制器
 */
@RestController
@RequestMapping("/api/v1/inspection/templates")
@Api(tags = "巡检模板管理")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InspectionTemplateController {

    @Autowired
    private InspectionTemplateService inspectionTemplateService;

    /**
     * 分页查询巡检模板列表
     */
    @GetMapping
    @ApiOperation(value = "分页查询巡检模板列表", notes = "支持按模板名称、设备类型、状态筛选")
    @ApiVersion("v1")
    public Result<Page<InspectionTemplate>> listTemplates(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "模板名称") @RequestParam(required = false) String templateName,
            @ApiParam(value = "设备类型") @RequestParam(required = false) String deviceType,
            @ApiParam(value = "状态") @RequestParam(required = false) String status) {
        Page<InspectionTemplate> result = inspectionTemplateService.listTemplates(page, size, templateName, deviceType, status);
        return Result.page(result);
    }

    /**
     * 根据ID获取巡检模板详情
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取巡检模板详情", notes = "根据ID查询巡检模板详细信息")
    @ApiVersion("v1")
    public Result<Map<String, Object>> getTemplateById(
            @ApiParam(value = "模板ID", required = true) @PathVariable Long id) {
        InspectionTemplate template = inspectionTemplateService.getTemplateById(id);
        if (template == null) {
            return Result.error("模板不存在");
        }

        List<InspectionTemplateItem> items = inspectionTemplateService.getTemplateItems(id);

        Map<String, Object> data = new java.util.HashMap<>();
        data.put("template", template);
        data.put("items", items);

        return Result.success(data);
    }

    /**
     * 创建巡检模板
     */
    @PostMapping
    @ApiOperation(value = "创建巡检模板", notes = "创建新的巡检模板")
    @ApiVersion("v1")
    public Result<String> createTemplate(
            @ApiParam(value = "巡检模板信息", required = true) @RequestBody Map<String, Object> request) {
        try {
            InspectionTemplate template = (InspectionTemplate) request.get("template");
            @SuppressWarnings("unchecked")
            List<InspectionTemplateItem> items = (List<InspectionTemplateItem>) request.get("items");

            boolean success = inspectionTemplateService.createTemplate(template, items);
            return success ? Result.success("创建成功") : Result.error("创建失败");
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新巡检模板
     */
    @PutMapping("/{id}")
    @ApiOperation(value = "更新巡检模板", notes = "更新巡检模板信息")
    @ApiVersion("v1")
    public Result<String> updateTemplate(
            @ApiParam(value = "模板ID", required = true) @PathVariable Long id,
            @ApiParam(value = "巡检模板信息", required = true) @RequestBody Map<String, Object> request) {
        try {
            InspectionTemplate template = (InspectionTemplate) request.get("template");
            template.setId(id);

            @SuppressWarnings("unchecked")
            List<InspectionTemplateItem> items = (List<InspectionTemplateItem>) request.get("items");

            boolean success = inspectionTemplateService.updateTemplate(template, items);
            return success ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除巡检模板
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除巡检模板", notes = "删除指定的巡检模板")
    @ApiVersion("v1")
    public Result<String> deleteTemplate(
            @ApiParam(value = "模板ID", required = true) @PathVariable Long id) {
        boolean success = inspectionTemplateService.deleteTemplate(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 复制巡检模板
     */
    @PostMapping("/{id}/copy")
    @ApiOperation(value = "复制巡检模板", notes = "复制指定的巡检模板")
    @ApiVersion("v1")
    public Result<InspectionTemplate> copyTemplate(
            @ApiParam(value = "模板ID", required = true) @PathVariable Long id,
            @ApiParam(value = "新模板名称", required = true) @RequestParam String newTemplateName) {
        InspectionTemplate newTemplate = inspectionTemplateService.copyTemplate(id, newTemplateName);
        if (newTemplate == null) {
            return Result.error("复制失败");
        }
        return Result.success(newTemplate);
    }

    /**
     * 启用巡检模板
     */
    @PutMapping("/{id}/enable")
    @ApiOperation(value = "启用巡检模板", notes = "启用指定的巡检模板")
    @ApiVersion("v1")
    public Result<String> enableTemplate(
            @ApiParam(value = "模板ID", required = true) @PathVariable Long id) {
        boolean success = inspectionTemplateService.enableTemplate(id);
        return success ? Result.success("启用成功") : Result.error("启用失败");
    }

    /**
     * 禁用巡检模板
     */
    @PutMapping("/{id}/disable")
    @ApiOperation(value = "禁用巡检模板", notes = "禁用指定的巡检模板")
    @ApiVersion("v1")
    public Result<String> disableTemplate(
            @ApiParam(value = "模板ID", required = true) @PathVariable Long id) {
        boolean success = inspectionTemplateService.disableTemplate(id);
        return success ? Result.success("禁用成功") : Result.error("禁用失败");
    }

    /**
     * 根据设备类型获取推荐模板
     */
    @GetMapping("/recommended/{deviceType}")
    @ApiOperation(value = "获取推荐模板", notes = "根据设备类型获取推荐的巡检模板")
    @ApiVersion("v1")
    public Result<List<InspectionTemplate>> getRecommendedTemplates(
            @ApiParam(value = "设备类型", required = true) @PathVariable String deviceType) {
        List<InspectionTemplate> templates = inspectionTemplateService.getRecommendedTemplates(deviceType);
        return Result.success(templates);
    }
}
