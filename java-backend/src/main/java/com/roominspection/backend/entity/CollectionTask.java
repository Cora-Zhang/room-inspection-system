package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 采集任务实体类
 */
@Data
@TableName("collection_task")
public class CollectionTask {

    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务编号
     */
    private String taskNo;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务类型（once:单次, schedule:定时）
     */
    private String taskType;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 采集节点ID
     */
    private String collectorId;

    /**
     * 采集节点名称
     */
    private String collectorName;

    /**
     * 采集协议
     */
    private String protocol;

    /**
     * 采集指标列表（JSON格式）
     */
    private String metricsJson;

    /**
     * 采集频率（秒）
     */
    private Integer frequency;

    /**
     * Cron表达式（定时任务）
     */
    private String cronExpression;

    /**
     * 任务状态（pending:待执行, running:执行中, success:成功, failed:失败, cancelled:已取消）
     */
    private String status;

    /**
     * 优先级（1-10，10最高）
     */
    private Integer priority;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 计划执行时间
     */
    private LocalDateTime scheduledTime;

    /**
     * 实际开始时间
     */
    private LocalDateTime startTime;

    /**
     * 实际结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行时长（毫秒）
     */
    private Long duration;

    /**
     * 采集结果（JSON格式）
     */
    private String resultJson;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;
}
