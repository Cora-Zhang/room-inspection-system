package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 接口配置实体类
 * 用于统一管理系统中的各种API配置
 */
@Data
@TableName("api_config")
public class ApiConfig {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置类型
     * DINGTALK-钉钉, SMS-短信, EMAIL-邮件,
     * HIKVISION-海康门禁, DAHUA-大华门禁,
     * SNMP-SNMP监控, MODBUS-Modbus监控, BMS-BMS接口,
     * SENSOR-传感器网络, FIRE-消防主机, OTHER-其他
     */
    private String configType;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值（敏感信息需加密存储）
     */
    private String configValue;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 是否敏感配置（0-否 1-是）
     */
    private Integer isSensitive;

    /**
     * 配置分组
     */
    private String configGroup;

    /**
     * 是否启用（0-禁用 1-启用）
     */
    private Integer status;

    /**
     * 排序序号
     */
    private Integer sortOrder;

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

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展JSON字段（存储额外的配置信息）
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Object extraConfig;
}
