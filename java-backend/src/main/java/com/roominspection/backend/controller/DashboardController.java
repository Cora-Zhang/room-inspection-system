package com.roominspection.backend.controller;

import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.RoomFloorPlan;
import com.roominspection.backend.service.RoomFloorPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 综合巡检看板Controller
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin
public class DashboardController {

    @Autowired
    private RoomFloorPlanService roomFloorPlanService;

    /**
     * 获取机房平面图列表
     */
    @GetMapping("/floor-plans")
    public Result<List<RoomFloorPlan>> getFloorPlans(@RequestParam String roomId) {
        List<RoomFloorPlan> floorPlans = roomFloorPlanService.listByRoomId(roomId);
        return Result.success(floorPlans);
    }

    /**
     * 获取主平面图
     */
    @GetMapping("/main-floor-plan")
    public Result<RoomFloorPlan> getMainFloorPlan(@RequestParam String roomId) {
        RoomFloorPlan floorPlan = roomFloorPlanService.getMainFloorPlan(roomId);
        return Result.success(floorPlan);
    }

    /**
     * 创建机房平面图
     */
    @PostMapping("/floor-plan")
    public Result<Boolean> createFloorPlan(@RequestBody RoomFloorPlan floorPlan) {
        boolean result = roomFloorPlanService.createFloorPlan(floorPlan);
        return Result.success(result);
    }

    /**
     * 更新机房平面图
     */
    @PutMapping("/floor-plan")
    public Result<Boolean> updateFloorPlan(@RequestBody RoomFloorPlan floorPlan) {
        boolean result = roomFloorPlanService.updateFloorPlan(floorPlan);
        return Result.success(result);
    }

    /**
     * 删除机房平面图
     */
    @DeleteMapping("/floor-plan/{id}")
    public Result<Boolean> deleteFloorPlan(@PathVariable String id) {
        boolean result = roomFloorPlanService.deleteFloorPlan(id);
        return Result.success(result);
    }

    /**
     * 设置主平面图
     */
    @PutMapping("/floor-plan/{id}/main")
    public Result<Boolean> setAsMain(@PathVariable String id, @RequestParam String roomId) {
        boolean result = roomFloorPlanService.setAsMain(id, roomId);
        return Result.success(result);
    }

    /**
     * 获取机房平面图统计信息
     */
    @GetMapping("/floor-plan/statistics")
    public Result<Map<String, Object>> getFloorPlanStatistics(@RequestParam String roomId) {
        Map<String, Object> stats = roomFloorPlanService.getStatistics(roomId);
        return Result.success(stats);
    }
}
