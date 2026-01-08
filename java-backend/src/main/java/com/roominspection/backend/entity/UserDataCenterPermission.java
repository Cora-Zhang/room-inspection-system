package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户数据中心权限实体类
 */
@Data
@TableName("user_datacenter_permission")
public class UserDataCenterPermission {

    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 数据中心ID
     */
    private Long datacenterId;

    /**
     * 数据中心编码
     */
    private String datacenterCode;

    /**
     * 权限列表（JSON格式）
     * 例如: ["view", "edit", "delete", "export"]
     */
    private String permissionsJson;

    /**
     * 机房权限列表（JSON格式）
     * 空表示全部机房，否则只包含指定机房ID
     */
    private String roomIdsJson;

    /**
     * 设备权限列表（JSON格式）
     * 空表示全部设备，否则只包含指定设备ID
     */
    private String deviceIdsJson;

    /**
     * 权限等级（1:查看, 2:操作, 3:管理）
     */
    private Integer permissionLevel;

    /**
     * 是否可导出数据
     */
    private Boolean canExport;

    /**
     * 是否可打印
     */
    private Boolean canPrint;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    private LocalDateTime expireTime;

    /**
     * 状态（active/inactive）
     */
    private String status;

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
