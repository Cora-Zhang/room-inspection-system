package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.DevicePerformanceReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 设备性能报表Service
 */
public interface DevicePerformanceReportService extends IService<DevicePerformanceReport> {

    /**
     * 根据报表类型查询报表列表
     *
     * @param reportType 报表类型
     * @return 报表列表
     */
    List<DevicePerformanceReport> listByReportType(String reportType);

    /**
     * 根据日期范围查询报表列表
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报表列表
     */
    List<DevicePerformanceReport> listByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 根据机房查询报表列表
     *
     * @param roomId 机房ID
     * @return 报表列表
     */
    List<DevicePerformanceReport> listByRoomId(String roomId);

    /**
     * 根据设备查询报表列表
     *
     * @param deviceId 设备ID
     * @return 报表列表
     */
    List<DevicePerformanceReport> listByDeviceId(String deviceId);

    /**
     * 生成日报
     *
     * @param reportDate 报表日期
     * @param roomId 机房ID（可选，为空则生成全机房报表）
     * @return 日报
     */
    DevicePerformanceReport generateDailyReport(LocalDate reportDate, String roomId);

    /**
     * 生成周报
     *
     * @param reportDate 报表日期（周日）
     * @param roomId 机房ID（可选）
     * @return 周报
     */
    DevicePerformanceReport generateWeeklyReport(LocalDate reportDate, String roomId);

    /**
     * 生成月报
     *
     * @param reportDate 报表日期（月末）
     * @param roomId 机房ID（可选）
     * @return 月报
     */
    DevicePerformanceReport generateMonthlyReport(LocalDate reportDate, String roomId);

    /**
     * 获取设备可用率统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param roomId 机房ID（可选）
     * @return 可用率统计结果
     */
    Map<String, Object> getAvailabilityRateStats(LocalDate startDate, LocalDate endDate, String roomId);

    /**
     * 获取设备MTBF和MTTR统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param deviceId 设备ID（可选）
     * @return MTBF和MTTR统计结果
     */
    Map<String, Object> getMTBFAndMTTRStats(LocalDate startDate, LocalDate endDate, String deviceId);

    /**
     * 获取工单处理时效分析
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 时效分析结果
     */
    Map<String, Object> getWorkOrderTimeAnalysis(LocalDate startDate, LocalDate endDate);
}
