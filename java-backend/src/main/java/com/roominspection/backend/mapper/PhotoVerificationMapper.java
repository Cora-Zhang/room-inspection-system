package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.PhotoVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 照片核验Mapper
 */
@Mapper
public interface PhotoVerificationMapper extends BaseMapper<PhotoVerification> {

    /**
     * 查询巡检任务的所有照片核验记录
     */
    @Select("SELECT * FROM photo_verification WHERE inspection_task_id = #{inspectionTaskId} " +
            "AND deleted = 0 ORDER BY photo_time")
    List<PhotoVerification> findByInspectionTaskId(@Param("inspectionTaskId") Long inspectionTaskId);

    /**
     * 查询设备的所有照片核验记录
     */
    @Select("SELECT * FROM photo_verification WHERE device_id = #{deviceId} " +
            "AND deleted = 0 ORDER BY photo_time DESC")
    List<PhotoVerification> findByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 统计巡检任务照片核验情况
     */
    @Select("SELECT " +
            "  COUNT(*) as total_count, " +
            "  SUM(CASE WHEN verification_status = 'passed' THEN 1 ELSE 0 END) as passed_count, " +
            "  SUM(CASE WHEN verification_status = 'failed' THEN 1 ELSE 0 END) as failed_count, " +
            "  SUM(CASE WHEN verification_status = 'pending' THEN 1 ELSE 0 END) as pending_count " +
            "FROM photo_verification " +
            "WHERE inspection_task_id = #{inspectionTaskId} AND deleted = 0")
    Map<String, Object> getStatistics(@Param("inspectionTaskId") Long inspectionTaskId);

    /**
     * 查询异常照片（模糊、光线过暗、无标签等）
     */
    @Select("SELECT * FROM photo_verification WHERE abnormal_type IS NOT NULL " +
            "AND verification_status IN ('failed', 'manual') " +
            "AND deleted = 0 ORDER BY photo_time DESC")
    List<PhotoVerification> findAbnormalPhotos();
}
