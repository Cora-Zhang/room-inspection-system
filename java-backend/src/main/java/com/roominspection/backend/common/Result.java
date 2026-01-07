package com.roominspection.backend.common;

import lombok.Data;

/**
 * 统一响应结果
 */
@Data
public class Result<T> {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功响应（有数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 失败响应（自定义状态码）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 分页响应
     */
    public static <T> Result<PageResult<T>> page(PageResult<T> pageResult) {
        return new Result<>(200, "查询成功", pageResult);
    }
}

/**
 * 分页结果
 */
@Data
class PageResult<T> {
    private Long total;
    private List<T> records;
    private Integer current;
    private Integer size;
    private Integer pages;

    public PageResult(Long total, List<T> records, Integer current, Integer size) {
        this.total = total;
        this.records = records;
        this.current = current;
        this.size = size;
        this.pages = (int) Math.ceil((double) total / size);
    }
}
