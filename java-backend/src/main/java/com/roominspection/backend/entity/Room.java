package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 机房实体
 */
@Data
@TableName("rooms")
public class Room {

    /**
     * 机房ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 机房编号
     */
    private String code;

    /**
     * 机房名称
     */
    private String name;

    /**
     * 机房类型：IDC-数据中心机房，EDGE-边缘机房，CORE-核心机房
     */
    private String type;

    /**
     * 位置
     */
    private String location;

    /**
     * 楼层
     */
    private String floor;

    /**
     * 面积（平方米）
     */
    private Double area;

    /**
     * 容量（机柜数）
     */
    private Integer capacity;

    /**
     * 负责人ID
     */
    private String managerId;

    /**
     * 管理员ID
     */
    private String adminId;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 机房状态：NORMAL-正常，MAINTENANCE-维护中，OFFLINE-停用
     */
    private String status;

    /**
     * 温度阈值（℃）
     */
    private Double temperatureThreshold;

    /**
     * 湿度阈值（%）
     */
    private Double humidityThreshold;

    /**
     * 描述
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
