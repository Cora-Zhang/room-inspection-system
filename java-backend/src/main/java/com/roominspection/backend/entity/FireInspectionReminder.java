package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 消防设施审验提醒实体
 */
@Data
@TableName("fire_inspection_reminder")
public class FireInspectionReminder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 提醒编号
     */
    private String reminderNo;

    /**
     * 设施ID
     */
    private Long facilityId;

    /**
     * 设施名称
     */
    private String facilityName;

    /**
     * 设施类型：1-灭火器，2-气体灭火系统，3-消防主机，4-消防栓，5-喷淋系统，6-应急照明
     */
    private Integer facilityType;

    /**
     * 机房ID
     */
    private Long roomId;

    /**
     * 设施编号
     */
    private String facilityCode;

    /**
     * 设施位置
     */
    private String facilityLocation;

    /**
     * 审验类型：1-年度检查，2-充装检查，3-专业检测，4-法定审验
     */
    private Integer inspectionType;

    /**
     * 上次审验日期
     */
    private LocalDate lastInspectionDate;

    /**
     * 法定审验周期(月)
     */
    private Integer inspectionPeriod;

    /**
     * 下次审验日期
     */
    private LocalDate nextInspectionDate;

    /**
     * 提醒类型：1-提前30天，2-提前7天，3-提前1天
     */
    private Integer reminderType;

    /**
     * 提醒内容
     */
    private String reminderContent;

    /**
     * 提醒日期
     */
    private LocalDate reminderDate;

    /**
     * 是否已处理：0-未处理，1-已处理
     */
    private Integer handled;

    /**
     * 处理人
     */
    private String handler;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;
}
