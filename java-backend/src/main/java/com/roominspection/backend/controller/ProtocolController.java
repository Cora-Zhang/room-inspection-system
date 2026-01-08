package com.roominspection.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.common.ApiVersion;
import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.ProtocolPlugin;
import com.roominspection.backend.service.ProtocolService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 协议管理控制器
 */
@RestController
@RequestMapping("/api/v1/plugins/protocols")
@Api(tags = "协议插件管理")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProtocolController {

    @Autowired
    private ProtocolService protocolService;

    /**
     * 分页查询协议插件列表
     */
    @GetMapping
    @ApiOperation(value = "分页查询协议插件列表", notes = "支持按协议名称和状态筛选")
    @ApiVersion("v1")
    public Result<Page<ProtocolPlugin>> listPlugins(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "协议名称") @RequestParam(required = false) String protocolName,
            @ApiParam(value = "状态") @RequestParam(required = false) String status) {
        Page<ProtocolPlugin> result = protocolService.listPlugins(page, size, protocolName, status);
        return Result.page(result);
    }

    /**
     * 根据ID获取协议插件详情
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取协议插件详情", notes = "根据ID查询协议插件详细信息")
    @ApiVersion("v1")
    public Result<ProtocolPlugin> getPluginById(
            @ApiParam(value = "插件ID", required = true) @PathVariable Long id) {
        ProtocolPlugin plugin = protocolService.getPluginById(id);
        if (plugin == null) {
            return Result.error("插件不存在");
        }
        return Result.success(plugin);
    }

    /**
     * 注册协议插件
     */
    @PostMapping
    @ApiOperation(value = "注册协议插件", notes = "注册新的协议插件")
    @ApiVersion("v1")
    public Result<String> registerPlugin(
            @ApiParam(value = "协议插件信息", required = true) @RequestBody ProtocolPlugin plugin) {
        boolean success = protocolService.registerPlugin(plugin);
        return success ? Result.success("注册成功") : Result.error("注册失败");
    }

    /**
     * 上传并注册协议插件
     */
    @PostMapping("/upload")
    @ApiOperation(value = "上传并注册协议插件", notes = "上传JAR文件并注册协议插件")
    @ApiVersion("v1")
    public Result<String> uploadAndRegisterPlugin(
            @ApiParam(value = "协议名称", required = true) @RequestParam String protocolName,
            @ApiParam(value = "协议类型", required = true) @RequestParam String protocolType,
            @ApiParam(value = "协议版本", required = true) @RequestParam String version,
            @ApiParam(value = "协议描述") @RequestParam(required = false) String description,
            @ApiParam(value = "配置Schema") @RequestParam(required = false) String configSchema,
            @ApiParam(value = "实现类全限定名", required = true) @RequestParam String implementationClass,
            @ApiParam(value = "作者") @RequestParam(required = false) String author,
            @ApiParam(value = "JAR文件", required = true) @RequestParam("file") MultipartFile file) {
        try {
            ProtocolPlugin plugin = new ProtocolPlugin();
            plugin.setProtocolName(protocolName);
            plugin.setProtocolType(protocolType);
            plugin.setVersion(version);
            plugin.setDescription(description);
            plugin.setConfigSchema(configSchema);
            plugin.setImplementationClass(implementationClass);
            plugin.setAuthor(author);

            byte[] jarBytes = file.getBytes();
            boolean success = protocolService.uploadAndRegisterPlugin(plugin, jarBytes);
            return success ? Result.success("上传并注册成功") : Result.error("上传并注册失败");
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 更新协议插件
     */
    @PutMapping("/{id}")
    @ApiOperation(value = "更新协议插件", notes = "更新协议插件信息")
    @ApiVersion("v1")
    public Result<String> updatePlugin(
            @ApiParam(value = "插件ID", required = true) @PathVariable Long id,
            @ApiParam(value = "协议插件信息", required = true) @RequestBody ProtocolPlugin plugin) {
        plugin.setId(id);
        boolean success = protocolService.updatePlugin(plugin);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除协议插件
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除协议插件", notes = "删除指定的协议插件")
    @ApiVersion("v1")
    public Result<String> deletePlugin(
            @ApiParam(value = "插件ID", required = true) @PathVariable Long id) {
        boolean success = protocolService.deletePlugin(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 启用协议插件
     */
    @PutMapping("/{id}/enable")
    @ApiOperation(value = "启用协议插件", notes = "启用指定的协议插件")
    @ApiVersion("v1")
    public Result<String> enablePlugin(
            @ApiParam(value = "插件ID", required = true) @PathVariable Long id) {
        boolean success = protocolService.enablePlugin(id);
        return success ? Result.success("启用成功") : Result.error("启用失败");
    }

    /**
     * 禁用协议插件
     */
    @PutMapping("/{id}/disable")
    @ApiOperation(value = "禁用协议插件", notes = "禁用指定的协议插件")
    @ApiVersion("v1")
    public Result<String> disablePlugin(
            @ApiParam(value = "插件ID", required = true) @PathVariable Long id) {
        boolean success = protocolService.disablePlugin(id);
        return success ? Result.success("禁用成功") : Result.error("禁用失败");
    }

    /**
     * 获取协议插件统计信息
     */
    @GetMapping("/statistics")
    @ApiOperation(value = "获取协议插件统计信息", notes = "统计协议插件的总体情况")
    @ApiVersion("v1")
    public Result<Map<String, Object>> getPluginStatistics() {
        Map<String, Object> statistics = protocolService.getPluginStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取已启用的协议列表
     */
    @GetMapping("/enabled")
    @ApiOperation(value = "获取已启用的协议列表", notes = "查询所有已启用的协议插件")
    @ApiVersion("v1")
    public Result<List<ProtocolPlugin>> getEnabledPlugins() {
        List<ProtocolPlugin> plugins = protocolService.getEnabledPlugins();
        return Result.success(plugins);
    }
}
