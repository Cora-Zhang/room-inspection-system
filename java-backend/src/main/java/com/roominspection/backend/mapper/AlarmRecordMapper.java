package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.AlarmRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警记录Mapper
 */
@Mapper
public interface AlarmRecordMapper extends BaseMapper<AlarmRecord> {

    /**
     * 查询指定时间范围的告警记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 告警记录列表
     */
    @Select("SELECT * FROM alarm_records WHERE alarm_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 ORDER BY alarm_time DESC")
    List<AlarmRecord> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定机房和时间范围的告警记录
     *
     * @param roomId 机房ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 告警记录列表
     */
    @Select("SELECT * FROM alarm_records WHERE room_id = #{roomId} AND alarm_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 ORDER BY alarm_time DESC")
    List<AlarmRecord> findByRoomAndTimeRange(@Param("roomId") String roomId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定级别的告警记录
     *
     * @param level 级别
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 告警记录列表
     */
    @Select("SELECT * FROM alarm_records WHERE level = #{level} AND alarm_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 ORDER BY alarm_time DESC")
    List<AlarmRecord> findByLevelAndTimeRange(@Param("level") String level,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 查询未处理的告警记录
     *
     * @return 告警记录列表
     */
    @Select("SELECT * FROM alarm_records WHERE status IN ('ACTIVE', 'ACKNOWLEDGED') AND deleted = 0 ORDER BY alarm_time DESC")
    List<AlarmRecord> findUnresolved();

    /**
     * 查询未处理的紧急告警记录
     *
     * @return 告警记录列表
     */
    @Select("SELECT * FROM alarm_records WHERE status IN ('ACTIVE', 'ACKNOWLEDGED') AND level = 'CRITICAL' AND deleted = 0 ORDER BY alarm_time DESC")
    List<AlarmRecord> findCriticalUnresolved();

    /**
     * 统计告警数量（按级别）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT level, COUNT(*) as count FROM alarm_records WHERE alarm_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 GROUP BY level")
    List<Map<String, Object>> countByLevel(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 统计告警数量（按类型）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT type, COUNT(*) as count FROM alarm_records WHERE alarm_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 GROUP BY type")
    List<Map<String, Object>> countByType(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 统计告警数量（按状态）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT status, COUNT(*) as count FROM alarm_records WHERE alarm_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);
}
