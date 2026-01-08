package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.RoomFloorPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 机房平面图Mapper
 */
@Mapper
public interface RoomFloorPlanMapper extends BaseMapper<RoomFloorPlan> {

    /**
     * 查询指定机房的平面图列表
     *
     * @param roomId 机房ID
     * @return 平面图列表
     */
    @Select("SELECT * FROM room_floor_plans WHERE room_id = #{roomId} AND deleted = 0 ORDER BY sort_order")
    List<RoomFloorPlan> findByRoomId(@Param("roomId") String roomId);

    /**
     * 查询机房的主平面图
     *
     * @param roomId 机房ID
     * @return 主平面图
     */
    @Select("SELECT * FROM room_floor_plans WHERE room_id = #{roomId} AND is_main = 1 AND deleted = 0 LIMIT 1")
    RoomFloorPlan findMainByRoomId(@Param("roomId") String roomId);
}
