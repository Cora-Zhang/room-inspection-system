package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 告警规则实体
 */
@Data
@TableName("alert_rule")
public class AlertRule {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则编码
     */
    private String ruleCode;

    /**
     * 设备ID（null表示适用于所有设备）
     */
    private String deviceId;

    /**
     * 设备类型（null表示适用于所有类型）
     */
    private String deviceType;

    /**
     * 设备子类型（null表示适用于所有子类型）
     */
    private String deviceSubType;

    /**
     * 机房ID（null表示适用于所有机房）
     */
    private String roomId;

    /**
     * 告警类型：
     * CPU_USAGE-CPU使用率告警
     * MEMORY_USAGE-内存使用率告警
     * DISK_USAGE-磁盘使用率告警
     * PORT_DOWN-端口down告警
     * TRAFFIC_ABNORMAL-流量异常告警
     * DEVICE_OFFLINE-设备离线告警
     * TEMP_ABNORMAL-温度异常告警
     * CUSTOM-自定义告警
     */
    private String alertType;

    /**
     * 告警级别：INFO-信息，WARNING-警告，CRITICAL-严重，EMERGENCY-紧急
     */
    private String alertLevel;

    /**
     * 告警条件：
     * GT-大于，LT-小于，EQ-等于，GE-大于等于，LE-小于等于，NE-不等于
     */
    private String condition;

    /**
     * 阈值上限
     */
    private Double thresholdUpper;

    /**
     * 阈值下限
     */
    private Double thresholdLower;

    /**
     * 持续时间（秒），满足条件持续该时间才触发告警
     */
    private Integer duration;

    /**
     * 告警消息模板
     */
    private String messageTemplate;

    /**
     * 是否启用（0-禁用 1-启用）
     */
    private Integer status;

    /**
     * 通知方式：EMAIL-邮件，SMS-短信，DINGTALK-钉钉，ALL-全部
     */
    private String notifyMethod;

    /**
     * 通知人员ID列表（逗号分隔）
     */
    private String notifyUserIds;

    /**
     * 是否静默（0-否 1-是，静默期间不发送通知）
     */
    private Integer isSilenced;

    /**
     * 静默开始时间
     */
    private LocalDateTime silenceStartTime;

    /**
     * 静默结束时间
     */
    private LocalDateTime silenceEndTime;

    /**
     * 告警恢复通知（0-否 1-是，告警恢复后发送通知）
     */
    private Integer recoveryNotify;

    /**
     * 规则优先级（数字越大优先级越高）
     */
    private Integer priority;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新人ID
     */
    private Long updatedBy;

    /**
     * 更新人姓名
     */
    private String updatedByName;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
