package com.roominspection.backend.common;

import java.lang.annotation.*;

/**
 * API版本控制注解
 * 用于标记接口的API版本
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiVersion {

    /**
     * API版本号
     * 例如: "v1", "v2"
     */
    String value() default ApiConstants.DEFAULT_VERSION;

    /**
     * 是否支持多版本
     * 如果为true，则该接口可以同时被多个版本访问
     */
    boolean multiVersion() default false;
}
