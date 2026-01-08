package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.MonitorTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 监控任务Mapper接口
 */
@Mapper
public interface MonitorTaskMapper extends BaseMapper<MonitorTask> {

    /**
     * 根据设备ID查询最新的任务
     *
     * @param deviceId 设备ID
     * @return 监控任务
     */
    @Select("SELECT * FROM monitor_task WHERE device_id = #{deviceId} ORDER BY created_at DESC LIMIT 1")
    MonitorTask selectLatestByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 统计任务状态
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    @Select("SELECT task_status, COUNT(*) as count FROM monitor_task " +
            "WHERE created_at >= #{startTime} AND created_at <= #{endTime} " +
            "GROUP BY task_status")
    List<Map<String, Object>> statisticsByStatus(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 统计任务耗时
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    @Select("SELECT AVG(duration) as avg_duration, MAX(duration) as max_duration, MIN(duration) as min_duration " +
            "FROM monitor_task " +
            "WHERE created_at >= #{startTime} AND created_at <= #{endTime} AND duration IS NOT NULL")
    Map<String, Object> statisticsDuration(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 清理历史任务记录
     *
     * @param beforeTime 清理时间点之前的数据
     * @return 删除数量
     */
    @Select("DELETE FROM monitor_task WHERE created_at < #{beforeTime}")
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);
}
