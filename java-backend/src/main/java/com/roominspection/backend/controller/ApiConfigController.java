package com.roominspection.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.ApiConfig;
import com.roominspection.backend.service.ApiConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 接口配置管理Controller
 */
@Slf4j
@Api(tags = "接口配置管理")
@RestController
@RequestMapping("/api/api-config")
public class ApiConfigController {

    @Autowired
    private ApiConfigService apiConfigService;

    /**
     * 分页查询接口配置
     */
    @ApiOperation("分页查询接口配置")
    @GetMapping("/page")
    public Result<IPage<ApiConfig>> page(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "20") Integer size,
            @ApiParam("配置类型") @RequestParam(required = false) String configType,
            @ApiParam("配置分组") @RequestParam(required = false) String configGroup,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("配置名称") @RequestParam(required = false) String configName) {
        try {
            Page<ApiConfig> page = new Page<>(current, size);
            IPage<ApiConfig> result = apiConfigService.queryApiConfigPage(page, configType, configGroup, status, configName);
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询接口配置失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取接口配置
     */
    @ApiOperation("根据ID获取接口配置")
    @GetMapping("/{id}")
    public Result<ApiConfig> getById(@ApiParam("配置ID") @PathVariable Long id) {
        try {
            ApiConfig config = apiConfigService.getById(id);
            if (config != null && config.getIsSensitive() != null && config.getIsSensitive() == 1) {
                // 敏感信息脱敏
                config.setConfigValue("******");
            }
            return Result.success(config);
        } catch (Exception e) {
            log.error("获取接口配置失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 新增接口配置
     */
    @ApiOperation("新增接口配置")
    @PostMapping
    public Result<ApiConfig> add(@RequestBody ApiConfig config) {
        try {
            // 检查配置键是否已存在
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ApiConfig> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(ApiConfig::getConfigType, config.getConfigType())
                    .eq(ApiConfig::getConfigKey, config.getConfigKey());
            if (apiConfigService.count(wrapper) > 0) {
                return Result.error("该配置类型的配置键已存在");
            }

            apiConfigService.save(config);
            return Result.success(config, "新增成功");
        } catch (Exception e) {
            log.error("新增接口配置失败", e);
            return Result.error("新增失败: " + e.getMessage());
        }
    }

    /**
     * 更新接口配置
     */
    @ApiOperation("更新接口配置")
    @PutMapping("/{id}")
    public Result<ApiConfig> update(@ApiParam("配置ID") @PathVariable Long id, @RequestBody ApiConfig config) {
        try {
            config.setId(id);
            apiConfigService.updateById(config);
            return Result.success(config, "更新成功");
        } catch (Exception e) {
            log.error("更新接口配置失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除接口配置
     */
    @ApiOperation("删除接口配置")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@ApiParam("配置ID") @PathVariable Long id) {
        try {
            apiConfigService.removeById(id);
            return Result.success(null, "删除成功");
        } catch (Exception e) {
            log.error("删除接口配置失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除接口配置
     */
    @ApiOperation("批量删除接口配置")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        try {
            apiConfigService.removeByIds(ids);
            return Result.success(null, "批量删除成功");
        } catch (Exception e) {
            log.error("批量删除接口配置失败", e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 根据配置类型获取所有配置
     */
    @ApiOperation("根据配置类型获取所有配置")
    @GetMapping("/type/{configType}")
    public Result<Map<String, String>> getConfigsByType(@ApiParam("配置类型") @PathVariable String configType) {
        try {
            Map<String, String> configs = apiConfigService.getConfigsByType(configType);
            return Result.success(configs);
        } catch (Exception e) {
            log.error("获取配置失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据配置分组获取所有配置
     */
    @ApiOperation("根据配置分组获取所有配置")
    @GetMapping("/group/{configGroup}")
    public Result<List<ApiConfig>> getConfigsByGroup(@ApiParam("配置分组") @PathVariable String configGroup) {
        try {
            List<ApiConfig> configs = apiConfigService.getConfigsByGroup(configGroup);
            // 敏感信息脱敏
            configs.forEach(config -> {
                if (config.getIsSensitive() != null && config.getIsSensitive() == 1) {
                    config.setConfigValue("******");
                }
            });
            return Result.success(configs);
        } catch (Exception e) {
            log.error("获取配置失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 测试接口连接
     */
    @ApiOperation("测试接口连接")
    @PostMapping("/test/{configType}")
    public Result<Map<String, Object>> testConnection(@ApiParam("配置类型") @PathVariable String configType) {
        try {
            Map<String, Object> result = apiConfigService.testConnection(configType);
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试连接失败", e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新配置
     */
    @ApiOperation("批量更新配置")
    @PutMapping("/batch")
    public Result<Void> batchUpdate(@RequestBody List<ApiConfig> configs) {
        try {
            // TODO: 从session或token中获取当前操作人信息
            Long operatorId = 1L;
            String operatorName = "系统管理员";
            apiConfigService.batchUpdateConfigs(configs, operatorId, operatorName);
            return Result.success(null, "批量更新成功");
        } catch (Exception e) {
            log.error("批量更新配置失败", e);
            return Result.error("批量更新失败: " + e.getMessage());
        }
    }

    /**
     * 导出配置
     */
    @ApiOperation("导出配置")
    @GetMapping("/export")
    public Result<List<Map<String, Object>>> exportConfigs(
            @ApiParam("配置类型") @RequestParam(required = false) String configType,
            @ApiParam("配置分组") @RequestParam(required = false) String configGroup) {
        try {
            List<Map<String, Object>> result = apiConfigService.exportConfigs(configType, configGroup);
            return Result.success(result);
        } catch (Exception e) {
            log.error("导出配置失败", e);
            return Result.error("导出失败: " + e.getMessage());
        }
    }

    /**
     * 获取配置类型列表
     */
    @ApiOperation("获取配置类型列表")
    @GetMapping("/config-types")
    public Result<List<Map<String, String>>> getConfigTypes() {
        try {
            List<Map<String, String>> types = List.of(
                    Map.of("value", "DINGTALK", "label", "钉钉"),
                    Map.of("value", "SMS", "label", "短信"),
                    Map.of("value", "EMAIL", "label", "邮件"),
                    Map.of("value", "HIKVISION", "label", "海康门禁"),
                    Map.of("value", "DAHUA", "label", "大华门禁"),
                    Map.of("value", "SNMP", "label", "SNMP监控"),
                    Map.of("value", "MODBUS", "label", "Modbus监控"),
                    Map.of("value", "BMS", "label", "BMS接口"),
                    Map.of("value", "SENSOR", "label", "传感器网络"),
                    Map.of("value", "FIRE", "label", "消防主机"),
                    Map.of("value", "OTHER", "label", "其他")
            );
            return Result.success(types);
        } catch (Exception e) {
            log.error("获取配置类型列表失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用配置
     */
    @ApiOperation("启用/禁用配置")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@ApiParam("配置ID") @PathVariable Long id, @ApiParam("状态") @RequestParam Integer status) {
        try {
            ApiConfig config = apiConfigService.getById(id);
            if (config == null) {
                return Result.error("配置不存在");
            }
            config.setStatus(status);
            apiConfigService.updateById(config);
            return Result.success(null, "状态更新成功");
        } catch (Exception e) {
            log.error("更新配置状态失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }
}
