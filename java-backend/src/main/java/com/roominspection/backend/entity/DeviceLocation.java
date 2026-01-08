package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 设备位置信息
 */
@Data
@TableName("device_locations")
public class DeviceLocation {

    /**
     * 位置ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型：SERVER-服务器，SWITCH-交换机，ROUTER-路由器，FIREWALL-防火墙，UPS-UPS，PDU-PDU，AIR_CONDITIONER-空调，SENSOR-传感器
     */
    private String deviceType;

    /**
     * 机房ID
     */
    private String roomId;

    /**
     * 平面图ID
     */
    private String floorPlanId;

    /**
     * 区域编号
     */
    private String zone;

    /**
     * 机柜编号
     */
    private String cabinet;

    /**
     * U位（设备在机柜中的位置）
     */
    private String uPosition;

    /**
     * X坐标（在平面图中的位置）
     */
    private Double xCoordinate;

    /**
     * Y坐标（在平面图中的位置）
     */
    private Double yCoordinate;

    /**
     * 设备状态：NORMAL-正常，WARNING-告警，ERROR-故障，OFFLINE-离线
     */
    private String status;

    /**
     * 最后告警时间
     */
    private LocalDateTime lastAlarmTime;

    /**
     * 备注信息
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
