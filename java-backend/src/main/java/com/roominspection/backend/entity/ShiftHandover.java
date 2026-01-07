package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 值班交接记录实体
 */
@Data
@TableName("shift_handovers")
public class ShiftHandover {

    /**
     * 交接记录ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 交接编号
     */
    private String code;

    /**
     * 交接日期
     */
    private LocalDate handoverDate;

    /**
     * 班次：DAY-白班，NIGHT-夜班
     */
    private String shift;

    /**
     * 交班人ID
     */
    private String outgoingStaffId;

    /**
     * 交班人姓名
     */
    private String outgoingStaffName;

    /**
     * 接班人ID
     */
    private String incomingStaffId;

    /**
     * 接班人姓名
     */
    private String incomingStaffName;

    /**
     * 交班时间
     */
    private LocalDateTime outgoingTime;

    /**
     * 接班时间
     */
    private LocalDateTime incomingTime;

    /**
     * 完成任务列表（JSON数组）
     */
    private String tasks;

    /**
     * 待处理问题列表（JSON数组）
     */
    private String issues;

    /**
     * 设备状态
     */
    private String equipmentStatus;

    /**
     * 交接内容
     */
    private String handoverContent;

    /**
     * 接班确认
     */
    private Boolean incomingConfirmed;

    /**
     * 交接备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
