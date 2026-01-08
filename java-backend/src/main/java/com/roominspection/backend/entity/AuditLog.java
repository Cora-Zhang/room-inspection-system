package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 * 用于记录用户的所有操作，支持审计追溯
 */
@Data
@TableName("audit_log")
public class AuditLog {

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作人ID
     */
    private String userId;

    /**
     * 操作人用户名
     */
    private String username;

    /**
     * 操作人姓名
     */
    private String realName;

    /**
     * 操作人IP地址
     */
    private String ip;

    /**
     * 操作类型（LOGIN-登录、LOGOUT-登出、CREATE-创建、UPDATE-更新、DELETE-删除、QUERY-查询、EXPORT-导出、IMPORT-导入、APPROVE-审批）
     */
    private String operationType;

    /**
     * 操作模块（AUTH-认证、USER-用户管理、ROLE-角色管理、PERMISSION-权限管理、ROOM-机房管理、DEVICE-设备管理、INSPECTION-巡检管理、SHIFT-值班管理、WORKORDER-工单管理、MONITOR-监控管理、ALERT-告警管理、DOORACCESS-门禁管理、FIREPROTECTION-消防管理、ENERGY-能效管理、SYSTEM-系统管理）
     */
    private String module;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求URL
     */
    private String url;

    /**
     * 请求参数（加密存储）
     */
    private String params;

    /**
     * 响应结果（加密存储）
     */
    private String result;

    /**
     * 操作状态（SUCCESS-成功、FAILED-失败）
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionTime;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

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
