package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 值班排班实体
 */
@Data
@TableName("shift_schedules")
public class ShiftSchedule {

    /**
     * 排班ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 值班日期
     */
    private LocalDate scheduleDate;

    /**
     * 班次：DAY-白班（08:00-17:00），NIGHT-夜班（18:00-次日07:00）
     */
    private String shift;

    /**
     * 值班人员ID
     */
    private String staffId;

    /**
     * 值班人员姓名（冗余字段）
     */
    private String staffName;

    /**
     * 责任机房ID
     */
    private Long roomId;

    /**
     * 责任机房名称
     */
    private String roomName;

    /**
     * 排班状态：SCHEDULED-已排班，IN_PROGRESS-值班中，COMPLETED-已完成，MISSED-缺勤
     */
    private String status;

    /**
     * 实际开始时间
     */
    private LocalDateTime actualStartTime;

    /**
     * 实际结束时间
     */
    private LocalDateTime actualEndTime;

    /**
     * 数据来源（1-手动创建 2-Excel导入 3-钉钉同步）
     */
    private Integer dataSource;

    /**
     * 导入批次号（用于Excel导入）
     */
    private String importBatch;

    /**
     * 钉钉同步状态（0-未同步 1-已同步 2-同步失败）
     */
    private Integer syncDingtalkStatus;

    /**
     * 钉钉任务ID
     */
    private String dingtalkTaskId;

    /**
     * 周期性排班类型（null-非周期性 1-每周轮换 2-每月轮换 3-季度轮换）
     */
    private Integer scheduleType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    private Integer deleted;
}
