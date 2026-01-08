package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 效率核查工单实体
 */
@Data
@TableName("efficiency_check_order")
public class EfficiencyCheckOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 工单编号
     */
    private String orderNo;

    /**
     * 空调ID
     */
    private Long acId;

    /**
     * 空调编号
     */
    private String acCode;

    /**
     * 机房ID
     */
    private Long roomId;

    /**
     * 工单类型：1-制冷效率检查，2-滤网清洁检查，3-预防性保养
     */
    private Integer orderType;

    /**
     * 触发原因
     */
    private String triggerReason;

    /**
     * 异常详情：温差异常描述、压缩机时长等
     */
    private String abnormalDetail;

    /**
     * 优先级：1-紧急，2-高，3-中，4-低
     */
    private Integer priority;

    /**
     * 工单状态：0-待指派，1-待处理，2-处理中，3-已完成，4-已取消
     */
    private Integer status;

    /**
     * 指派人
     */
    private String assignee;

    /**
     * 指派时间
     */
    private LocalDateTime assignTime;

    /**
     * 开始处理时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 处理描述
     */
    private String handleDescription;

    /**
     * 现场照片
     */
    private String photos;

    /**
     * 工单备注
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
