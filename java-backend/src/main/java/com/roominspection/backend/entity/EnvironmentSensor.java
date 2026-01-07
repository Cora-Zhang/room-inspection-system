package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 环境传感器实体类
 */
@Data
@TableName("environment_sensor")
public class EnvironmentSensor {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 传感器名称
     */
    private String sensorName;

    /**
     * 传感器类型（TEMPERATURE-温湿度、WATER-漏水、SMOKE-烟感）
     */
    private String sensorType;

    /**
     * 设备ID（关联设备管理表）
     */
    private Long deviceId;

    /**
     * 机房ID（关联机房管理表）
     */
    private Long roomId;

    /**
     * 安装位置描述
     */
    private String location;

    /**
     * 坐标X（用于热力图绘制）
     */
    private Double coordinateX;

    /**
     * 坐标Y（用于热力图绘制）
     */
    private Double coordinateY;

    /**
     * 传感器IP地址
     */
    private String ipAddress;

    /**
     * 传感器端口
     */
    private Integer port;

    /**
     * 协议类型（MODBUS、SNMP、BACNET、HTTP）
     */
    private String protocolType;

    /**
     * 寄存器地址（Modbus）
     */
    private Integer registerAddress;

    /**
     * SNMP OID
     */
    private String snmpOid;

    /**
     * 采集频率（秒）
     */
    private Integer collectInterval;

    /**
     * 温度阈值上限（℃）
     */
    private Double tempThresholdHigh;

    /**
     * 温度阈值下限（℃）
     */
    private Double tempThresholdLow;

    /**
     * 湿度阈值上限（%）
     */
    private Double humidityThresholdHigh;

    /**
     * 湿度阈值下限（%）
     */
    private Double humidityThresholdLow;

    /**
     * 状态（NORMAL-正常、ALARM-告警、OFFLINE-离线）
     */
    private String status;

    /**
     * 最后采集时间
     */
    private LocalDateTime lastCollectTime;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private Long updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
