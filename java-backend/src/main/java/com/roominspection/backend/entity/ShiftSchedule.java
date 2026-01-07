package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
     * 排班状态：SCHEDULED-已排班，COMPLETED-已完成，MISSED-缺勤
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
     * 备注
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
