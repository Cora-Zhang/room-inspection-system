package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 巡检模板项实体类
 */
@Data
@TableName("inspection_template_item")
public class InspectionTemplateItem {

    /**
     * 模板项ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 巡检项名称
     */
    private String itemName;

    /**
     * 巡检项编码
     */
    private String itemCode;

    /**
     * 巡检项类型（numeric:数值型, status:状态型, text:文本型, image:图片型）
     */
    private String itemType;

    /**
     * 单位（数值型有效）
     */
    private String unit;

    /**
     * 最小值（数值型有效）
     */
    private Double minValue;

    /**
     * 最大值（数值型有效）
     */
    private Double maxValue;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 选项列表（状态型有效，逗号分隔）
     */
    private String options;

    /**
     * 是否必填
     */
    private Boolean required;

    /**
     * 巡检说明
     */
    private String description;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
