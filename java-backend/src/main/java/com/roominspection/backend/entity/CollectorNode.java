package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 采集节点实体类
 */
@Data
@TableName("collector_node")
public class CollectorNode {

    /**
     * 节点ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 节点编号
     */
    private String nodeCode;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点IP地址
     */
    private String ipAddress;

    /**
     * 节点端口
     */
    private Integer port;

    /**
     * 节点类型（master:主节点, slave:从节点）
     */
    private String nodeType;

    /**
     * 最大并发任务数
     */
    private Integer maxConcurrentTasks;

    /**
     * 当前任务数
     */
    private Integer currentTasks;

    /**
     * 节点状态（online:在线, offline:离线, busy:忙碌）
     */
    private String status;

    /**
     * CPU使用率（百分比）
     */
    private Double cpuUsage;

    /**
     * 内存使用率（百分比）
     */
    private Double memoryUsage;

    /**
     * 网络使用率（百分比）
     */
    private Double networkUsage;

    /**
     * 磁盘使用率（百分比）
     */
    private Double diskUsage;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatTime;

    /**
     * 采集成功率（百分比）
     */
    private Double successRate;

    /**
     * 采集总次数
     */
    private Long totalCollections;

    /**
     * 采集失败次数
     */
    private Long failedCollections;

    /**
     * 所在机房ID
     */
    private Long roomId;

    /**
     * 所在机房名称
     */
    private String roomName;

    /**
     * 地理位置经度
     */
    private Double longitude;

    /**
     * 地理位置纬度
     */
    private Double latitude;

    /**
     * 备注
     */
    private String remark;

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

    /**
     * 更新人
     */
    private String updateBy;
}
