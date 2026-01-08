package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 性能指标实体
 */
@Data
@TableName("performance_metric")
public class PerformanceMetric {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 指标类型：
     * API_RESPONSE- API响应时间
     * DB_QUERY- 数据库查询时间
     * MONITOR_COLLECT- 监控数据采集时间
     * WORK_ORDER_PROCESS- 工单处理时间
     * CACHE_HIT- 缓存命中率
     * MEMORY_USAGE- 内存使用率
     * CPU_USAGE- CPU使用率
     */
    private String metricType;

    /**
     * 指标值
     */
    private Double metricValue;

    /**
     * 单位
     */
    private String unit;

    /**
     * 关联对象ID（如API路径、设备ID、工单ID等）
     */
    private String objectId;

    /**
     * 关联对象类型
     */
    private String objectType;

    /**
     * 阈值上限
     */
    private Double thresholdUpper;

    /**
     * 是否超出阈值
     */
    private Integer exceededThreshold;

    /**
     * 额外信息（JSON格式）
     */
    private String extraInfo;

    /**
     * 记录时间
     */
    private LocalDateTime recordTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
