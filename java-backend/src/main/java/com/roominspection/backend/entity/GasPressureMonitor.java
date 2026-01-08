package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 气体灭火系统压力监控实体
 */
@Data
@TableName("gas_pressure_monitor")
public class GasPressureMonitor {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 灭火系统ID
     */
    private Long systemId;

    /**
     * 系统编号
     */
    private String systemCode;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 机房ID
     */
    private Long roomId;

    /**
     * 灭火气体类型：1-七氟丙烷，2-IG541，3-二氧化碳
     */
    private Integer gasType;

    /**
     * 钢瓶编号
     */
    private String bottleCode;

    /**
     * 钢瓶位置
     */
    private String bottleLocation;

    /**
     * 额定压力(MPa)
     */
    private Double ratedPressure;

    /**
     * 当前压力(MPa)
     */
    private Double currentPressure;

    /**
     * 压力状态：0-正常，1-偏低，2-预警，3-异常
     */
    private Integer pressureStatus;

    /**
     * 预警阈值(MPa)
     */
    private Double alertThreshold;

    /**
     * 异常阈值(MPa)
     */
    private Double abnormalThreshold;

    /**
     * 压力传感器ID
     */
    private String sensorId;

    /**
     * 最后采集时间
     */
    private LocalDateTime lastCollectTime;

    /**
     * 状态：0-正常，1-告警，2-离线
     */
    private Integer status;

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
