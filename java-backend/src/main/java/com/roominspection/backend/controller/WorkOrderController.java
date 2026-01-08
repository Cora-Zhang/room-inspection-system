package com.roominspection.backend.controller;

import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.WorkOrder;
import com.roominspection.backend.entity.WorkOrderFlow;
import com.roominspection.backend.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 工单管理Controller
 */
@RestController
@RequestMapping("/api/work-orders")
@CrossOrigin
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;

    /**
     * 分页查询工单列表
     */
    @GetMapping("/list")
    public Result<List<WorkOrder>> list(
            @RequestParam(required = false) String roomId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String ownerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<WorkOrder> workOrders;

        if (startTime != null && endTime != null) {
            workOrders = workOrderService.listByTimeRange(startTime, endTime);
        } else if (status != null) {
            workOrders = workOrderService.listByStatus(status);
        } else if (type != null) {
            workOrders = workOrderService.listByType(type);
        } else if (ownerId != null) {
            workOrders = workOrderService.listByOwnerId(ownerId);
        } else {
            // 默认查询待处理工单
            workOrders = workOrderService.getPendingWorkOrders();
        }

        return Result.success(workOrders);
    }

    /**
     * 获取超时工单列表
     */
    @GetMapping("/overdue")
    public Result<List<WorkOrder>> getOverdue() {
        List<WorkOrder> workOrders = workOrderService.getOverdueWorkOrders();
        return Result.success(workOrders);
    }

    /**
     * 获取待处理工单列表
     */
    @GetMapping("/pending")
    public Result<List<WorkOrder>> getPending() {
        List<WorkOrder> workOrders = workOrderService.getPendingWorkOrders();
        return Result.success(workOrders);
    }

    /**
     * 获取紧急工单列表
     */
    @GetMapping("/urgent")
    public Result<List<WorkOrder>> getUrgent() {
        List<WorkOrder> workOrders = workOrderService.getUrgentWorkOrders();
        return Result.success(workOrders);
    }

    /**
     * 创建工单
     */
    @PostMapping("/create")
    public Result<Boolean> createWorkOrder(@RequestBody WorkOrder workOrder) {
        boolean result = workOrderService.createWorkOrder(workOrder);
        return Result.success(result);
    }

    /**
     * 从告警自动创建工单
     */
    @PostMapping("/auto-create")
    public Result<WorkOrder> autoCreateFromAlarm(@RequestParam String alarmId) {
        WorkOrder workOrder = workOrderService.autoCreateFromAlarm(alarmId);
        return Result.success(workOrder);
    }

    /**
     * 指派工单
     */
    @PutMapping("/{id}/assign")
    public Result<Boolean> assignWorkOrder(
            @PathVariable String id,
            @RequestParam String assignedTo,
            @RequestParam String assignedToName,
            @RequestParam(required = false) String priority) {
        boolean result = workOrderService.assignWorkOrder(id, assignedTo, assignedToName, priority);
        return Result.success(result);
    }

    /**
     * 开始处理工单
     */
    @PutMapping("/{id}/start")
    public Result<Boolean> startWorkOrder(
            @PathVariable String id,
            @RequestParam String ownerId,
            @RequestParam String ownerName) {
        boolean result = workOrderService.startWorkOrder(id, ownerId, ownerName);
        return Result.success(result);
    }

    /**
     * 完成工单
     */
    @PutMapping("/{id}/complete")
    public Result<Boolean> completeWorkOrder(
            @PathVariable String id,
            @RequestParam String handleResult,
            @RequestParam(required = false) String handleDescription,
            @RequestParam(required = false) Double duration) {
        boolean result = workOrderService.completeWorkOrder(id, handleResult, handleDescription, duration);
        return Result.success(result);
    }

    /**
     * 取消工单
     */
    @PutMapping("/{id}/cancel")
    public Result<Boolean> cancelWorkOrder(
            @PathVariable String id,
            @RequestParam String cancelReason) {
        boolean result = workOrderService.cancelWorkOrder(id, cancelReason);
        return Result.success(result);
    }

    /**
     * 关闭工单
     */
    @PutMapping("/{id}/close")
    public Result<Boolean> closeWorkOrder(
            @PathVariable String id,
            @RequestParam String closeReason,
            @RequestParam String closedBy,
            @RequestParam String closedByName) {
        boolean result = workOrderService.closeWorkOrder(id, closeReason, closedBy, closedByName);
        return Result.success(result);
    }

    /**
     * 获取工单流转记录
     */
    @GetMapping("/{id}/flows")
    public Result<List<WorkOrderFlow>> getWorkOrderFlows(@PathVariable String id) {
        List<WorkOrderFlow> flows = workOrderService.getWorkOrderFlows(id);
        return Result.success(flows);
    }

    /**
     * 获取工单统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        Map<String, Object> stats = workOrderService.getStatistics(startTime, endTime);
        return Result.success(stats);
    }

    /**
     * 按状态统计工单数量
     */
    @GetMapping("/count/status")
    public Result<List<Map<String, Object>>> countByStatus(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        List<Map<String, Object>> count = workOrderService.countByStatus(startTime, endTime);
        return Result.success(count);
    }

    /**
     * 按类型统计工单数量
     */
    @GetMapping("/count/type")
    public Result<List<Map<String, Object>>> countByType(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        List<Map<String, Object>> count = workOrderService.countByType(startTime, endTime);
        return Result.success(count);
    }

    /**
     * 按优先级统计工单数量
     */
    @GetMapping("/count/priority")
    public Result<List<Map<String, Object>>> countByPriority(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        List<Map<String, Object>> count = workOrderService.countByPriority(startTime, endTime);
        return Result.success(count);
    }

    /**
     * 检查工单超时
     */
    @PostMapping("/check-overdue")
    public Result<Integer> checkOverdue() {
        int count = workOrderService.checkWorkOrderOverdue();
        return Result.success(count);
    }
}
