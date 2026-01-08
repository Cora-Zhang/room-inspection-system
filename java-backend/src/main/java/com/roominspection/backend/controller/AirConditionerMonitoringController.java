package com.roominspection.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.AirConditioner;
import com.roominspection.backend.entity.AirConditionerData;
import com.roominspection.backend.entity.EfficiencyCheckOrder;
import com.roominspection.backend.service.AirConditionerDataService;
import com.roominspection.backend.service.AirConditionerService;
import com.roominspection.backend.service.EfficiencyCheckOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 精密空调监控Controller
 */
@RestController
@RequestMapping("/api/air-conditioner")
@CrossOrigin
public class AirConditionerMonitoringController {

    @Autowired
    private AirConditionerService acService;

    @Autowired
    private AirConditionerDataService acDataService;

    @Autowired
    private EfficiencyCheckOrderService efficiencyCheckOrderService;

    /**
     * 查询空调列表
     */
    @GetMapping("/list")
    public Result<IPage<AirConditioner>> list(
            @RequestParam(required = false) Long roomId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<AirConditioner> page = new Page<>(current, size);
        IPage<AirConditioner> result = acService.page(page, null);
        return Result.success(result);
    }

    /**
     * 根据机房ID查询空调列表
     */
    @GetMapping("/listByRoom")
    public Result<List<AirConditioner>> listByRoom(@RequestParam Long roomId) {
        List<AirConditioner> list = acService.listByRoomId(roomId);
        return Result.success(list);
    }

    /**
     * 获取空调详情
     */
    @GetMapping("/detail")
    public Result<AirConditioner> detail(@RequestParam Long id) {
        AirConditioner ac = acService.getById(id);
        return Result.success(ac);
    }

    /**
     * 新增空调
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody AirConditioner ac) {
        acService.save(ac);
        return Result.success();
    }

    /**
     * 更新空调
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody AirConditioner ac) {
        acService.updateById(ac);
        return Result.success();
    }

    /**
     * 删除空调
     */
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        acService.removeById(id);
        return Result.success();
    }

    /**
     * 批量采集空调数据
     */
    @PostMapping("/batchCollect")
    public Result<Void> batchCollect() {
        acService.batchCollectData();
        return Result.success();
    }

    /**
     * 手动触发数据采集
     */
    @PostMapping("/collect")
    public Result<Void> collect(@RequestParam Long acId) {
        acService.collectData(acId);
        return Result.success();
    }

    /**
     * 获取空调最新数据
     */
    @GetMapping("/latestData")
    public Result<AirConditionerData> getLatestData(@RequestParam Long acId) {
        AirConditionerData data = acDataService.getLatestByAcId(acId);
        return Result.success(data);
    }

    /**
     * 获取空调历史数据
     */
    @GetMapping("/historyData")
    public Result<List<AirConditionerData>> getHistoryData(
            @RequestParam Long acId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<AirConditionerData> list = acDataService.listByAcId(acId, startTime, endTime);
        return Result.success(list);
    }

    /**
     * 获取温差异常数据
     */
    @GetMapping("/abnormalData")
    public Result<List<AirConditionerData>> getAbnormalData(
            @RequestParam Long acId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<AirConditionerData> list = acDataService.listAbnormalData(acId, startTime, endTime);
        return Result.success(list);
    }

    /**
     * 获取温度趋势数据
     */
    @GetMapping("/temperatureTrend")
    public Result<List<Map<String, Object>>> getTemperatureTrend(
            @RequestParam Long acId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<Map<String, Object>> trend = acDataService.getTemperatureTrend(acId, startTime, endTime);
        return Result.success(trend);
    }

    /**
     * 获取功率趋势数据
     */
    @GetMapping("/powerTrend")
    public Result<List<Map<String, Object>>> getPowerTrend(
            @RequestParam Long acId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<Map<String, Object>> trend = acDataService.getPowerTrend(acId, startTime, endTime);
        return Result.success(trend);
    }

    /**
     * 获取空调统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(@RequestParam(required = false) Long roomId) {
        Map<String, Object> stats = acService.getStatistics(roomId);
        return Result.success(stats);
    }

    /**
     * 查询效率核查工单列表
     */
    @GetMapping("/efficiencyOrders/list")
    public Result<IPage<EfficiencyCheckOrder>> listEfficiencyOrders(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long acId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<EfficiencyCheckOrder> page = new Page<>(current, size);

        // 根据条件查询
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EfficiencyCheckOrder> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(roomId != null, EfficiencyCheckOrder::getRoomId, roomId);
        wrapper.eq(acId != null, EfficiencyCheckOrder::getAcId, acId);
        wrapper.eq(status != null, EfficiencyCheckOrder::getStatus, status);
        wrapper.orderByDesc(EfficiencyCheckOrder::getCreateTime);

        IPage<EfficiencyCheckOrder> result = efficiencyCheckOrderService.page(page, wrapper);
        return Result.success(result);
    }

    /**
     * 指派工单
     */
    @PostMapping("/efficiencyOrders/assign")
    public Result<Void> assignOrder(@RequestParam Long orderId, @RequestParam String assignee) {
        efficiencyCheckOrderService.assignOrder(orderId, assignee);
        return Result.success();
    }

    /**
     * 开始处理工单
     */
    @PostMapping("/efficiencyOrders/start")
    public Result<Void> startOrder(@RequestParam Long orderId) {
        efficiencyCheckOrderService.startOrder(orderId);
        return Result.success();
    }

    /**
     * 完成工单
     */
    @PostMapping("/efficiencyOrders/complete")
    public Result<Void> completeOrder(
            @RequestParam Long orderId,
            @RequestParam String handleResult,
            @RequestParam(required = false) String handleDescription,
            @RequestParam(required = false) String photos) {
        efficiencyCheckOrderService.completeOrder(orderId, handleResult, handleDescription, photos);
        return Result.success();
    }

    /**
     * 取消工单
     */
    @PostMapping("/efficiencyOrders/cancel")
    public Result<Void> cancelOrder(@RequestParam Long orderId, @RequestParam String reason) {
        efficiencyCheckOrderService.cancelOrder(orderId, reason);
        return Result.success();
    }

    /**
     * 获取工单统计数据
     */
    @GetMapping("/efficiencyOrders/statistics")
    public Result<Map<String, Object>> getEfficiencyOrderStatistics(
            @RequestParam(required = false) Long roomId) {
        Map<String, Object> stats = efficiencyCheckOrderService.getStatistics(roomId);
        return Result.success(stats);
    }
}
