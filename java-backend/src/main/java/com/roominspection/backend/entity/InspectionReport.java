package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 巡检报表
 */
@Data
@TableName("inspection_reports")
public class InspectionReport {

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
     * 报表类型：DAILY-日报，WEEKLY-周报，MONTHLY-月报，QUARTERLY-季报，CUSTOM-自定义
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
     * 计划巡检次数
     */
    private Integer plannedInspections;

    /**
     * 实际巡检次数
     */
    private Integer actualInspections;

    /**
     * 完成率（%）
     */
    private Double completionRate;

    /**
     * 正常巡检次数
     */
    private Integer normalInspections;

    /**
     * 异常巡检次数
     */
    private Integer abnormalInspections;

    /**
     * 发现问题数量
     */
    private Integer issueCount;

    /**
     * 已处理问题数量
     */
    private Integer resolvedIssueCount;

    /**
     * 问题处理率（%）
     */
    private Double issueResolutionRate;

    /**
     * 生成工单数量
     */
    private Integer workOrderCount;

    /**
     * 已完成工单数量
     */
    private Integer completedWorkOrderCount;

    /**
     * 工单完成率（%）
     */
    private Double workOrderCompletionRate;

    /**
     * 平均巡检时长（分钟）
     */
    private Double avgInspectionDuration;

    /**
     * 最短巡检时长（分钟）
     */
    private Double minInspectionDuration;

    /**
     * 最长巡检时长（分钟）
     */
    private Double maxInspectionDuration;

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
