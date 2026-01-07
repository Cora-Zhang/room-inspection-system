package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 热力图数据实体类
 */
@Data
@TableName("heatmap_data")
public class HeatmapData {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 机房ID
     */
    private Long roomId;

    /**
     * 热力图类型（TEMPERATURE-温度热力图、HUMIDITY-湿度热力图）
     */
    private String heatmapType;

    /**
     * 坐标X
     */
    private Double coordinateX;

    /**
     * 坐标Y
     */
    private Double coordinateY;

    /**
     * 温度值（热力图类型为温度时）
     */
    private Double temperature;

    /**
     * 湿度值（热力图类型为湿度时）
     */
    private Double humidity;

    /**
     * 热力值（0-1之间，用于热力图渲染）
     */
    private Double heatValue;

    /**
     * 是否异常区域（0-正常、1-异常）
     */
    private Integer isAbnormal;

    /**
     * 异常类型（HIGH_TEMP-高温、LOW_TEMP-低温、HIGH_HUMIDITY-高湿、LOW_HUMIDITY-低湿）
     */
    private String abnormalType;

    /**
     * 数据时间
     */
    private LocalDateTime dataTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
