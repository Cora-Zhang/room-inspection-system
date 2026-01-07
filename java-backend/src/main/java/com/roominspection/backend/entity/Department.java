package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 部门实体
 */
@Data
@TableName("departments")
public class Department {

    /**
     * 部门ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 部门编码
     */
    private String code;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 父部门ID
     */
    private String parentId;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 层级路径
     */
    private String path;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态：ACTIVE-激活，INACTIVE-未激活
     */
    private String status;

    /**
     * 外部系统ID
     */
    private String externalId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
