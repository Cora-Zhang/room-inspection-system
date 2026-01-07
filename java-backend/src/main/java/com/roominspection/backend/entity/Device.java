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
     * 设备类型：SERVER-服务器，SWITCH-交换机，ROUTER-路由器，FIREWALL-防火墙，STORAGE-存储设备，UPS-UPS，AC-空调，SENSOR-温湿度传感器，PDU-PDU
     */
    private String type;

    /**
     * 设备子类型（扩展分类）：
     * 服务器：APPLICATION-应用服务器，DATABASE-数据库服务器，WEB-Web服务器，VIRTUAL-虚拟化服务器
     * 交换机：CORE-核心交换机，AGGREGATION-汇聚交换机，ACCESS-接入交换机
     * 路由器：CORE-核心路由器，EDGE-边缘路由器
     * 防火墙：PERIMETER-边界防火墙，INTERNAL-内部防火墙
     */
    private String subType;

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
     * 机房名称
     */
    private String roomName;

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
     * SNMP版本（v1, v2c, v3）
     */
    private String snmpVersion;

    /**
     * SNMP Community字符串
     */
    private String snmpCommunity;

    /**
     * SNMP端口号
     */
    private Integer snmpPort;

    /**
     * 监控参数（JSON格式，存储设备特定的监控配置）
     */
    private String monitorParams;

    /**
     * 是否关键设备（0-否 1-是，关键设备需要拍照巡检）
     */
    private Integer isKeyDevice;

    /**
     * 巡检周期（HOURLY-每小时，DAILY-每日，WEEKLY-每周，MONTHLY-每月，CUSTOM-自定义）
     */
    private String inspectionCycle;

    /**
     * 自定义巡检间隔（分钟）
     */
    private Integer customInspectionInterval;

    /**
     * CPU使用率告警阈值（%）
     */
    private Double cpuThreshold;

    /**
     * 内存使用率告警阈值（%）
     */
    private Double memoryThreshold;

    /**
     * 磁盘使用率告警阈值（%）
     */
    private Double diskThreshold;

    /**
     * 流量异常波动告警阈值（%）
     */
    private Double trafficThreshold;

    /**
     * 端口异常告警（0-禁用 1-启用）
     */
    private Integer portDownAlertEnabled;

    /**
     * 设备标签信息（用于OCR识别，JSON格式存储标签坐标等信息）
     */
    private String deviceLabels;

    /**
     * 设备照片URL（最新巡检照片）
     */
    private String latestPhotoUrl;

    /**
     * 上次巡检时间
     */
    private LocalDateTime lastInspectionTime;

    /**
     * 下次巡检时间
     */
    private LocalDateTime nextInspectionTime;

    /**
     * 门禁设备ID
     */
    private String accessControlDeviceId;

    /**
     * 设备状态：ONLINE-在线，OFFLINE-离线，FAULT-故障，MAINTENANCE-维护中
     */
    private String status;

    /**
     * 当前CPU使用率（%）
     */
    private Double currentCpuUsage;

    /**
     * 当前内存使用率（%）
     */
    private Double currentMemoryUsage;

    /**
     * 当前磁盘使用率（%）
     */
    private Double currentDiskUsage;

    /**
     * 最近状态更新时间
     */
    private LocalDateTime lastStatusUpdateTime;

    /**
     * 负责人ID
     */
    private String managerId;

    /**
     * 负责人姓名
     */
    private String managerName;

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
