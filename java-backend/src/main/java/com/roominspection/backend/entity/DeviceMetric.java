package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 设备监控指标实体
 */
@Data
@TableName("device_metric")
public class DeviceMetric {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备编号
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 指标类型：
     * CPU_USAGE-CPU使用率
     * MEMORY_USAGE-内存使用率
     * DISK_USAGE-磁盘使用率
     * NETWORK_IN-网络入流量
     * NETWORK_OUT-网络出流量
     * PORT_STATUS-端口状态
     * TEMPERATURE-温度
     * HUMIDITY-湿度
     * POWER_USAGE-功率
     * CUSTOM-自定义指标
     */
    private String metricType;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 指标值（数值型）
     */
    private Double metricValue;

    /**
     * 指标单位
     */
    private String unit;

    /**
     * 指标状态：NORMAL-正常，WARNING-警告，CRITICAL-严重
     */
    private String status;

    /**
     * 是否超出阈值（0-否 1-是）
     */
    private Integer exceededThreshold;

    /**
     * 端口名称（用于端口状态指标）
     */
    private String portName;

    /**
     * 端口索引（用于端口状态指标）
     */
    private Integer portIndex;

    /**
     * 端口状态：UP, DOWN, TESTING, UNKNOWN
     */
    private String portStatus;

    /**
     * 磁盘名称（用于磁盘使用率指标）
     */
    private String diskName;

    /**
     * 磁盘路径（用于磁盘使用率指标）
     */
    private String diskPath;

    /**
     * 磁盘总量（GB）
     */
    private Double diskTotal;

    /**
     * 磁盘已用量（GB）
     */
    private Double diskUsed;

    /**
     * 磁盘可用量（GB）
     */
    private Double diskFree;

    /**
     * 阈值上限
     */
    private Double thresholdUpper;

    /**
     * 阈值下限
     */
    private Double thresholdLower;

    /**
     * 自定义指标键（JSON格式，用于存储额外的指标信息）
     */
    private String customKey;

    /**
     * 自定义指标值（JSON格式，用于存储复杂的指标数据）
     */
    private String customValue;

    /**
     * 采集方式：SNMP- SNMP采集，MODBUS-Modbus采集，SCRIPT-脚本采集，MANUAL-手动录入
     */
    private String collectionMethod;

    /**
     * 采集时间
     */
    private LocalDateTime collectionTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
