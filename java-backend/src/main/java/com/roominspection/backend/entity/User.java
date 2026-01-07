package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("users")
public class User {

    /**
     * 用户ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码（加密后）
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 部门ID
     */
    private String departmentId;

    /**
     * 职位
     */
    private String position;

    /**
     * 员工工号
     */
    private String employeeId;

    /**
     * 用户状态：ACTIVE-激活，INACTIVE-未激活，LOCKED-锁定，DELETED-已删除
     */
    private String status;

    /**
     * 用户来源：LOCAL-本地创建，LDAP-LDAP同步，SSO-SSO登录创建，HR_SYSTEM-HR系统同步，IMPORT-手动导入
     */
    private String source;

    /**
     * 外部系统ID（LDAP/HR系统ID）
     */
    private String externalId;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 失败登录次数
     */
    private Integer failedLoginCount;

    /**
     * 锁定时间
     */
    private LocalDateTime lockedUntil;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
