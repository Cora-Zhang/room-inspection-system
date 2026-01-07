package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
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
     * 责任机房ID
     */
    private Long roomId;

    /**
     * 责任机房名称
     */
    private String roomName;

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
     * 门禁刷卡记录（JSON数组，记录值班人员进出机房情况）
     */
    private String accessRecords;

    /**
     * 是否异常（如未按时到岗）
     */
    private Boolean isAbnormal;

    /**
     * 异常原因
     */
    private String abnormalReason;

    /**
     * 是否已发送交接提醒
     */
    private Boolean isReminded;

    /**
     * 提醒发送时间
     */
    private LocalDateTime remindTime;

    /**
     * 交接备注
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
