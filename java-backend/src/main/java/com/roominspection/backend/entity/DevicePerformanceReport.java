package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备性能报表
 */
@Data
@TableName("device_performance_reports")
public class DevicePerformanceReport {

    /**
     * 报表ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 报表编号
     */
    private String reportCode;

    /**
     * 报表类型：DAILY-日报，WEEKLY-周报，MONTHLY-月报，QUARTERLY-季报
     */
    private String reportType;

    /**
     * 统计日期
     */
    private LocalDate reportDate;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 机房ID
     */
    private String roomId;

    /**
     * 机房名称
     */
    private String roomName;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 设备总数
     */
    private Integer totalDevices;

    /**
     * 正常设备数量
     */
    private Integer normalDevices;

    /**
     * 告警设备数量
     */
    private Integer warningDevices;

    /**
     * 故障设备数量
     */
    private Integer errorDevices;

    /**
     * 离线设备数量
     */
    private Integer offlineDevices;

    /**
     * 设备可用率（%）
     */
    private Double availabilityRate;

    /**
     * 运行时长（小时）
     */
    private Double runningHours;

    /**
     * 故障时长（小时）
     */
    private Double downtimeHours;

    /**
     * 告警次数
     */
    private Integer alarmCount;

    /**
     * 紧急告警次数
     */
    private Integer criticalAlarmCount;

    /**
     * 重要告警次数
     */
    private Integer majorAlarmCount;

    /**
     * 一般告警次数
     */
    private Integer minorAlarmCount;

    /**
     * 平均无故障时间（MTBF，小时）
     */
    private Double mtbf;

    /**
     * 平均修复时间（MTTR，小时）
     */
    private Double mttr;

    /**
     * 维护次数
     */
    private Integer maintenanceCount;

    /**
     * 保养次数
     */
    private Integer preventiveMaintenanceCount;

    /**
     * 性能指标（JSON格式，包含CPU、内存、磁盘等性能数据）
     */
    private String performanceMetrics;

    /**
     * 报表数据（JSON格式，包含详细统计信息）
     */
    private String reportData;

    /**
     * 报表人ID
     */
    private String generatedBy;

    /**
     * 报表人姓名
     */
    private String generatedByName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
