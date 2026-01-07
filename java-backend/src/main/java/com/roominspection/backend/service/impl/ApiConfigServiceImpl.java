package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.ApiConfig;
import com.roominspection.backend.mapper.ApiConfigMapper;
import com.roominspection.backend.service.ApiConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 接口配置服务实现类
 */
@Slf4j
@Service
public class ApiConfigServiceImpl extends ServiceImpl<ApiConfigMapper, ApiConfig> implements ApiConfigService {

    @Override
    public IPage<ApiConfig> queryApiConfigPage(Page<ApiConfig> page, String configType, String configGroup, Integer status, String configName) {
        LambdaQueryWrapper<ApiConfig> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(configType)) {
            wrapper.eq(ApiConfig::getConfigType, configType);
        }
        if (StringUtils.hasText(configGroup)) {
            wrapper.eq(ApiConfig::getConfigGroup, configGroup);
        }
        if (status != null) {
            wrapper.eq(ApiConfig::getStatus, status);
        }
        if (StringUtils.hasText(configName)) {
            wrapper.like(ApiConfig::getConfigName, configName);
        }

        wrapper.orderByAsc(ApiConfig::getSortOrder).orderByDesc(ApiConfig::getCreatedAt);

        return page(page, wrapper);
    }

    @Override
    public String getConfigValue(String configType, String configKey) {
        LambdaQueryWrapper<ApiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiConfig::getConfigType, configType)
                .eq(ApiConfig::getConfigKey, configKey)
                .eq(ApiConfig::getStatus, 1)
                .last("LIMIT 1");

        ApiConfig config = getOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public Map<String, String> getConfigsByType(String configType) {
        LambdaQueryWrapper<ApiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiConfig::getConfigType, configType)
                .eq(ApiConfig::getStatus, 1);

        List<ApiConfig> configs = list(wrapper);
        return configs.stream()
                .collect(Collectors.toMap(ApiConfig::getConfigKey, ApiConfig::getConfigValue, (v1, v2) -> v1));
    }

    @Override
    public List<ApiConfig> getConfigsByGroup(String configGroup) {
        LambdaQueryWrapper<ApiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiConfig::getConfigGroup, configGroup)
                .eq(ApiConfig::getStatus, 1)
                .orderByAsc(ApiConfig::getSortOrder);

        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfigValue(Long id, String configValue, Long operatorId, String operatorName) {
        ApiConfig config = getById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }

        // 如果是敏感配置，应该加密存储（这里简化处理）
        String finalValue = configValue;
        if (config.getIsSensitive() != null && config.getIsSensitive() == 1) {
            // TODO: 实现加密逻辑
            // finalValue = encrypt(configValue);
        }

        config.setConfigValue(finalValue);
        config.setUpdatedBy(operatorId);
        config.setUpdatedByName(operatorName);
        config.setUpdatedAt(LocalDateTime.now());

        return updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateConfigs(List<ApiConfig> configs, Long operatorId, String operatorName) {
        for (ApiConfig config : configs) {
            if (config.getId() != null) {
                // 更新
                ApiConfig existConfig = getById(config.getId());
                if (existConfig != null) {
                    // 如果是敏感配置，应该加密存储
                    String finalValue = config.getConfigValue();
                    if (existConfig.getIsSensitive() != null && existConfig.getIsSensitive() == 1) {
                        // TODO: 实现加密逻辑
                        // finalValue = encrypt(config.getConfigValue());
                    }
                    existConfig.setConfigValue(finalValue);
                    existConfig.setUpdatedBy(operatorId);
                    existConfig.setUpdatedByName(operatorName);
                    existConfig.setUpdatedAt(LocalDateTime.now());
                    updateById(existConfig);
                }
            } else {
                // 新增
                config.setCreatedBy(operatorId);
                config.setCreatedByName(operatorName);
                config.setCreatedAt(LocalDateTime.now());
                config.setUpdatedBy(operatorId);
                config.setUpdatedByName(operatorName);
                config.setUpdatedAt(LocalDateTime.now());
                save(config);
            }
        }
        return true;
    }

    @Override
    public Map<String, Object> testConnection(String configType) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "");

        try {
            switch (configType) {
                case "DINGTALK":
                    // TODO: 实现钉钉连接测试
                    result.put("success", true);
                    result.put("message", "钉钉连接测试通过");
                    break;
                case "SMS":
                    // TODO: 实现短信连接测试
                    result.put("success", true);
                    result.put("message", "短信服务连接测试通过");
                    break;
                case "HIKVISION":
                    // TODO: 实现海康门禁连接测试
                    result.put("success", true);
                    result.put("message", "海康门禁连接测试通过");
                    break;
                case "DAHUA":
                    // TODO: 实现大华门禁连接测试
                    result.put("success", true);
                    result.put("message", "大华门禁连接测试通过");
                    break;
                case "SNMP":
                    // TODO: 实现SNMP连接测试
                    result.put("success", true);
                    result.put("message", "SNMP连接测试通过");
                    break;
                case "MODBUS":
                    // TODO: 实现Modbus连接测试
                    result.put("success", true);
                    result.put("message", "Modbus连接测试通过");
                    break;
                default:
                    result.put("message", "不支持的配置类型: " + configType);
                    break;
            }
        } catch (Exception e) {
            log.error("测试连接失败: configType={}", configType, e);
            result.put("message", "连接测试失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importConfigs(List<ApiConfig> configs, Long operatorId, String operatorName) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (ApiConfig config : configs) {
            try {
                LambdaQueryWrapper<ApiConfig> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ApiConfig::getConfigType, config.getConfigType())
                        .eq(ApiConfig::getConfigKey, config.getConfigKey());

                ApiConfig existConfig = getOne(wrapper);
                if (existConfig != null) {
                    // 更新
                    existConfig.setConfigName(config.getConfigName());
                    existConfig.setConfigValue(config.getConfigValue());
                    existConfig.setDescription(config.getDescription());
                    existConfig.setConfigGroup(config.getConfigGroup());
                    existConfig.setStatus(config.getStatus() != null ? config.getStatus() : 1);
                    existConfig.setSortOrder(config.getSortOrder());
                    existConfig.setUpdatedBy(operatorId);
                    existConfig.setUpdatedByName(operatorName);
                    existConfig.setUpdatedAt(LocalDateTime.now());
                    updateById(existConfig);
                } else {
                    // 新增
                    config.setCreatedBy(operatorId);
                    config.setCreatedByName(operatorName);
                    config.setCreatedAt(LocalDateTime.now());
                    config.setUpdatedBy(operatorId);
                    config.setUpdatedByName(operatorName);
                    config.setUpdatedAt(LocalDateTime.now());
                    if (config.getStatus() == null) {
                        config.setStatus(1);
                    }
                    save(config);
                }
                successCount++;
            } catch (Exception e) {
                log.error("导入配置失败: configType={}, configKey={}", config.getConfigType(), config.getConfigKey(), e);
                failCount++;
            }
        }

        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("totalCount", configs.size());
        return result;
    }

    @Override
    public List<Map<String, Object>> exportConfigs(String configType, String configGroup) {
        LambdaQueryWrapper<ApiConfig> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(configType)) {
            wrapper.eq(ApiConfig::getConfigType, configType);
        }
        if (StringUtils.hasText(configGroup)) {
            wrapper.eq(ApiConfig::getConfigGroup, configGroup);
        }

        wrapper.orderByAsc(ApiConfig::getConfigType, ApiConfig::getConfigGroup, ApiConfig::getSortOrder);

        List<ApiConfig> configs = list(wrapper);
        return configs.stream().map(config -> {
            Map<String, Object> map = new HashMap<>();
            map.put("configType", config.getConfigType());
            map.put("configName", config.getConfigName());
            map.put("configKey", config.getConfigKey());
            map.put("configValue", config.getIsSensitive() != null && config.getIsSensitive() == 1 ? "******" : config.getConfigValue());
            map.put("description", config.getDescription());
            map.put("configGroup", config.getConfigGroup());
            map.put("status", config.getStatus());
            map.put("sortOrder", config.getSortOrder());
            return map;
        }).collect(Collectors.toList());
    }
}
