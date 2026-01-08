package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 健康检查记录
 */
@Data
@TableName("health_check")
public class HealthCheck {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务实例ID
     */
    private String instanceId;

    /**
     * 服务地址
     */
    private String serviceAddress;

    /**
     * 检查类型（HEARTBEAT/RESOURCE/DATABASE/REDIS/MQ）
     */
    private String checkType;

    /**
     * 检查状态（UP/DOWN/UNKNOWN）
     */
    private String status;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 检查详情（JSON格式）
     */
    private String details;

    /**
     * 检查时间
     */
    private LocalDateTime checkTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
