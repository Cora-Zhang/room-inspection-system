package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.EnvironmentSensor;
import java.util.List;
import java.util.Map;

/**
 * 环境传感器服务接口
 */
public interface EnvironmentSensorService extends IService<EnvironmentSensor> {

    /**
     * 添加传感器
     */
    Long addSensor(EnvironmentSensor sensor);

    /**
     * 更新传感器
     */
    boolean updateSensor(EnvironmentSensor sensor);

    /**
     * 删除传感器
     */
    boolean deleteSensor(Long id);

    /**
     * 批量删除传感器
     */
    boolean batchDeleteSensors(List<Long> ids);

    /**
     * 获取传感器详情
     */
    EnvironmentSensor getSensorDetail(Long id);

    /**
     * 查询传感器列表
     */
    List<EnvironmentSensor> getSensorList(Map<String, Object> params);

    /**
     * 根据机房ID查询传感器列表
     */
    List<EnvironmentSensor> getSensorsByRoomId(Long roomId);

    /**
     * 根据传感器类型查询传感器列表
     */
    List<EnvironmentSensor> getSensorsByType(String sensorType);

    /**
     * 批量采集传感器数据
     */
    void batchCollectData();

    /**
     * 采集单个传感器数据
     */
    void collectSensorData(Long sensorId);

    /**
     * 手动触发数据采集
     */
    void manualCollectData(Long sensorId);

    /**
     * 获取传感器最新数据
     */
    Map<String, Object> getSensorLatestData(Long sensorId);

    /**
     * 获取机房传感器统计数据
     */
    Map<String, Object> getRoomSensorStatistics(Long roomId);

    /**
     * 更新传感器状态
     */
    boolean updateSensorStatus(Long id, String status);
}
