package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 巡检记录实体
 */
@Data
@TableName("inspections")
public class Inspection {

    /**
     * 巡检ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 巡检编号
     */
    private String code;

    /**
     * 机房ID
     */
    private String roomId;

    /**
     * 机房名称（冗余字段，方便查询）
     */
    private String roomName;

    /**
     * 巡检人ID
     */
    private String inspectorId;

    /**
     * 巡检人姓名（冗余字段，方便查询）
     */
    private String inspectorName;

    /**
     * 班次：DAY-白班，NIGHT-夜班
     */
    private String shift;

    /**
     * 巡检日期
     */
    private LocalDateTime inspectionDate;

    /**
     * 巡检开始时间
     */
    private LocalDateTime startTime;

    /**
     * 巡检结束时间
     */
    private LocalDateTime endTime;

    /**
     * 温度（℃）
     */
    private Double temperature;

    /**
     * 湿度（%）
     */
    private Double humidity;

    /**
     * 巡检结果：NORMAL-正常，WARNING-警告，ERROR-异常
     */
    private String result;

    /**
     * 巡检状态：PENDING-待处理，PROCESSING-进行中，COMPLETED-已完成
     */
    private String status;

    /**
     * 发现问题数量
     */
    private Integer issueCount;

    /**
     * 问题描述
     */
    private String issues;

    /**
     * 处理建议
     */
    private String suggestions;

    /**
     * 照片URL列表
     */
    private String photos;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
