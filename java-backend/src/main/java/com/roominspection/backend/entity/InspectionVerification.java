package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * 巡检核验实体
 * 用于生成巡检核验报告，分析巡检完整性和质量
 */
@Data
@TableName("inspection_verification")
public class InspectionVerification {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 巡检任务ID
     */
    private Long inspectionTaskId;

    /**
     * 任务编号
     */
    private String taskNo;

    /**
     * 机房ID
     */
    private Long roomId;

    /**
     * 机房名称
     */
    private String roomName;

    /**
     * 巡检人员ID
     */
    private Long inspectorId;

    /**
     * 巡检人员姓名
     */
    private String inspectorName;

    /**
     * 计划开始时间
     */
    private LocalDateTime plannedStartTime;

    /**
     * 计划结束时间
     */
    private LocalDateTime plannedEndTime;

    /**
     * 实际开始时间（基于门禁进入记录）
     */
    private LocalDateTime actualStartTime;

    /**
     * 实际结束时间（基于门禁离开记录）
     */
    private LocalDateTime actualEndTime;

    /**
     * 计划时长（分钟）
     */
    private Integer plannedDuration;

    /**
     * 实际时长（分钟）
     */
    private Integer actualDuration;

    /**
     * 停留时长（分钟，在机房内的总时间）
     */
    private Integer stayDuration;

    /**
     * 计划巡检设备数
     */
    private Integer plannedDeviceCount;

    /**
     * 实际巡检设备数
     */
    private Integer actualDeviceCount;

    /**
     * 计划巡检路线（JSON格式，存储机房-设备序列）
     */
    private String plannedRoute;

    /**
     * 实际巡检路线（基于门禁日志和设备拍照记录）
     */
    private String actualRoute;

    /**
     * 路线一致性（true-完全一致、false-不一致）
     */
    private Boolean routeConsistent;

    /**
     * 进出核验（true-已验证、false-未验证）
     */
    private Boolean accessVerified;

    /**
     * 进入门禁记录ID
     */
    private Long enterLogId;

    /**
     * 离开门禁记录ID
     */
    private Long exitLogId;

    /**
     * 是否按时进入（与计划开始时间对比）
     */
    private Boolean onTimeEntry;

    /**
     * 是否按时完成（与计划结束时间对比）
     */
    private Boolean onTimeCompletion;

    /**
     * 进入延迟时间（分钟，正数表示延迟）
     */
    private Integer entryDelay;

    /**
     * 完成延迟时间（分钟，正数表示延迟）
     */
    private Integer completionDelay;

    /**
     * 照片核验通过数
     */
    private Integer photoPassedCount;

    /**
     * 照片核验失败数
     */
    private Integer photoFailedCount;

    /**
     * 照片核验率（0-100）
     */
    private BigDecimal photoPassRate;

    /**
     * 异常行为识别（JSON格式，存储异常类型列表）
     * fast_pass-快速通过、missed_route-未按路线、incomplete-未完成、repeated-重复巡检
     */
    private String abnormalBehaviors;

    /**
     * 巡检质量评分（0-100分）
     */
    private Integer qualityScore;

    /**
     * 评分等级（excellent-优秀、good-良好、average-合格、poor-较差、fail-不合格）
     */
    private String gradeLevel;

    /**
     * 核验状态（pending-待核验、processing-核验中、completed-已完成）
     */
    private String verificationStatus;

    /**
     * 核验报告（Markdown格式）
     */
    private String verificationReport;

    /**
     * 核验建议（JSON格式，存储改进建议列表）
     */
    private String suggestions;

    /**
     * 审核人员ID
     */
    private Long reviewerId;

    /**
     * 审核意见
     */
    private String reviewComment;

    /**
     * 扩展字段（JSON格式）
     */
    private String extraData;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;
}
