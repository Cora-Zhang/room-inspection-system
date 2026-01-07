package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 门禁日志实体
 * 用于记录人员进出机房记录，支持海康、大华等门禁系统对接
 */
@Data
@TableName("door_access_log")
public class DoorAccessLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 门禁系统类型（hikvision-海康、dahua-大华、custom-自定义）
     */
    private String systemType;

    /**
     * 门禁设备ID
     */
    private String deviceId;

    /**
     * 门禁设备名称（如：机房A入口、机房B出口）
     */
    private String deviceName;

    /**
     * 机房ID
     */
    private Long roomId;

    /**
     * 人员ID（关联user或duty_staff表）
     */
    private Long staffId;

    /**
     * 人员姓名
     */
    private String staffName;

    /**
     * 人员工号/卡号
     */
    private String staffCode;

    /**
     * 进出方向（in-进入、out-离开）
     */
    private String direction;

    /**
     * 进出时间
     */
    private LocalDateTime accessTime;

    /**
     * 访问方式（card-刷卡、face-人脸、fingerprint-指纹、password-密码）
     */
    private String accessMethod;

    /**
     * 访问状态（success-成功、failed-失败、denied-拒绝）
     */
    private String status;

    /**
     * 拒绝原因（如：权限不足、卡片过期）
     */
    private String rejectReason;

    /**
     * 关联巡检任务ID（如果有）
     */
    private Long inspectionTaskId;

    /**
     * 是否已核验
     */
    private Boolean verified;

    /**
     * 核验结果（match-匹配、mismatch-不匹配、pending-待核验）
     */
    private String verificationResult;

    /**
     * 门禁照片URL（人脸识别或抓拍照片）
     */
    private String photoUrl;

    /**
     * 扩展字段（JSON格式，存储门禁系统返回的原始数据）
     */
    private String extraData;

    /**
     * 数据来源（sync-同步对接、manual-手动录入）
     */
    private String dataSource;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;
}
