package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.DeviceLocation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 设备位置Mapper
 */
@Mapper
public interface DeviceLocationMapper extends BaseMapper<DeviceLocation> {

    /**
     * 查询指定机房的设备位置列表
     *
     * @param roomId 机房ID
     * @return 设备位置列表
     */
    @Select("SELECT * FROM device_locations WHERE room_id = #{roomId} AND deleted = 0")
    List<DeviceLocation> findByRoomId(@Param("roomId") String roomId);

    /**
     * 查询指定平面图的设备位置列表
     *
     * @param floorPlanId 平面图ID
     * @return 设备位置列表
     */
    @Select("SELECT * FROM device_locations WHERE floor_plan_id = #{floorPlanId} AND deleted = 0")
    List<DeviceLocation> findByFloorPlanId(@Param("floorPlanId") String floorPlanId);

    /**
     * 查询指定区域的设备位置列表
     *
     * @param zone 区域编号
     * @return 设备位置列表
     */
    @Select("SELECT * FROM device_locations WHERE zone = #{zone} AND deleted = 0")
    List<DeviceLocation> findByZone(@Param("zone") String zone);

    /**
     * 查询指定状态的设备位置列表
     *
     * @param status 状态
     * @return 设备位置列表
     */
    @Select("SELECT * FROM device_locations WHERE status = #{status} AND deleted = 0")
    List<DeviceLocation> findByStatus(@Param("status") String status);

    /**
     * 查询指定设备类型的设备位置列表
     *
     * @param deviceType 设备类型
     * @return 设备位置列表
     */
    @Select("SELECT * FROM device_locations WHERE device_type = #{deviceType} AND deleted = 0")
    List<DeviceLocation> findByDeviceType(@Param("deviceType") String deviceType);
}
