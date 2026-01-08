package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 监控配置实体
 */
@Data
@TableName("monitor_config")
public class MonitorConfig {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置编码（唯一）
     */
    private String configCode;

    /**
     * 设备类型：
     * SERVER-服务器
     * SWITCH-交换机
     * ROUTER-路由器
     * FIREWALL-防火墙
     * UPS-UPS电源
     * PDU-PDU配电
     * AIR_CONDITIONER-精密空调
     * SENSOR-传感器
     * ALL-所有类型
     */
    private String deviceType;

    /**
     * 采集频率（秒），支持1-86400秒（1秒到24小时）
     */
    private Integer collectionInterval;

    /**
     * 是否启用（0-禁用 1-启用）
     */
    private Integer status;

    /**
     * 并发数限制（同时采集的设备数量）
     */
    private Integer concurrencyLimit;

    /**
     * 超时时间（毫秒）
     */
    private Integer timeout;

    /**
     * 重试次数
     */
    private Integer retries;

    /**
     * 采集协议：SNMP, MODBUS, HTTP, MQTT, SCRIPT, CUSTOM
     */
    private String protocol;

    /**
     * 协议配置（JSON格式，存储协议相关配置）
     */
    private String protocolConfig;

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
