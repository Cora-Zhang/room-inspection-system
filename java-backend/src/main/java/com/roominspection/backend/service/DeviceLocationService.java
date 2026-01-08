package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.DeviceLocation;

import java.util.List;
import java.util.Map;

/**
 * 设备位置Service
 */
public interface DeviceLocationService extends IService<DeviceLocation> {

    /**
     * 根据机房ID查询设备位置列表
     *
     * @param roomId 机房ID
     * @return 设备位置列表
     */
    List<DeviceLocation> listByRoomId(String roomId);

    /**
     * 根据平面图ID查询设备位置列表
     *
     * @param floorPlanId 平面图ID
     * @return 设备位置列表
     */
    List<DeviceLocation> listByFloorPlanId(String floorPlanId);

    /**
     * 根据状态查询设备位置列表
     *
     * @param status 状态
     * @return 设备位置列表
     */
    List<DeviceLocation> listByStatus(String status);

    /**
     * 创建设备位置
     *
     * @param deviceLocation 设备位置对象
     * @return 创建结果
     */
    boolean createDeviceLocation(DeviceLocation deviceLocation);

    /**
     * 批量创建设备位置
     *
     * @param deviceLocations 设备位置列表
     * @return 创建结果
     */
    boolean batchCreateDeviceLocations(List<DeviceLocation> deviceLocations);

    /**
     * 更新设备位置
     *
     * @param deviceLocation 设备位置对象
     * @return 更新结果
     */
    boolean updateDeviceLocation(DeviceLocation deviceLocation);

    /**
     * 删除设备位置
     *
     * @param id 位置ID
     * @return 删除结果
     */
    boolean deleteDeviceLocation(String id);

    /**
     * 批量删除设备位置
     *
     * @param ids 位置ID列表
     * @return 删除结果
     */
    boolean batchDeleteDeviceLocations(List<String> ids);

    /**
     * 更新设备状态
     *
     * @param id 位置ID
     * @param status 状态
     * @return 更新结果
     */
    boolean updateStatus(String id, String status);

    /**
     * 获取设备位置统计信息
     *
     * @param roomId 机房ID
     * @return 统计信息
     */
    Map<String, Object> getStatistics(String roomId);

    /**
     * 获取告警设备列表
     *
     * @param roomId 机房ID
     * @return 告警设备列表
     */
    List<DeviceLocation> getAlarmDevices(String roomId);
}
