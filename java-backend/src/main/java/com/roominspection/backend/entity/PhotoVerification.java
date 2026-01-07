package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 照片核验实体
 * 用于核验巡检上传的设备照片，支持质量检查、OCR识别、一致性核验
 */
@Data
@TableName("photo_verification")
public class PhotoVerification {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联巡检任务ID
     */
    private Long inspectionTaskId;

    /**
     * 关联设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型（server-服务器、switch-交换机、router-路由器、firewall-防火墙等）
     */
    private String deviceType;

    /**
     * 照片OSS存储路径
     */
    private String photoPath;

    /**
     * 照片URL（访问地址）
     */
    private String photoUrl;

    /**
     * 上传人员ID
     */
    private Long uploaderId;

    /**
     * 上传人员姓名
     */
    private String uploaderName;

    /**
     * 拍照时间（从照片EXIF或上传时间）
     */
    private LocalDateTime photoTime;

    /**
     * 拍摄位置（基于门禁记录或GPS）
     */
    private String photoLocation;

    /**
     * 照片分辨率（如：1920x1080）
     */
    private String resolution;

    /**
     * 照片大小（字节）
     */
    private Long fileSize;

    /**
     * 照片清晰度评分（0-100分）
     */
    private Integer clarityScore;

    /**
     * 亮度评分（0-100分）
     */
    private Integer brightnessScore;

    /**
     * 模糊度检测（normal-正常、blurry-模糊、very_blurry-严重模糊）
     */
    private String blurStatus;

    /**
     * OCR识别结果（JSON格式，识别设备标签、序列号等）
     */
    private String ocrResult;

    /**
     * OCR识别置信度（0-1）
     */
    private Double ocrConfidence;

    /**
     * 设备标签是否可识别（true-可识别、false-不可识别）
     */
    private Boolean labelRecognizable;

    /**
     * 时间一致性核验（true-一致、false-不一致）
     */
    private Boolean timeConsistent;

    /**
     * 时间偏差（秒，与门禁记录对比）
     */
    private Integer timeDeviation;

    /**
     * 位置一致性核验（true-一致、false-不一致）
     */
    private Boolean locationConsistent;

    /**
     * 照片水印内容（JSON格式，包含时间、地点、人员等信息）
     */
    private String watermark;

    /**
     * 核验状态（pending-待核验、passed-通过、failed-未通过、manual-人工复核）
     */
    private String verificationStatus;

    /**
     * 核验结果摘要
     */
    private String verificationSummary;

    /**
     * 核验人员ID（人工复核）
     */
    private Long verifierId;

    /**
     * 核验时间
     */
    private LocalDateTime verificationTime;

    /**
     * 异常标记（multiple-重复照片、dark-光线过暗、blur-模糊、no_label-无标签）
     */
    private String abnormalType;

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
