package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.DoorAccessLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门禁日志Mapper
 */
@Mapper
public interface DoorAccessLogMapper extends BaseMapper<DoorAccessLog> {

    /**
     * 查询指定人员和时间范围内的门禁记录
     */
    @Select("SELECT * FROM door_access_log WHERE staff_id = #{staffId} " +
            "AND access_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY access_time")
    List<DoorAccessLog> findByStaffIdAndTimeRange(@Param("staffId") Long staffId,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定机房的门禁记录
     */
    @Select("SELECT * FROM door_access_log WHERE room_id = #{roomId} " +
            "AND access_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY access_time DESC")
    List<DoorAccessLog> findByRoomIdAndTimeRange(@Param("roomId") Long roomId,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 查询进入记录（direction = 'in'）
     */
    @Select("SELECT * FROM door_access_log WHERE staff_id = #{staffId} " +
            "AND direction = 'in' AND status = 'success' " +
            "AND access_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY access_time")
    List<DoorAccessLog> findEntryRecords(@Param("staffId") Long staffId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查询离开记录（direction = 'out'）
     */
    @Select("SELECT * FROM door_access_log WHERE staff_id = #{staffId} " +
            "AND direction = 'out' AND status = 'success' " +
            "AND access_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY access_time")
    List<DoorAccessLog> findExitRecords(@Param("staffId") Long staffId,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 统计人员停留时长（进入到离开的时间差）
     */
    @Select("SELECT " +
            "  t1.id, " +
            "  t1.staff_id, " +
            "  t1.staff_name, " +
            "  t1.access_time as entry_time, " +
            "  t2.access_time as exit_time, " +
            "  TIMESTAMPDIFF(MINUTE, t1.access_time, t2.access_time) as stay_minutes " +
            "FROM door_access_log t1 " +
            "LEFT JOIN door_access_log t2 ON t1.staff_id = t2.staff_id " +
            "  AND t1.direction = 'in' AND t2.direction = 'out' " +
            "  AND t1.access_time < t2.access_time " +
            "WHERE t1.staff_id = #{staffId} " +
            "  AND t1.access_time BETWEEN #{startTime} AND #{endTime} " +
            "  AND t1.deleted = 0 AND t2.deleted = 0 " +
            "ORDER BY t1.access_time")
    List<Map<String, Object>> calculateStayDuration(@Param("staffId") Long staffId,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 统计机房访问量
     */
    @Select("SELECT room_id, room_name, COUNT(*) as access_count " +
            "FROM door_access_log " +
            "WHERE access_time BETWEEN #{startTime} AND #{endTime} " +
            "  AND deleted = 0 " +
            "GROUP BY room_id, room_name " +
            "ORDER BY access_count DESC")
    List<Map<String, Object>> getRoomAccessStatistics(@Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);
}
