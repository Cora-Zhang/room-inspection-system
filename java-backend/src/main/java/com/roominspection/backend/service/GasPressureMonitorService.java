package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.GasPressureMonitor;
import java.util.List;
import java.util.Map;

/**
 * 气体压力监控Service
 */
public interface GasPressureMonitorService extends IService<GasPressureMonitor> {

    /**
     * 根据机房ID查询气体压力监控列表
     */
    List<GasPressureMonitor> listByRoomId(Long roomId);

    /**
     * 批量采集气体压力数据
     */
    void batchCollectData();

    /**
     * 手动触发指定系统的数据采集
     */
    void collectData(Long systemId);

    /**
     * 更新气体压力数据
     */
    void updatePressure(Long systemId, Double pressure);

    /**
     * 检查气体压力是否异常，自动触发确认工单
     */
    void checkPressureAbnormal(Long systemId);

    /**
     * 获取气体压力统计数据
     */
    Map<String, Object> getStatistics(Long roomId);
}
