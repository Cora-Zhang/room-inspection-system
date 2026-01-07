package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 电力趋势实体
 * 用于存储电力系统的趋势分析数据和容量预警报告
 */
@Data
@TableName("power_trend")
public class PowerTrend {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 电力设备ID
     */
    private Long powerSystemId;

    /**
     * 设备编号
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 统计日期
     */
    private LocalDate statisticDate;

    /**
     * 统计周期（hourly-小时、daily-日、weekly-周、monthly-月）
     */
    private String statisticPeriod;

    /**
     * 采集样本数
     */
    private Integer sampleCount;

    // ========== 负载统计 ==========
    /**
     * 平均负载百分比（%）
     */
    private BigDecimal avgLoadPercent;

    /**
     * 最大负载百分比（%）
     */
    private BigDecimal maxLoadPercent;

    /**
     * 最小负载百分比（%）
     */
    private BigDecimal minLoadPercent;

    /**
     * 峰值负载时间
     */
    private LocalDateTime peakLoadTime;

    /**
     * 负载标准差
     */
    private BigDecimal loadStdDev;

    // ========== 功率统计 ==========
    /**
     * 平均有功功率（kW）
     */
    private BigDecimal avgActivePower;

    /**
     * 最大有功功率（kW）
     */
    private BigDecimal maxActivePower;

    /**
     * 累计耗电量（kWh）
     */
    private BigDecimal totalEnergyConsumption;

    // ========== 趋势分析 ==========
    /**
     * 负载变化趋势（up-上升、down-下降、stable-稳定）
     */
    private String loadTrend;

    /**
     * 连续上升天数
     */
    private Integer consecutiveUpDays;

    /**
     * 连续下降天数
     */
    private Integer consecutiveDownDays;

    /**
     * 7日负载增长率（%）
     */
    private BigDecimal sevenDayGrowthRate;

    /**
     * 30日负载增长率（%）
     */
    private BigDecimal thirtyDayGrowthRate;

    // ========== 预警分析 ==========
    /**
     * 是否需要容量预警
     */
    private Boolean needCapacityAlert;

    /**
     * 预警等级（info-信息、warning-警告、critical-严重）
     */
    private String alertLevel;

    /**
     * 预警阈值（%）
     */
    private BigDecimal alertThreshold;

    /**
     * 预计达到阈值天数
     */
    private Integer daysToThreshold;

    /**
     * 预警报告（Markdown格式）
     */
    private String alertReport;

    /**
     * 建议措施（JSON格式，存储改进建议列表）
     */
    private String suggestions;

    // ========== 容量分析 ==========
    /**
     * 当前容量利用率（%）
     */
    private BigDecimal capacityUtilization;

    /**
     * 剩余容量（kW）
     */
    private BigDecimal remainingCapacity;

    /**
     * 预计满载天数
     */
    private Integer estimatedDaysToFullLoad;

    /**
     * 扩容建议（JSON格式）
     */
    private String expansionSuggestion;

    /**
     * 扩展字段（JSON格式）
     */
    private String extraData;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;
}
