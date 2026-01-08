package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.MonitorConfig;

import java.util.List;
import java.util.Map;

/**
 * 监控配置服务接口
 */
public interface MonitorConfigService extends IService<MonitorConfig> {

    /**
     * 根据设备类型获取监控配置
     *
     * @param deviceType 设备类型
     * @return 监控配置
     */
    MonitorConfig getConfigByDeviceType(String deviceType);

    /**
     * 获取采集频率
     *
     * @param deviceType 设备类型
     * @return 采集频率（秒）
     */
    Integer getCollectionInterval(String deviceType);

    /**
     * 设置采集频率
     *
     * @param configId      配置ID
     * @param interval      采集频率（秒）
     * @param updatedBy     更新人ID
     * @param updatedByName 更新人姓名
     * @return 是否成功
     */
    boolean setCollectionInterval(Long configId, Integer interval, Long updatedBy, String updatedByName);

    /**
     * 获取并发数限制
     *
     * @param deviceType 设备类型
     * @return 并发数限制
     */
    Integer getConcurrencyLimit(String deviceType);

    /**
     * 获取所有启用的配置
     *
     * @return 配置列表
     */
    List<MonitorConfig> getAllEnabledConfigs();

    /**
     * 获取配置统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getConfigStatistics();
}
