package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 设备实体
 */
@Data
@TableName("devices")
public class Device {

    /**
     * 设备ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 设备编号
     */
    private String code;

    /**
     * 设备名称
     */
    private String name;

    /**
     * 设备类型：SERVER-服务器，SWITCH-交换机，ROUTER-路由器，FIREWALL-防火墙，STORAGE-存储设备，UPS-UPS，空调-空调，温湿度-温湿度传感器，PDU-PDU
     */
    private String type;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 型号
     */
    private String model;

    /**
     * 序列号
     */
    private String serialNumber;

    /**
     * 机房ID
     */
    private String roomId;

    /**
     * 机柜编号
     */
    private String rackCode;

    /**
     * U位
     */
    private Integer uPosition;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * MAC地址
     */
    private String macAddress;

    /**
     * 监控协议：SNMP，MODBUS，BMS，SENSOR，FIRE
     */
    private String protocol;

    /**
     * 监控参数
     */
    private String monitorParams;

    /**
     * 门禁设备ID
     */
    private String accessControlDeviceId;

    /**
     * 设备状态：ONLINE-在线，OFFLINE-离线，FAULT-故障，MAINTENANCE-维护中
     */
    private String status;

    /**
     * 负责人ID
     */
    private String managerId;

    /**
     * 采购日期
     */
    private LocalDateTime purchaseDate;

    /**
     * 保修到期日期
     */
    private LocalDateTime warrantyExpireDate;

    /**
     * 描述
     */
    private String description;

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
