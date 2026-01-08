package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.InspectionReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 巡检报表Mapper
 */
@Mapper
public interface InspectionReportMapper extends BaseMapper<InspectionReport> {

    /**
     * 查询指定类型的报表列表
     *
     * @param reportType 报表类型
     * @return 报表列表
     */
    @Select("SELECT * FROM inspection_reports WHERE report_type = #{reportType} AND deleted = 0 ORDER BY report_date DESC")
    List<InspectionReport> findByReportType(@Param("reportType") String reportType);

    /**
     * 查询指定日期范围的报表列表
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报表列表
     */
    @Select("SELECT * FROM inspection_reports WHERE report_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0 ORDER BY report_date DESC")
    List<InspectionReport> findByDateRange(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * 查询指定机房的报表列表
     *
     * @param roomId 机房ID
     * @return 报表列表
     */
    @Select("SELECT * FROM inspection_reports WHERE room_id = #{roomId} AND deleted = 0 ORDER BY report_date DESC")
    List<InspectionReport> findByRoomId(@Param("roomId") String roomId);

    /**
     * 查询指定日期的报表
     *
     * @param reportDate 报表日期
     * @return 报表
     */
    @Select("SELECT * FROM inspection_reports WHERE report_date = #{reportDate} AND deleted = 0 LIMIT 1")
    InspectionReport findByReportDate(@Param("reportDate") LocalDate reportDate);
}
