package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.MonitorConfig;
import com.roominspection.backend.mapper.MonitorConfigMapper;
import com.roominspection.backend.service.MonitorConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 监控配置服务实现
 */
@Slf4j
@Service
public class MonitorConfigServiceImpl extends ServiceImpl<MonitorConfigMapper, MonitorConfig>
        implements MonitorConfigService {

    @Autowired
    private MonitorConfigMapper monitorConfigMapper;

    /**
     * 根据设备类型获取监控配置
     */
    @Override
    @Cacheable(value = "monitorConfig", key = "#deviceType")
    public MonitorConfig getConfigByDeviceType(String deviceType) {
        List<MonitorConfig> configs = monitorConfigMapper.selectByDeviceType(deviceType);
        return configs.isEmpty() ? null : configs.get(0);
    }

    /**
     * 获取采集频率
     */
    @Override
    @Cacheable(value = "collectionInterval", key = "#deviceType")
    public Integer getCollectionInterval(String deviceType) {
        MonitorConfig config = getConfigByDeviceType(deviceType);
        return config != null ? config.getCollectionInterval() : 60; // 默认60秒
    }

    /**
     * 设置采集频率
     */
    @Override
    @CacheEvict(value = {"monitorConfig", "collectionInterval"}, key = "#configId")
    @Transactional(rollbackFor = Exception.class)
    public boolean setCollectionInterval(Long configId, Integer interval, Long updatedBy, String updatedByName) {
        // 验证采集频率范围（1-86400秒）
        if (interval < 1 || interval > 86400) {
            throw new IllegalArgumentException("采集频率必须在1-86400秒之间");
        }

        MonitorConfig config = monitorConfigMapper.selectById(configId);
        if (config == null) {
            throw new IllegalArgumentException("配置不存在");
        }

        config.setCollectionInterval(interval);
        config.setUpdatedBy(updatedBy);
        config.setUpdatedByName(updatedByName);
        config.setUpdatedAt(LocalDateTime.now());

        boolean result = monitorConfigMapper.updateById(config) > 0;
        if (result) {
            log.info("更新监控配置采集频率成功: configId={}, interval={}, deviceType={}",
                    configId, interval, config.getDeviceType());
        }

        return result;
    }

    /**
     * 获取并发数限制
     */
    @Override
    @Cacheable(value = "concurrencyLimit", key = "#deviceType")
    public Integer getConcurrencyLimit(String deviceType) {
        MonitorConfig config = getConfigByDeviceType(deviceType);
        return config != null ? config.getConcurrencyLimit() : 50; // 默认50并发
    }

    /**
     * 获取所有启用的配置
     */
    @Override
    public List<MonitorConfig> getAllEnabledConfigs() {
        return monitorConfigMapper.selectAllEnabled();
    }

    /**
     * 获取配置统计信息
     */
    @Override
    public Map<String, Object> getConfigStatistics() {
        List<MonitorConfig> allConfigs = monitorConfigMapper.selectList(null);
        List<MonitorConfig> enabledConfigs = monitorConfigMapper.selectAllEnabled();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", allConfigs.size());
        statistics.put("enabled", enabledConfigs.size());
        statistics.put("disabled", allConfigs.size() - enabledConfigs.size());

        // 按设备类型统计
        Map<String, Long> byType = new HashMap<>();
        for (MonitorConfig config : enabledConfigs) {
            String deviceType = config.getDeviceType();
            byType.put(deviceType, byType.getOrDefault(deviceType, 0L) + 1);
        }
        statistics.put("byType", byType);

        // 统计采集频率
        Map<String, Long> byInterval = new HashMap<>();
        for (MonitorConfig config : enabledConfigs) {
            Integer interval = config.getCollectionInterval();
            String key;
            if (interval <= 10) {
                key = "高频(≤10s)";
            } else if (interval <= 60) {
                key = "中频(11-60s)";
            } else if (interval <= 300) {
                key = "低频(61-300s)";
            } else {
                key = "极低频(>300s)";
            }
            byInterval.put(key, byInterval.getOrDefault(key, 0L) + 1);
        }
        statistics.put("byInterval", byInterval);

        return statistics;
    }
}
