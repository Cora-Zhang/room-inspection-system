package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 能效优化工单实体类
 */
@Data
@TableName("energy_efficiency_order")
public class EnergyEfficiencyOrder {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 工单编号
     */
    private String orderNo;

    /**
     * 机房ID
     */
    private Long roomId;

    /**
     * 工单类型（HIGH_TEMP-高温检查、HUMIDITY_RISE-湿度排查、COLD_AIR-冷通道检查、AIR_DUCT-风道检查）
     */
    private String orderType;

    /**
     * 工单标题
     */
    private String title;

    /**
     * 工单描述
     */
    private String description;

    /**
     * 触发条件（持续高温、湿度缓升等）
     */
    private String triggerCondition;

    /**
     * 触发值
     */
    private Double triggerValue;

    /**
     * 触发时间
     */
    private LocalDateTime triggerTime;

    /**
     * 异常区域ID
     */
    private Long abnormalAreaId;

    /**
     * 异常区域描述
     */
    private String abnormalArea;

    /**
     * 优先级（LOW-低、MEDIUM-中、HIGH-高、URGENT-紧急）
     */
    private String priority;

    /**
     * 工单状态（PENDING-待处理、PROCESSING-处理中、COMPLETED-已完成、CLOSED-已关闭）
     */
    private String status;

    /**
     * 负责人ID
     */
    private Long assigneeId;

    /**
     * 负责人姓名
     */
    private String assigneeName;

    /**
     * 指派时间
     */
    private LocalDateTime assignTime;

    /**
     * 计划完成时间
     */
    private LocalDateTime planCompleteTime;

    /**
     * 实际完成时间
     */
    private LocalDateTime actualCompleteTime;

    /**
     * 处理结果
     */
    private String result;

    /**
     * 处理建议
     */
    private String suggestion;

    /**
     * 附件ID
     */
    private Long attachmentId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private Long updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
