package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 服务实例
 */
@Data
@TableName("service_instance")
public class ServiceInstance {

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
     * 服务地址（IP:PORT）
     */
    private String serviceAddress;

    /**
     * 服务状态（UP/DOWN/STARTING/STOPPING）
     */
    private String status;

    /**
     * 健康状态（HEALTHY/UNHEALTHY/DEGRADED）
     */
    private String healthStatus;

    /**
     * 实例权重（负载均衡使用）
     */
    private Integer weight;

    /**
     * 是否为主节点
     */
    private Boolean isPrimary;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeat;

    /**
     * 启动时间
     */
    private LocalDateTime startTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
