package com.roominspection.backend.service;

import com.roominspection.backend.entity.WorkOrder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 异步工单处理服务接口
 * 用于优化工单处理性能，支持异步操作和缓存
 */
public interface AsyncWorkOrderService {

    /**
     * 异步创建工单
     *
     * @param workOrder 工单对象
     * @return CompletableFuture
     */
    CompletableFuture<Boolean> createWorkOrderAsync(WorkOrder workOrder);

    /**
     * 异步批量创建工单
     *
     * @param workOrders 工单列表
     * @return CompletableFuture
     */
    CompletableFuture<Integer> createBatchWorkOrdersAsync(List<WorkOrder> workOrders);

    /**
     * 异步指派工单
     *
     * @param id           工单ID
     * @param assignedTo   被指派人ID
     * @param assignedToName 被指派人姓名
     * @param priority     优先级
     * @return CompletableFuture
     */
    CompletableFuture<Boolean> assignWorkOrderAsync(String id, String assignedTo, String assignedToName, String priority);

    /**
     * 异步开始工单
     *
     * @param id       工单ID
     * @param ownerId  负责人ID
     * @param ownerName 负责人姓名
     * @return CompletableFuture
     */
    CompletableFuture<Boolean> startWorkOrderAsync(String id, String ownerId, String ownerName);

    /**
     * 异步完成工单
     *
     * @param id                工单ID
     * @param handleResult      处理结果
     * @param handleDescription 处理描述
     * @param duration          处理时长（小时）
     * @return CompletableFuture
     */
    CompletableFuture<Boolean> completeWorkOrderAsync(String id, String handleResult,
                                                       String handleDescription, Double duration);

    /**
     * 异步自动创建告警工单
     *
     * @param alarmId 告警ID
     * @return CompletableFuture
     */
    CompletableFuture<WorkOrder> autoCreateFromAlarmAsync(String alarmId);

    /**
     * 获取缓存统计
     *
     * @return 缓存统计
     */
    Map<String, Object> getCacheStatistics();

    /**
     * 清空工单缓存
     */
    void clearWorkOrderCache();

    /**
     * 检查超时工单
     *
     * @return 超时工单数量
     */
    int checkOverdueWorkOrders();
}
