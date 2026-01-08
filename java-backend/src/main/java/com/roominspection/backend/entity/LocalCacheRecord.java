package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 本地缓存记录（监控服务故障时的本地缓存）
 */
@Data
@TableName("local_cache_record")
public class LocalCacheRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 缓存类型（MONITOR_DATA/ALARM/CONFIG）
     */
    private String cacheType;

    /**
     * 数据源类型（DEVICE/SENSOR/ALARM）
     */
    private String dataSourceType;

    /**
     * 数据源ID
     */
    private Long dataSourceId;

    /**
     * 数据内容（JSON格式）
     */
    private String dataContent;

    /**
     * 数据时间戳
     */
    private Long dataTimestamp;

    /**
     * 缓存状态（PENDING/SYNCED/FAILED）
     */
    private String status;

    /**
     * 同步失败次数
     */
    private Integer syncFailCount;

    /**
     * 最后同步时间
     */
    private LocalDateTime lastSyncTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
