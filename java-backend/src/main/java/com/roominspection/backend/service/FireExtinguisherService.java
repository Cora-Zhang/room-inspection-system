package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.FireExtinguisher;
import java.util.List;
import java.util.Map;

/**
 * 灭火器管理Service
 */
public interface FireExtinguisherService extends IService<FireExtinguisher> {

    /**
     * 根据机房ID查询灭火器列表
     */
    List<FireExtinguisher> listByRoomId(Long roomId);

    /**
     * 批量采集灭火器数据（压力、重量）
     */
    void batchCollectData();

    /**
     * 手动触发指定灭火器的数据采集
     */
    void collectData(Long extinguisherId);

    /**
     * 更新灭火器压力数据
     */
    void updatePressure(Long extinguisherId, Double pressure);

    /**
     * 更新灭火器重量数据
     */
    void updateWeight(Long extinguisherId, Double weight);

    /**
     * 检查灭火器压力是否异常，自动触发紧急确认工单
     */
    void checkPressureAbnormal(Long extinguisherId);

    /**
     * 生成月度检查工单（每月首日自动生成）
     */
    void generateMonthlyCheckOrder();

    /**
     * 获取灭火器统计数据
     */
    Map<String, Object> getStatistics(Long roomId);
}
