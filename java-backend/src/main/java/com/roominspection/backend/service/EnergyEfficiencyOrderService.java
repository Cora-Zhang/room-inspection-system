package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.EnergyEfficiencyOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 能效优化工单服务接口
 */
public interface EnergyEfficiencyOrderService extends IService<EnergyEfficiencyOrder> {

    /**
     * 创建能效工单
     */
    Long createOrder(EnergyEfficiencyOrder order);

    /**
     * 更新工单
     */
    boolean updateOrder(EnergyEfficiencyOrder order);

    /**
     * 删除工单
     */
    boolean deleteOrder(Long id);

    /**
     * 批量删除工单
     */
    boolean batchDeleteOrders(List<Long> ids);

    /**
     * 获取工单详情
     */
    EnergyEfficiencyOrder getOrderDetail(Long id);

    /**
     * 查询工单列表
     */
    List<EnergyEfficiencyOrder> getOrderList(Map<String, Object> params);

    /**
     * 指派工单
     */
    boolean assignOrder(Long orderId, Long assigneeId, String assigneeName);

    /**
     * 完成工单
     */
    boolean completeOrder(Long orderId, String result, String suggestion);

    /**
     * 关闭工单
     */
    boolean closeOrder(Long orderId);

    /**
     * 触发高温工单
     */
    Long triggerHighTempOrder(Long roomId, Long abnormalAreaId, String abnormalArea, Double triggerValue);

    /**
     * 触发湿度预警工单
     */
    Long triggerHumidityRiseOrder(Long roomId, Long abnormalAreaId, String abnormalArea, Double triggerValue);

    /**
     * 触发冷通道检查工单
     */
    Long triggerColdAirCheckOrder(Long roomId, Long abnormalAreaId, String abnormalArea);

    /**
     * 触发风道检查工单
     */
    Long triggerAirDuctCheckOrder(Long roomId, Long abnormalAreaId, String abnormalArea);

    /**
     * 自动检查并触发工单
     */
    void autoCheckAndTriggerOrders();

    /**
     * 获取工单统计
     */
    Map<String, Object> getOrderStatistics(Long roomId);

    /**
     * 获取工单趋势
     */
    List<Map<String, Object>> getOrderTrend(Long roomId, Integer days);
}
