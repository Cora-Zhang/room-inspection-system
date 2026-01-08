package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 告警记录
 */
@Data
@TableName("alarm_records")
public class AlarmRecord {

    /**
     * 告警ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 告警编号
     */
    private String alarmCode;

    /**
     * 告警级别：CRITICAL-紧急，MAJOR-重要，MINOR-一般
     */
    private String level;

    /**
     * 告警类型：DEVICE-设备，ENVIRONMENT-环境，POWER-电力，FIRE-消防，ACCESS-门禁，OTHER-其他
     */
    private String type;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 机房ID
     */
    private String roomId;

    /**
     * 告警标题
     */
    private String title;

    /**
     * 告警内容
     */
    private String content;

    /**
     * 告警值
     */
    private String alarmValue;

    /**
     * 阈值
     */
    private String threshold;

    /**
     * 告警状态：ACTIVE-活跃，ACKNOWLEDGED-已确认，RESOLVED-已解决，CLOSED-已关闭
     */
    private String status;

    /**
     * 告警来源：SYSTEM-系统，MANUAL-人工，INTERFACE-接口
     */
    private String source;

    /**
     * 发生时间
     */
    private LocalDateTime alarmTime;

    /**
     * 确认人ID
     */
    private String acknowledgedBy;

    /**
     * 确认时间
     */
    private LocalDateTime acknowledgedAt;

    /**
     * 处理人ID
     */
    private String handledBy;

    /**
     * 处理时间
     */
    private LocalDateTime handledAt;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 关联工单ID
     */
    private String workOrderId;

    /**
     * 是否发送短信通知
     */
    private Boolean smsSent;

    /**
     * 是否发送钉钉通知
     */
    private Boolean dingTalkSent;

    /**
     * 是否发送邮件通知
     */
    private Boolean emailSent;

    /**
     * 重试次数
     */
    private Integer retryCount;

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
