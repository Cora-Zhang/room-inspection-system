package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.EfficiencyCheckOrder;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 效率核查工单Service
 */
public interface EfficiencyCheckOrderService extends IService<EfficiencyCheckOrder> {

    /**
     * 根据空调ID查询工单列表
     */
    List<EfficiencyCheckOrder> listByAcId(Long acId);

    /**
     * 根据机房ID查询工单列表
     */
    List<EfficiencyCheckOrder> listByRoomId(Long roomId);

    /**
     * 根据状态查询工单列表
     */
    List<EfficiencyCheckOrder> listByStatus(Integer status);

    /**
     * 指派工单
     */
    void assignOrder(Long orderId, String assignee);

    /**
     * 开始处理工单
     */
    void startOrder(Long orderId);

    /**
     * 完成工单
     */
    void completeOrder(Long orderId, String handleResult, String handleDescription, String photos);

    /**
     * 取消工单
     */
    void cancelOrder(Long orderId, String reason);

    /**
     * 检查是否有未处理工单
     */
    boolean hasOpenOrder(Long acId, Integer orderType);

    /**
     * 获取工单统计数据
     */
    java.util.Map<String, Object> getStatistics(Long roomId);
}
