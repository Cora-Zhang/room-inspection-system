package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.ShiftSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 值班排班Mapper
 */
@Mapper
public interface ShiftScheduleMapper extends BaseMapper<ShiftSchedule> {

    /**
     * 查询指定日期范围的排班记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排班记录列表
     */
    @Select("SELECT * FROM shift_schedules WHERE schedule_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    List<ShiftSchedule> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询指定值班人员的排班记录
     *
     * @param staffId 值班人员ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排班记录列表
     */
    @Select("SELECT * FROM shift_schedules WHERE staff_id = #{staffId} AND schedule_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    List<ShiftSchedule> findByStaffAndDateRange(@Param("staffId") String staffId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * 查询指定日期的排班记录
     *
     * @param scheduleDate 值班日期
     * @return 排班记录列表
     */
    @Select("SELECT * FROM shift_schedules WHERE schedule_date = #{scheduleDate} AND deleted = 0")
    List<ShiftSchedule> findByScheduleDate(@Param("scheduleDate") LocalDate scheduleDate);
}
