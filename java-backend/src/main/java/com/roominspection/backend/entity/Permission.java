package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限实体
 * 用于RBAC权限管理，采用树形结构
 */
@Data
@TableName("permission")
public class Permission {

    /**
     * 权限ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父权限ID（0表示根节点）
     */
    private Long parentId;

    /**
     * 权限类型（MENU-菜单、BUTTON-按钮、API-接口）
     */
    private String type;

    /**
     * 权限编码（如：user:create、user:update、inspection:approve）
     */
    private String permissionCode;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 路由路径（前端路由）
     */
    private String path;

    /**
     * 组件路径（前端组件路径）
     */
    private String component;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 是否可见（true-显示、false-隐藏）
     */
    private Boolean visible;

    /**
     * 是否缓存（true-缓存、false-不缓存）
     */
    private Boolean keepAlive;

    /**
     * 是否外链（true-外链、false-内链）
     */
    private Boolean isFrame;

    /**
     * 权限状态（ACTIVE-激活、INACTIVE-停用）
     */
    private String status;

    /**
     * 创建人ID
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否删除
     */
    @TableLogic
    private Boolean deleted;

    /**
     * 子权限列表（非数据库字段，用于树形结构）
     */
    @TableField(exist = false)
    private List<Permission> children;
}
