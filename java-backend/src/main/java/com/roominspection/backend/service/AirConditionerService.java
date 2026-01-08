package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.AirConditioner;
import java.util.List;
import java.util.Map;

/**
 * 精密空调管理Service
 */
public interface AirConditionerService extends IService<AirConditioner> {

    /**
     * 根据机房ID查询空调列表
     */
    List<AirConditioner> listByRoomId(Long roomId);

    /**
     * 批量采集空调数据
     */
    void batchCollectData();

    /**
     * 手动触发指定空调的数据采集
     */
    void collectData(Long acId);

    /**
     * 获取空调统计数据
     */
    Map<String, Object> getStatistics(Long roomId);

    /**
     * 更新空调运行时长
     */
    void updateRuntime(Long acId, Double additionalHours);

    /**
     * 检查压缩机运行时长是否达到保养阈值，自动触发保养工单
     */
    void checkMaintenanceThreshold(Long acId);
}
