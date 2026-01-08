package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.AirConditionerData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 空调运行数据Service
 */
public interface AirConditionerDataService extends IService<AirConditionerData> {

    /**
     * 根据空调ID查询最新数据
     */
    AirConditionerData getLatestByAcId(Long acId);

    /**
     * 根据空调ID查询历史数据
     */
    List<AirConditionerData> listByAcId(Long acId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取温差异常数据
     */
    List<AirConditionerData> listAbnormalData(Long acId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取空调温度趋势数据
     */
    List<Map<String, Object>> getTemperatureTrend(Long acId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取空调功率趋势数据
     */
    List<Map<String, Object>> getPowerTrend(Long acId, LocalDateTime startTime, LocalDateTime endTime);
}
