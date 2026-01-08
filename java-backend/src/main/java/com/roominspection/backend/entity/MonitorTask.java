package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 监控任务实体
 */
@Data
@TableName("monitor_task")
public class MonitorTask {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID（唯一标识）
     */
    private String taskId;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 机房ID
     */
    private String roomId;

    /**
     * 机房名称
     */
    private String roomName;

    /**
     * 任务类型：
     * COLLECTION-数据采集
     * DISCOVERY-设备发现
     * CHECK-健康检查
     */
    private String taskType;

    /**
     * 任务状态：
     * PENDING-待执行
     * RUNNING-执行中
     * SUCCESS-成功
     * FAILED-失败
     * TIMEOUT-超时
     */
    private String taskStatus;

    /**
     * 采集开始时间
     */
    private LocalDateTime startTime;

    /**
     * 采集结束时间
     */
    private LocalDateTime endTime;

    /**
     * 采集耗时（毫秒）
     */
    private Long duration;

    /**
     * 采集的数据条数
     */
    private Integer dataCount;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
