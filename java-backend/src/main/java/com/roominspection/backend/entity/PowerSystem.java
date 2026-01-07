package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电力系统设备实体
 * 用于管理机房电力设备（市电、UPS、PDU等），支持SNMP/Modbus协议监控
 */
@Data
@TableName("power_system")
public class PowerSystem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 设备编号（唯一标识）
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型（main_power-市电、ups-不间断电源、pdu-配电单元、generator-发电机）
     */
    private String deviceType;

    /**
     * 设备品牌
     */
    private String brand;

    /**
     * 设备型号
     */
    private String model;

    /**
     * 序列号
     */
    private String serialNumber;

    /**
     * 机房ID
     */
    private Long roomId;

    /**
     * 机房名称
     */
    private String roomName;

    /**
     * 位置描述（如：机房A区第一排）
     */
    private String location;

    /**
     * 额定容量（kVA或kW）
     */
    private BigDecimal ratedCapacity;

    /**
     * 监控协议（snmp、modbus、bms、custom）
     */
    private String protocol;

    /**
     * 监控地址（IP地址或串口地址）
     */
    private String monitorAddress;

    /**
     * 监控端口（SNMP端口或Modbus端口）
     */
    private Integer monitorPort;

    /**
     * SNMP Community或认证凭证
     */
    private String authCredential;

    /**
     * OID配置（JSON格式，存储各指标的OID映射）
     */
    private String oidConfig;

    /**
     * Modbus寄存器配置（JSON格式）
     */
    private String modbusConfig;

    /**
     * 采集间隔（秒）
     */
    private Integer collectInterval;

    /**
     * 上次采集时间
     */
    private LocalDateTime lastCollectTime;

    /**
     * 设备状态（online-在线、offline-离线、unknown-未知）
     */
    private String status;

    /**
     * 最后告警时间
     */
    private LocalDateTime lastAlertTime;

    /**
     * 关联设备列表（JSON格式，如UPS关联的服务器）
     */
    private String relatedDevices;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 扩展字段（JSON格式）
     */
    private String extraData;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;
}
