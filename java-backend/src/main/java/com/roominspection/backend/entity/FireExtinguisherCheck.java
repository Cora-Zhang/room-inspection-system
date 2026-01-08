package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 灭火器检查记录实体
 */
@Data
@TableName("fire_extinguisher_check")
public class FireExtinguisherCheck {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 检查记录编号
     */
    private String checkNo;

    /**
     * 灭火器ID
     */
    private Long extinguisherId;

    /**
     * 灭火器编号
     */
    private String extinguisherCode;

    /**
     * 机房ID
     */
    private Long roomId;

    /**
     * 检查类型：1-月度称重与外观检查，2-紧急确认，3-年度检查
     */
    private Integer checkType;

    /**
     * 检查人
     */
    private String checker;

    /**
     * 检查时间
     */
    private LocalDateTime checkTime;

    /**
     * 压力值(MPa)
     */
    private Double pressureValue;

    /**
     * 压力状态：0-正常，1-偏低，2-异常
     */
    private Integer pressureStatus;

    /**
     * 重量值(kg)
     */
    private Double weightValue;

    /**
     * 重量状态：0-正常，1-偏低，2-异常
     */
    private Integer weightStatus;

    /**
     * 外观检查结果：0-完好，1-轻微损坏，2-严重损坏
     */
    private Integer appearanceStatus;

    /**
     * 外观检查描述
     */
    private String appearanceDescription;

    /**
     * 检查照片
     */
    private String photos;

    /**
     * 是否需要充装：0-否，1-是
     */
    private Integer needRefill;

    /**
     * 充装备注
     */
    private String refillRemark;

    /**
     * 检查结果：0-合格，1-不合格
     */
    private Integer checkResult;

    /**
     * 处理措施
     */
    private String handlingMeasures;

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
