package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 精密空调运行数据实体
 */
@Data
@TableName("air_conditioner_data")
public class AirConditionerData {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 空调ID
     */
    private Long acId;

    /**
     * 空调编号
     */
    private String acCode;

    /**
     * 运行模式：0-关闭，1-制冷，2-制热，3-除湿，4-通风
     */
    private Integer runMode;

    /**
     * 设定温度(℃)
     */
    private Double setTemperature;

    /**
     * 回风温度(℃)
     */
    private Double returnTemperature;

    /**
     * 回风湿度(%)
     */
    private Double returnHumidity;

    /**
     * 送风温度(℃)
     */
    private Double supplyTemperature;

    /**
     * 压缩机1状态：0-停止，1-运行
     */
    private Integer compressor1Status;

    /**
     * 压缩机2状态：0-停止，1-运行
     */
    private Integer compressor2Status;

    /**
     * 风机1状态：0-停止，1-运行
     */
    private Integer fan1Status;

    /**
     * 风机2状态：0-停止，1-运行
     */
    private Integer fan2Status;

    /**
     * 加湿器状态：0-停止，1-运行
     */
    private Integer humidifierStatus;

    /**
     * 电加热器状态：0-关闭，1-开启
     */
    private Integer heaterStatus;

    /**
     * 当前功率(kW)
     */
    private Double currentPower;

    /**
     * 温差：回风温度 - 设定温度
     */
    private Double temperatureDiff;

    /**
     * 是否温差异常：0-正常，1-异常
     */
    private Integer isDiffAbnormal;

    /**
     * 采集时间
     */
    private LocalDateTime collectTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
