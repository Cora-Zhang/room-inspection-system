package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 数据库备份记录
 */
@Data
@TableName("database_backup")
public class DatabaseBackup {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 备份名称
     */
    private String backupName;

    /**
     * 备份文件路径
     */
    private String backupPath;

    /**
     * 备份文件大小（字节）
     */
    private Long fileSize;

    /**
     * 备份类型（FULL/INCREMENTAL/DIFFERENTIAL）
     */
    private String backupType;

    /**
     * 备份状态（SUCCESS/FAILED/IN_PROGRESS/CANCELLED）
     */
    private String status;

    /**
     * 备份开始时间
     */
    private LocalDateTime startTime;

    /**
     * 备份结束时间
     */
    private LocalDateTime endTime;

    /**
     * 备份耗时（秒）
     */
    private Long duration;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 备份描述
     */
    private String description;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否自动备份
     */
    private Boolean isAutoBackup;
}
