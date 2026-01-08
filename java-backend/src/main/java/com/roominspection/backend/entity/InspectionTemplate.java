package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 巡检模板实体类
 */
@Data
@TableName("inspection_template")
public class InspectionTemplate {

    /**
     * 模板ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板编码（唯一标识）
     */
    private String templateCode;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 是否系统模板
     */
    private Boolean isSystem;

    /**
     * 状态（active/inactive）
     */
    private String status;

    /**
     * 巡检项列表（JSON格式存储）
     */
    private String itemsJson;

    /**
     * 预计耗时（分钟）
     */
    private Integer estimatedTime;

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

    /**
     * 使用次数
     */
    private Long useCount;
}
