package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.RoomFloorPlan;

import java.util.List;
import java.util.Map;

/**
 * 机房平面图Service
 */
public interface RoomFloorPlanService extends IService<RoomFloorPlan> {

    /**
     * 根据机房ID查询平面图列表
     *
     * @param roomId 机房ID
     * @return 平面图列表
     */
    List<RoomFloorPlan> listByRoomId(String roomId);

    /**
     * 获取机房主平面图
     *
     * @param roomId 机房ID
     * @return 主平面图
     */
    RoomFloorPlan getMainFloorPlan(String roomId);

    /**
     * 创建机房平面图
     *
     * @param floorPlan 平面图对象
     * @return 创建结果
     */
    boolean createFloorPlan(RoomFloorPlan floorPlan);

    /**
     * 更新机房平面图
     *
     * @param floorPlan 平面图对象
     * @return 更新结果
     */
    boolean updateFloorPlan(RoomFloorPlan floorPlan);

    /**
     * 删除机房平面图
     *
     * @param id 平面图ID
     * @return 删除结果
     */
    boolean deleteFloorPlan(String id);

    /**
     * 设置主平面图
     *
     * @param id 平面图ID
     * @param roomId 机房ID
     * @return 设置结果
     */
    boolean setAsMain(String id, String roomId);

    /**
     * 获取机房平面图统计信息
     *
     * @param roomId 机房ID
     * @return 统计信息
     */
    Map<String, Object> getStatistics(String roomId);
}
