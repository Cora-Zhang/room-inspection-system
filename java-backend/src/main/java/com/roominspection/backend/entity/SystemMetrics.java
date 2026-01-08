package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统监控指标
 */
@Data
@TableName("system_metrics")
public class SystemMetrics {

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
     * CPU使用率（%）
     */
    private Double cpuUsage;

    /**
     * 内存使用率（%）
     */
    private Double memoryUsage;

    /**
     * 磁盘使用率（%）
     */
    private Double diskUsage;

    /**
     * JVM堆内存使用率（%）
     */
    private Double jvmHeapUsage;

    /**
     * 线程数
     */
    private Integer threadCount;

    /**
     * 请求总数
     */
    private Long requestCount;

    /**
     * 错误数
     */
    private Long errorCount;

    /**
     * 平均响应时间（毫秒）
     */
    private Double avgResponseTime;

    /**
     * 指标采集时间
     */
    private LocalDateTime metricsTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
