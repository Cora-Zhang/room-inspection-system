package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 精密空调实体
 */
@Data
@TableName("air_conditioner")
public class AirConditioner {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 空调编号
     */
    private String acCode;

    /**
     * 空调名称
     */
    private String acName;

    /**
     * 机房ID
     */
    private Long roomId;

    /**
     * 安装位置
     */
    private String location;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 型号
     */
    private String model;

    /**
     * 制冷量(kW)
     */
    private Double coolingCapacity;

    /**
     * 当前运行模式：0-关闭，1-制冷，2-制热，3-除湿，4-通风
     */
    private Integer runMode;

    /**
     * 设定温度(℃)
     */
    private Double setTemperature;

    /**
     * 当前回风温度(℃)
     */
    private Double currentReturnTemp;

    /**
     * 当前回风湿度(%)
     */
    private Double currentReturnHumidity;

    /**
     * 压缩机累计运行时长(小时)
     */
    private Double compressorRuntimeHours;

    /**
     * 上次保养时间
     */
    private LocalDateTime lastMaintenanceTime;

    /**
     * 下次保养时间
     */
    private LocalDateTime nextMaintenanceTime;

    /**
     * 保养阈值(小时)，达到此阈值自动触发保养工单
     */
    private Double maintenanceThreshold;

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
