package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 工单
 */
@Data
@TableName("work_orders")
public class WorkOrder {

    /**
     * 工单ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 工单编号
     */
    private String orderCode;

    /**
     * 工单类型：INSPECTION-巡检工单，MAINTENANCE-维修工单，MAINTENANCE_PREVENTIVE-保养工单，EMERGENCY-应急工单，EFFICIENCY_CHECK-效率核查，ENERGY_OPTIMIZATION-能效优化
     */
    private String type;

    /**
     * 工单标题
     */
    private String title;

    /**
     * 工单描述
     */
    private String description;

    /**
     * 优先级：URGENT-紧急，HIGH-高，MEDIUM-中，LOW-低
     */
    private String priority;

    /**
     * 工单状态：PENDING-待处理，ASSIGNED-已指派，IN_PROGRESS-处理中，WAITING-等待中，COMPLETED-已完成，CANCELLED-已取消，CLOSED-已关闭
     */
    private String status;

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
     * 创建人ID
     */
    private String createdBy;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 指派人ID
     */
    private String assignedTo;

    /**
     * 指派人姓名
     */
    private String assignedToName;

    /**
     * 指派时间
     */
    private LocalDateTime assignedAt;

    /**
     * 负责人ID
     */
    private String ownerId;

    /**
     * 负责人姓名
     */
    private String ownerName;

    /**
     * 预期开始时间
     */
    private LocalDateTime expectedStartTime;

    /**
     * 实际开始时间
     */
    private LocalDateTime actualStartTime;

    /**
     * 预期完成时间
     */
    private LocalDateTime expectedEndTime;

    /**
     * 实际完成时间
     */
    private LocalDateTime actualEndTime;

    /**
     * 工作时长（小时）
     */
    private Double duration;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 处理说明
     */
    private String handleDescription;

    /**
     * 附件信息（JSON格式）
     */
    private String attachments;

    /**
     * 关联告警ID
     */
    private String alarmId;

    /**
     * 是否超时
     */
    private Boolean isOverdue;

    /**
     * 超时时长（分钟）
     */
    private Integer overdueMinutes;

    /**
     * 质量评分
     */
    private Integer qualityScore;

    /**
     * 评分说明
     */
    private String qualityComment;

    /**
     * 关闭人ID
     */
    private String closedBy;

    /**
     * 关闭时间
     */
    private LocalDateTime closedAt;

    /**
     * 关闭原因
     */
    private String closeReason;

    /**
     * 备注
     */
    private String remark;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
