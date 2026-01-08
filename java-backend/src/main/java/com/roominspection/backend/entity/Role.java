package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色实体
 * 用于RBAC权限管理
 */
@Data
@TableName("role")
public class Role {

    /**
     * 角色ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色编码（如：ADMIN、OPERATOR、VIEWER、INSPECTOR、MAINTAINER）
     */
    private String roleCode;

    /**
     * 角色名称（如：系统管理员、操作员、查看员、巡检员、维修员）
     */
    private String roleName;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 角色级别（1-超级管理员、2-管理员、3-主管、4-普通用户、5-访客）
     */
    private Integer level;

    /**
     * 数据权限范围（ALL-全部、DEPT-本部门及以下、DEPT_ONLY-本部门、SELF-仅本人、CUSTOM-自定义）
     */
    private String dataScope;

    /**
     * 自定义数据权限部门ID（逗号分隔）
     */
    private String dataScopeDeptIds;

    /**
     * 角色状态（ACTIVE-激活、INACTIVE-停用）
     */
    private String status;

    /**
     * 排序
     */
    private Integer sortOrder;

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
}
