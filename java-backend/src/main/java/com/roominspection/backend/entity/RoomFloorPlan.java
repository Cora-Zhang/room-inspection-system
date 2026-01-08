package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 机房平面图
 */
@Data
@TableName("room_floor_plans")
public class RoomFloorPlan {

    /**
     * 平面图ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 机房ID
     */
    private String roomId;

    /**
     * 机房名称
     */
    private String roomName;

    /**
     * 楼层
     */
    private String floor;

    /**
     * 平面图名称
     */
    private String name;

    /**
     * 平面图URL
     */
    private String imageUrl;

    /**
     * 平面图数据（JSON格式，包含区域、设备位置等信息）
     */
    private String planData;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 是否主图
     */
    private Boolean isMain;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态：ACTIVE-启用，INACTIVE-停用
     */
    private String status;

    /**
     * 创建人ID
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
