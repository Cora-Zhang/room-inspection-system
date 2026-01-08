package com.roominspection.backend.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 操作类型（LOGIN-登录、LOGOUT-登出、CREATE-创建、UPDATE-更新、DELETE-删除、QUERY-查询、EXPORT-导出、IMPORT-导入、APPROVE-审批）
     */
    String operationType() default "QUERY";

    /**
     * 操作模块（AUTH-认证、USER-用户管理、ROLE-角色管理、PERMISSION-权限管理、ROOM-机房管理、DEVICE-设备管理、INSPECTION-巡检管理、SHIFT-值班管理、WORKORDER-工单管理、MONITOR-监控管理、ALERT-告警管理、DOORACCESS-门禁管理、FIREPROTECTION-消防管理、ENERGY-能效管理、SYSTEM-系统管理）
     */
    String module();

    /**
     * 操作描述
     */
    String description();

    /**
     * 是否记录请求参数
     */
    boolean logParams() default true;

    /**
     * 是否记录响应结果
     */
    boolean logResult() default false;

    /**
     * 是否异步记录
     */
    boolean async() default true;
}
