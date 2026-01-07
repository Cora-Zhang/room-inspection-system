package com.roominspection.backend.controller;

import com.roominspection.backend.entity.EnergyEfficiencyOrder;
import com.roominspection.backend.service.EnergyEfficiencyOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 能效优化控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/energy-efficiency")
public class EnergyEfficiencyController {

    @Autowired
    private EnergyEfficiencyOrderService energyEfficiencyOrderService;

    // ==================== 工单管理 ====================

    /**
     * 创建工单
     */
    @PostMapping("/order")
    public Map<String, Object> createOrder(@RequestBody EnergyEfficiencyOrder order) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long id = energyEfficiencyOrderService.createOrder(order);
            result.put("success", true);
            result.put("message", "创建工单成功");
            result.put("data", id);
        } catch (Exception e) {
            log.error("创建工单失败", e);
            result.put("success", false);
            result.put("message", "创建工单失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新工单
     */
    @PutMapping("/order")
    public Map<String, Object> updateOrder(@RequestBody EnergyEfficiencyOrder order) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = energyEfficiencyOrderService.updateOrder(order);
            result.put("success", success);
            result.put("message", success ? "更新工单成功" : "更新工单失败");
        } catch (Exception e) {
            log.error("更新工单失败", e);
            result.put("success", false);
            result.put("message", "更新工单失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除工单
     */
    @DeleteMapping("/order/{id}")
    public Map<String, Object> deleteOrder(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = energyEfficiencyOrderService.deleteOrder(id);
            result.put("success", success);
            result.put("message", success ? "删除工单成功" : "删除工单失败");
        } catch (Exception e) {
            log.error("删除工单失败", e);
            result.put("success", false);
            result.put("message", "删除工单失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 批量删除工单
     */
    @DeleteMapping("/order/batch")
    public Map<String, Object> batchDeleteOrders(@RequestBody List<Long> ids) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = energyEfficiencyOrderService.batchDeleteOrders(ids);
            result.put("success", success);
            result.put("message", success ? "批量删除工单成功" : "批量删除工单失败");
        } catch (Exception e) {
            log.error("批量删除工单失败", e);
            result.put("success", false);
            result.put("message", "批量删除工单失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取工单详情
     */
    @GetMapping("/order/{id}")
    public Map<String, Object> getOrderDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            EnergyEfficiencyOrder order = energyEfficiencyOrderService.getOrderDetail(id);
            result.put("success", true);
            result.put("data", order);
        } catch (Exception e) {
            log.error("获取工单详情失败", e);
            result.put("success", false);
            result.put("message", "获取工单详情失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 查询工单列表
     */
    @GetMapping("/order/list")
    public Map<String, Object> getOrderList(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String orderType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long assigneeId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("roomId", roomId);
            params.put("orderType", orderType);
            params.put("status", status);
            params.put("priority", priority);
            params.put("assigneeId", assigneeId);
            
            List<EnergyEfficiencyOrder> list = energyEfficiencyOrderService.getOrderList(params);
            result.put("success", true);
            result.put("data", list);
        } catch (Exception e) {
            log.error("查询工单列表失败", e);
            result.put("success", false);
            result.put("message", "查询工单列表失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 指派工单
     */
    @PostMapping("/order/{orderId}/assign")
    public Map<String, Object> assignOrder(
            @PathVariable Long orderId,
            @RequestParam Long assigneeId,
            @RequestParam String assigneeName) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = energyEfficiencyOrderService.assignOrder(orderId, assigneeId, assigneeName);
            result.put("success", success);
            result.put("message", success ? "指派工单成功" : "指派工单失败");
        } catch (Exception e) {
            log.error("指派工单失败", e);
            result.put("success", false);
            result.put("message", "指派工单失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 完成工单
     */
    @PostMapping("/order/{orderId}/complete")
    public Map<String, Object> completeOrder(
            @PathVariable Long orderId,
            @RequestParam String result,
            @RequestParam(required = false) String suggestion) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = energyEfficiencyOrderService.completeOrder(orderId, result, suggestion);
            response.put("success", success);
            response.put("message", success ? "完成工单成功" : "完成工单失败");
        } catch (Exception e) {
            log.error("完成工单失败", e);
            response.put("success", false);
            response.put("message", "完成工单失败: " + e.getMessage());
        }
        return response;
    }

    /**
     * 关闭工单
     */
    @PostMapping("/order/{orderId}/close")
    public Map<String, Object> closeOrder(@PathVariable Long orderId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = energyEfficiencyOrderService.closeOrder(orderId);
            result.put("success", success);
            result.put("message", success ? "关闭工单成功" : "关闭工单失败");
        } catch (Exception e) {
            log.error("关闭工单失败", e);
            result.put("success", false);
            result.put("message", "关闭工单失败: " + e.getMessage());
        }
        return result;
    }

    // ==================== 自动触发 ====================

    /**
     * 手动触发高温工单
     */
    @PostMapping("/auto/trigger/high-temp")
    public Map<String, Object> triggerHighTempOrder(
            @RequestParam Long roomId,
            @RequestParam(required = false) Long abnormalAreaId,
            @RequestParam(required = false) String abnormalArea,
            @RequestParam Double triggerValue) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long orderId = energyEfficiencyOrderService.triggerHighTempOrder(roomId, abnormalAreaId, abnormalArea, triggerValue);
            result.put("success", true);
            result.put("message", "触发高温工单成功");
            result.put("data", orderId);
        } catch (Exception e) {
            log.error("触发高温工单失败", e);
            result.put("success", false);
            result.put("message", "触发高温工单失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 手动触发湿度预警工单
     */
    @PostMapping("/auto/trigger/humidity-rise")
    public Map<String, Object> triggerHumidityRiseOrder(
            @RequestParam Long roomId,
            @RequestParam(required = false) Long abnormalAreaId,
            @RequestParam(required = false) String abnormalArea,
            @RequestParam Double triggerValue) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long orderId = energyEfficiencyOrderService.triggerHumidityRiseOrder(roomId, abnormalAreaId, abnormalArea, triggerValue);
            result.put("success", true);
            result.put("message", "触发湿度预警工单成功");
            result.put("data", orderId);
        } catch (Exception e) {
            log.error("触发湿度预警工单失败", e);
            result.put("success", false);
            result.put("message", "触发湿度预警工单失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 自动检查并触发工单
     */
    @PostMapping("/auto/check-trigger")
    public Map<String, Object> autoCheckAndTriggerOrders() {
        Map<String, Object> result = new HashMap<>();
        try {
            energyEfficiencyOrderService.autoCheckAndTriggerOrders();
            result.put("success", true);
            result.put("message", "自动检查并触发工单完成");
        } catch (Exception e) {
            log.error("自动检查并触发工单失败", e);
            result.put("success", false);
            result.put("message", "自动检查并触发工单失败: " + e.getMessage());
        }
        return result;
    }

    // ==================== 统计分析 ====================

    /**
     * 获取工单统计
     */
    @GetMapping("/order/statistics")
    public Map<String, Object> getOrderStatistics(@RequestParam(required = false) Long roomId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> statistics = energyEfficiencyOrderService.getOrderStatistics(roomId);
            result.put("success", true);
            result.put("data", statistics);
        } catch (Exception e) {
            log.error("获取工单统计失败", e);
            result.put("success", false);
            result.put("message", "获取工单统计失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取工单趋势
     */
    @GetMapping("/order/trend")
    public Map<String, Object> getOrderTrend(
            @RequestParam(required = false) Long roomId,
            @RequestParam(defaultValue = "7") Integer days) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> trend = energyEfficiencyOrderService.getOrderTrend(roomId, days);
            result.put("success", true);
            result.put("data", trend);
        } catch (Exception e) {
            log.error("获取工单趋势失败", e);
            result.put("success", false);
            result.put("message", "获取工单趋势失败: " + e.getMessage());
        }
        return result;
    }
}
