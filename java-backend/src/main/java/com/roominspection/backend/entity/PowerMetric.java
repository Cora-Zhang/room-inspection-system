package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电力指标实体
 * 用于存储电力设备的实时监控数据，支持市电、UPS、PDU等不同类型的指标
 */
@Data
@TableName("power_metric")
public class PowerMetric {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 电力设备ID
     */
    private Long powerSystemId;

    /**
     * 设备编号
     */
    private String deviceCode;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 采集时间
     */
    private LocalDateTime collectTime;

    // ========== 市电输入参数 ==========
    /**
     * 输入电压A相（V）
     */
    private BigDecimal inputVoltageA;

    /**
     * 输入电压B相（V）
     */
    private BigDecimal inputVoltageB;

    /**
     * 输入电压C相（V）
     */
    private BigDecimal inputVoltageC;

    /**
     * 输入电流A相（A）
     */
    private BigDecimal inputCurrentA;

    /**
     * 输入电流B相（A）
     */
    private BigDecimal inputCurrentB;

    /**
     * 输入电流C相（A）
     */
    private BigDecimal inputCurrentC;

    /**
     * 输入频率（Hz）
     */
    private BigDecimal inputFrequency;

    /**
     * 有功功率（kW）
     */
    private BigDecimal activePower;

    /**
     * 无功功率（kVar）
     */
    private BigDecimal reactivePower;

    /**
     * 功率因数
     */
    private BigDecimal powerFactor;

    // ========== UPS参数 ==========
    /**
     * UPS负载百分比（%）
     */
    private BigDecimal upsLoadPercent;

    /**
     * UPS后备时间（分钟）
     */
    private Integer upsBackupTime;

    /**
     * UPS输出电压A相（V）
     */
    private BigDecimal outputVoltageA;

    /**
     * UPS输出电压B相（V）
     */
    private BigDecimal outputVoltageB;

    /**
     * UPS输出电压C相（V）
     */
    private BigDecimal outputVoltageC;

    /**
     * 电池温度（℃）
     */
    private BigDecimal batteryTemperature;

    /**
     * 电池容量百分比（%）
     */
    private BigDecimal batteryCapacity;

    /**
     * 电池状态（charging-充电、discharging-放电、idle-空闲）
     */
    private String batteryStatus;

    // ========== PDU参数 ==========
    /**
     * PDU总电流（A）
     */
    private BigDecimal pduTotalCurrent;

    /**
     * PDU负载状态（normal-正常、overload-过载、warning-预警）
     */
    private String pduLoadStatus;

    /**
     * PDU输出电压（V）
     */
    private BigDecimal pduOutputVoltage;

    // ========== 通用参数 ==========
    /**
     * 总负载百分比（%）
     */
    private BigDecimal totalLoadPercent;

    /**
     * 总有功功率（kW）
     */
    private BigDecimal totalActivePower;

    /**
     * 环境温度（℃）
     */
    private BigDecimal ambientTemperature;

    /**
     * 环境湿度（%）
     */
    private BigDecimal ambientHumidity;

    /**
     * 数据来源（snmp、modbus、manual）
     */
    private String dataSource;

    /**
     * 是否告警
     */
    private Boolean isAlert;

    /**
     * 告警信息（JSON格式，存储告警详情）
     */
    private String alertInfo;

    /**
     * 扩展字段（JSON格式，存储其他自定义指标）
     */
    private String extraData;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
