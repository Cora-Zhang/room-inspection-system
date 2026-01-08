package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 灭火器实体
 */
@Data
@TableName("fire_extinguisher")
public class FireExtinguisher {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 灭火器编号
     */
    private String extinguisherCode;

    /**
     * 灭火器名称
     */
    private String extinguisherName;

    /**
     * 机房ID
     */
    private Long roomId;

    /**
     * 安装位置
     */
    private String location;

    /**
     * 灭火器类型：1-干粉，2-二氧化碳，3-七氟丙烷，4-其他
     */
    private Integer extinguisherType;

    /**
     * 规格(如：4kg、50kg)
     */
    private String specification;

    /**
     * 生产厂家
     */
    private String manufacturer;

    /**
     * 出厂日期
     */
    private LocalDateTime manufactureDate;

    /**
     * 上次充装日期
     */
    private LocalDateTime lastRefillDate;

    /**
     * 下次充装日期
     */
    private LocalDateTime nextRefillDate;

    /**
     * 额定压力(MPa)
     */
    private Double ratedPressure;

    /**
     * 当前压力(MPa)
     */
    private Double currentPressure;

    /**
     * 压力状态：0-正常，1-偏低，2-偏低预警，3-异常
     */
    private Integer pressureStatus;

    /**
     * 重量(kg)
     */
    private Double weight;

    /**
     * 当前重量(kg)，来自智能称重传感器
     */
    private Double currentWeight;

    /**
     * 重量状态：0-正常，1-偏低，2-偏低预警，3-异常
     */
    private Integer weightStatus;

    /**
     * 压力表类型：0-模拟表，1-数字压力表
     */
    private Integer pressureMeterType;

    /**
     * 是否支持智能称重：0-不支持，1-支持
     */
    private Integer supportWeighing;

    /**
     * 数字压力表传感器ID
     */
    private String pressureSensorId;

    /**
     * 智能称重传感器ID
     */
    private String weighingSensorId;

    /**
     * 压力预警阈值(MPa)，低于此值触发预警
     */
    private Double pressureAlertThreshold;

    /**
     * 压力异常阈值(MPa)，低于此值触发异常
     */
    private Double pressureAbnormalThreshold;

    /**
     * 重量预警阈值(kg)，低于此值触发预警
     */
    private Double weightAlertThreshold;

    /**
     * 重量异常阈值(kg)，低于此值触发异常
     */
    private Double weightAbnormalThreshold;

    /**
     * 状态：0-正常，1-告警，2-离线
     */
    private Integer status;

    /**
     * 最后采集时间
     */
    private LocalDateTime lastCollectTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;
}
