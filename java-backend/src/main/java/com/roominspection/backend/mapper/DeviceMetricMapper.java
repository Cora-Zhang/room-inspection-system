package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.DeviceMetric;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备监控指标Mapper接口
 */
@Mapper
public interface DeviceMetricMapper extends BaseMapper<DeviceMetric> {

    /**
     * 批量插入设备指标
     *
     * @param metrics 指标列表
     * @return 插入数量
     */
    int insertBatch(@Param("metrics") List<DeviceMetric> metrics);

    /**
     * 查询设备的最新指标
     *
     * @param deviceId 设备ID
     * @return 指标列表
     */
    @Select("SELECT * FROM device_metric WHERE device_id = #{deviceId} " +
            "AND collection_time = (SELECT MAX(collection_time) FROM device_metric WHERE device_id = #{deviceId})")
    List<DeviceMetric> selectLatestByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 查询机房的所有最新指标
     *
     * @param roomId 机房ID
     * @return 指标列表
     */
    @Select("SELECT dm.* FROM device_metric dm " +
            "INNER JOIN (SELECT device_id, MAX(collection_time) as max_time " +
            "            FROM device_metric " +
            "            WHERE device_id IN (SELECT id FROM device WHERE room_id = #{roomId}) " +
            "            GROUP BY device_id) latest " +
            "ON dm.device_id = latest.device_id AND dm.collection_time = latest.max_time")
    List<DeviceMetric> selectLatestByRoomId(@Param("roomId") String roomId);

    /**
     * 查询告警指标
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 指标列表
     */
    @Select("SELECT * FROM device_metric WHERE status != 'NORMAL' " +
            "AND collection_time >= #{startTime} AND collection_time <= #{endTime} " +
            "ORDER BY collection_time DESC")
    List<DeviceMetric> selectAlarmMetrics(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 清理历史指标数据
     *
     * @param beforeTime 清理时间点之前的数据
     * @return 删除数量
     */
    @Select("DELETE FROM device_metric WHERE collection_time < #{beforeTime}")
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);
}
