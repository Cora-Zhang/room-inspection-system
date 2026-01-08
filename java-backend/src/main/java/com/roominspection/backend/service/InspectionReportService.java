package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.InspectionReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 巡检报表Service
 */
public interface InspectionReportService extends IService<InspectionReport> {

    /**
     * 根据报表类型查询报表列表
     *
     * @param reportType 报表类型
     * @return 报表列表
     */
    List<InspectionReport> listByReportType(String reportType);

    /**
     * 根据日期范围查询报表列表
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报表列表
     */
    List<InspectionReport> listByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 根据机房查询报表列表
     *
     * @param roomId 机房ID
     * @return 报表列表
     */
    List<InspectionReport> listByRoomId(String roomId);

    /**
     * 生成日报
     *
     * @param reportDate 报表日期
     * @param roomId 机房ID（可选，为空则生成全机房报表）
     * @return 日报
     */
    InspectionReport generateDailyReport(LocalDate reportDate, String roomId);

    /**
     * 生成周报
     *
     * @param reportDate 报表日期（周日）
     * @param roomId 机房ID（可选）
     * @return 周报
     */
    InspectionReport generateWeeklyReport(LocalDate reportDate, String roomId);

    /**
     * 生成月报
     *
     * @param reportDate 报表日期（月末）
     * @param roomId 机房ID（可选）
     * @return 月报
     */
    InspectionReport generateMonthlyReport(LocalDate reportDate, String roomId);

    /**
     * 获取巡检完成率统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param roomId 机房ID（可选）
     * @return 统计信息
     */
    Map<String, Object> getCompletionRateStats(LocalDate startDate, LocalDate endDate, String roomId);

    /**
     * 获取巡检时效分析
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param roomId 机房ID（可选）
     * @return 时效分析结果
     */
    Map<String, Object> getTimeAnalysis(LocalDate startDate, LocalDate endDate, String roomId);

    /**
     * 获取值班人员绩效统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 绩效统计结果
     */
    List<Map<String, Object>> getStaffPerformanceStats(LocalDate startDate, LocalDate endDate);
}
