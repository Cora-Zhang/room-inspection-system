package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门禁权限实体
 * 用于管理值班人员的门禁访问权限
 */
@Data
@TableName("door_access_permission")
public class DoorAccessPermission {

    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 值班人员ID
     */
    private Long staffId;

    /**
     * 值班人员姓名
     */
    private String staffName;

    /**
     * 值班人员工号
     */
    private String staffNo;

    /**
     * 机房ID
     */
    private Long roomId;

    /**
     * 机房名称
     */
    private String roomName;

    /**
     * 门禁设备ID（海康/大华设备ID）
     */
    private String deviceId;

    /**
     * 门禁卡号
     */
    private String cardNo;

    /**
     * 权限类型（1-值班权限 2-临时权限）
     */
    private Integer permissionType;

    /**
     * 生效开始时间
     */
    private LocalDateTime effectiveStartTime;

    /**
     * 生效结束时间
     */
    private LocalDateTime effectiveEndTime;

    /**
     * 权限状态（0-未生效 1-生效中 2-已过期 3-已回收）
     */
    private Integer status;

    /**
     * 下发状态（0-未下发 1-已下发 2-下发失败）
     */
    private Integer syncStatus;

    /**
     * 下发时间
     */
    private LocalDateTime syncTime;

    /**
     * 门禁系统类型（1-海康 2-大华）
     */
    private Integer doorSystemType;

    /**
     * 回收时间
     */
    private LocalDateTime revokeTime;

    /**
     * 回收原因
     */
    private String revokeReason;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    private Integer deleted;
}
