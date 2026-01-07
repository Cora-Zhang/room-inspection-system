package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 设备巡检记录实体
 */
@Data
@TableName("device_inspection_record")
public class DeviceInspectionRecord {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 巡检编号
     */
    private String code;

    /**
     * 设备ID
     */
    private String deviceId;

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
     * 机房ID
     */
    private String roomId;

    /**
     * 机房名称
     */
    private String roomName;

    /**
     * 巡检类型：AUTO-自动巡检，MANUAL-手动巡检
     */
    private String inspectionType;

    /**
     * 巡检方式：SNMP-SNMP采集，SCRIPT-脚本执行，PHOTO-拍照巡检，MANUAL-手动录入
     */
    private String inspectionMethod;

    /**
     * 巡检开始时间
     */
    private LocalDateTime startTime;

    /**
     * 巡检结束时间
     */
    private LocalDateTime endTime;

    /**
     * 巡检人ID
     */
    private String inspectorId;

    /**
     * 巡检人姓名
     */
    private String inspectorName;

    /**
     * 巡检结果：NORMAL-正常，WARNING-警告，ERROR-错误
     */
    private String result;

    /**
     * CPU使用率（%）
     */
    private Double cpuUsage;

    /**
     * 内存使用率（%）
     */
    private Double memoryUsage;

    /**
     * 磁盘使用率（%）
     */
    private Double diskUsage;

    /**
     * 巡检数据（JSON格式，存储详细的巡检指标）
     */
    private String inspectionData;

    /**
     * 异常信息
     */
    private String issues;

    /**
     * 处理建议
     */
    private String suggestions;

    /**
     * 照片URL列表（逗号分隔）
     */
    private String photoUrls;

    /**
     * 是否已OCR识别（0-否 1-是）
     */
    private Integer ocrRecognized;

    /**
     * OCR识别结果（设备标签识别信息，JSON格式）
     */
    private String ocrResult;

    /**
     * 是否包含水印（0-否 1-是）
     */
    private Integer hasWatermark;

    /**
     * 水印信息（巡检人员、时间、设备信息，JSON格式）
     */
    private String watermarkInfo;

    /**
     * 脚本执行结果（自定义脚本执行输出）
     */
    private String scriptResult;

    /**
     * 是否告警（0-否 1-是）
     */
    private Integer isAlerted;

    /**
     * 告警ID列表（逗号分隔）
     */
    private String alertIds;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新人ID
     */
    private Long updatedBy;

    /**
     * 更新人姓名
     */
    private String updatedByName;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
