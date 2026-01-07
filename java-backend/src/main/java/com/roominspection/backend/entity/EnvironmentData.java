package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 环境数据实体类
 */
@Data
@TableName("environment_data")
public class EnvironmentData {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 传感器ID
     */
    private Long sensorId;

    /**
     * 机房ID
     */
    private Long roomId;

    /**
     * 数据类型（TEMPERATURE-温度、HUMIDITY-湿度、WATER-漏水、SMOKE-烟感）
     */
    private String dataType;

    /**
     * 数值
     */
    private Double value;

    /**
     * 单位（℃、%、无单位）
     */
    private String unit;

    /**
     * 是否告警（0-正常、1-告警）
     */
    private Integer isAlarm;

    /**
     * 告警级别（INFO-信息、WARNING-警告、CRITICAL-严重）
     */
    private String alarmLevel;

    /**
     * 告警信息
     */
    private String alarmMessage;

    /**
     * 采集时间
     */
    private LocalDateTime collectTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
