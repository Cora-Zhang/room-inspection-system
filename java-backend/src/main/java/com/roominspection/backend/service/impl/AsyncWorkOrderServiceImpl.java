package com.roominspection.backend.service.impl;

import com.roominspection.backend.entity.WorkOrder;
import com.roominspection.backend.service.AsyncWorkOrderService;
import com.roominspection.backend.service.WorkOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 异步工单处理服务实现
 * 优化工单处理性能，支持异步操作和缓存
 */
@Slf4j
@Service
public class AsyncWorkOrderServiceImpl implements AsyncWorkOrderService {

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private CacheManager cacheManager;

    /**
     * 异步创建工单
     */
    @Override
    @Async("taskExecutor")
    public CompletableFuture<Boolean> createWorkOrderAsync(WorkOrder workOrder) {
        try {
            long startTime = System.currentTimeMillis();
            boolean result = workOrderService.createWorkOrder(workOrder);
            long duration = System.currentTimeMillis() - startTime;

            log.info("异步创建工单完成: orderId={}, duration={}ms, success={}",
                    workOrder.getId(), duration, result);

            // 清除相关缓存
            clearWorkOrderCache();

            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("异步创建工单失败", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 异步批量创建工单
     */
    @Override
    @Async("taskExecutor")
    public CompletableFuture<Integer> createBatchWorkOrdersAsync(List<WorkOrder> workOrders) {
        long startTime = System.currentTimeMillis();
        int successCount = 0;

        try {
            for (WorkOrder workOrder : workOrders) {
                try {
                    boolean result = workOrderService.createWorkOrder(workOrder);
                    if (result) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("创建工单失败: title={}", workOrder.getTitle(), e);
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("批量创建工单完成: total={}, success={}, duration={}ms",
                    workOrders.size(), successCount, duration);

            // 清除相关缓存
            clearWorkOrderCache();

            return CompletableFuture.completedFuture(successCount);

        } catch (Exception e) {
            log.error("批量创建工单失败", e);
            return CompletableFuture.completedFuture(successCount);
        }
    }

    /**
     * 异步指派工单
     */
    @Override
    @Async("taskExecutor")
    public CompletableFuture<Boolean> assignWorkOrderAsync(String id, String assignedTo, String assignedToName, String priority) {
        try {
            long startTime = System.currentTimeMillis();
            boolean result = workOrderService.assignWorkOrder(id, assignedTo, assignedToName, priority);
            long duration = System.currentTimeMillis() - startTime;

            log.info("异步指派工单完成: orderId={}, assignedTo={}, duration={}ms, success={}",
                    id, assignedTo, duration, result);

            // 清除相关缓存
            clearCacheByOrderId(id);

            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("异步指派工单失败: orderId={}", id, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 异步开始工单
     */
    @Override
    @Async("taskExecutor")
    public CompletableFuture<Boolean> startWorkOrderAsync(String id, String ownerId, String ownerName) {
        try {
            long startTime = System.currentTimeMillis();
            boolean result = workOrderService.startWorkOrder(id, ownerId, ownerName);
            long duration = System.currentTimeMillis() - startTime;

            log.info("异步开始工单完成: orderId={}, ownerId={}, duration={}ms, success={}",
                    id, ownerId, duration, result);

            // 清除相关缓存
            clearCacheByOrderId(id);

            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("异步开始工单失败: orderId={}", id, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 异步完成工单
     */
    @Override
    @Async("taskExecutor")
    public CompletableFuture<Boolean> completeWorkOrderAsync(String id, String handleResult,
                                                              String handleDescription, Double duration) {
        try {
            long startTime = System.currentTimeMillis();
            boolean result = workOrderService.completeWorkOrder(id, handleResult, handleDescription, duration);
            long endTime = System.currentTimeMillis() - startTime;

            log.info("异步完成工单完成: orderId={}, handleResult={}, duration={}ms, success={}",
                    id, handleResult, endTime, result);

            // 清除相关缓存
            clearCacheByOrderId(id);

            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("异步完成工单失败: orderId={}", id, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 异步自动创建告警工单
     */
    @Override
    @Async("taskExecutor")
    public CompletableFuture<WorkOrder> autoCreateFromAlarmAsync(String alarmId) {
        try {
            long startTime = System.currentTimeMillis();
            WorkOrder workOrder = workOrderService.autoCreateFromAlarm(alarmId);
            long duration = System.currentTimeMillis() - startTime;

            log.info("异步自动创建告警工单完成: alarmId={}, orderId={}, duration={}ms",
                    alarmId, workOrder.getId(), duration);

            // 清除相关缓存
            clearWorkOrderCache();

            return CompletableFuture.completedFuture(workOrder);
        } catch (Exception e) {
            log.error("异步自动创建告警工单失败: alarmId={}", alarmId, e);
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * 获取缓存统计
     */
    @Override
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        Cache workOrderCache = cacheManager.getCache("workOrder");
        if (workOrderCache != null) {
            // Spring Cache不直接提供获取缓存大小的方法
            // 这里记录缓存是否可用
            statistics.put("workOrderCache", "enabled");
        } else {
            statistics.put("workOrderCache", "disabled");
        }

        statistics.put("cacheManager", cacheManager.getClass().getSimpleName());
        statistics.put("timestamp", LocalDateTime.now());

        return statistics;
    }

    /**
     * 清空工单缓存
     */
    @Override
    public void clearWorkOrderCache() {
        Cache workOrderCache = cacheManager.getCache("workOrder");
        if (workOrderCache != null) {
            workOrderCache.clear();
            log.debug("已清空工单缓存");
        }
    }

    /**
     * 清除指定工单的缓存
     */
    private void clearCacheByOrderId(String orderId) {
        Cache workOrderCache = cacheManager.getCache("workOrder");
        if (workOrderCache != null) {
            workOrderCache.evict(orderId);
            log.debug("已清除工单缓存: orderId={}", orderId);
        }
    }

    /**
     * 检查超时工单
     */
    @Override
    public int checkOverdueWorkOrders() {
        try {
            // 查询所有未完成的工单
            List<WorkOrder> pendingOrders = workOrderService.listByTimeRange(
                    LocalDateTime.now().minusDays(30), // 查询30天内的工单
                    LocalDateTime.now()
            );

            int overdueCount = 0;
            for (WorkOrder workOrder : pendingOrders) {
                if (!"COMPLETED".equals(workOrder.getStatus()) &&
                        !"CANCELLED".equals(workOrder.getStatus()) &&
                        !"CLOSED".equals(workOrder.getStatus())) {

                    // 检查是否超时
                    if (isOverdue(workOrder)) {
                        // 标记为超时
                        workOrder.setIsOverdue(true);
                        workOrderService.updateById(workOrder);
                        overdueCount++;

                        log.warn("工单已超时: orderId={}, title={}, priority={}",
                                workOrder.getId(), workOrder.getTitle(), workOrder.getPriority());
                    }
                }
            }

            return overdueCount;
        } catch (Exception e) {
            log.error("检查超时工单失败", e);
            return 0;
        }
    }

    /**
     * 判断工单是否超时
     */
    private boolean isOverdue(WorkOrder workOrder) {
        // 根据优先级判断是否超时
        // 紧急工单：4小时
        // 高优先级：24小时
        // 中优先级：48小时
        // 低优先级：72小时

        if (workOrder.getAssignedAt() == null) {
            return false;
        }

        long assignedTime = workOrder.getAssignedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        long currentTime = System.currentTimeMillis();
        long elapsedHours = TimeUnit.MILLISECONDS.toHours(currentTime - assignedTime);

        int maxHours;
        switch (workOrder.getPriority()) {
            case "URGENT":
                maxHours = 4;
                break;
            case "HIGH":
                maxHours = 24;
                break;
            case "MEDIUM":
                maxHours = 48;
                break;
            case "LOW":
                maxHours = 72;
                break;
            default:
                maxHours = 48;
        }

        return elapsedHours > maxHours;
    }
}
