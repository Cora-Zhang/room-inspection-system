package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.EnvironmentData;
import com.roominspection.backend.entity.HeatmapData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 环境数据服务接口
 */
public interface EnvironmentDataService extends IService<EnvironmentData> {

    /**
     * 保存环境数据
     */
    boolean saveEnvironmentData(EnvironmentData data);

    /**
     * 批量保存环境数据
     */
    boolean saveBatchEnvironmentData(List<EnvironmentData> dataList);

    /**
     * 根据传感器ID获取最新数据
     */
    List<EnvironmentData> getLatestDataBySensorId(Long sensorId);

    /**
     * 根据机房ID获取最新数据
     */
    List<EnvironmentData> getLatestDataByRoomId(Long roomId);

    /**
     * 根据数据类型查询历史数据
     */
    List<EnvironmentData> getDataByType(Long roomId, String dataType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取环境数据趋势
     */
    List<Map<String, Object>> getDataTrend(Long roomId, String dataType, Integer hours);

    /**
     * 获取告警数据列表
     */
    List<EnvironmentData> getAlarmDataList(Long roomId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 生成温湿度热力图数据
     */
    List<HeatmapData> generateHeatmapData(Long roomId, String heatmapType, LocalDateTime dataTime);

    /**
     * 识别异常区域
     */
    List<Map<String, Object>> identifyAbnormalAreas(Long roomId, String heatmapType, LocalDateTime dataTime);

    /**
     * 清理历史数据
     */
    int cleanHistoryData(LocalDateTime beforeTime);

    /**
     * 获取数据统计
     */
    Map<String, Object> getDataStatistics(Long roomId, LocalDateTime startTime, LocalDateTime endTime);
}
