package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 工单流转记录
 */
@Data
@TableName("work_order_flows")
public class WorkOrderFlow {

    /**
     * 流转ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 工单ID
     */
    private String workOrderId;

    /**
     * 工单编号
     */
    private String orderCode;

    /**
     * 操作类型：CREATE-创建，ASSIGN-指派，START-开始，PAUSE-暂停，RESUME-恢复，COMPLETE-完成，CANCEL-取消，CLOSE-关闭，REASSIGN-重新指派
     */
    private String actionType;

    /**
     * 操作前状态
     */
    private String fromStatus;

    /**
     * 操作后状态
     */
    private String toStatus;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作内容
     */
    private String content;

    /**
     * 操作意见
     */
    private String comment;

    /**
     * 操作时间
     */
    private LocalDateTime operatedAt;

    /**
     * 关联附件（JSON格式）
     */
    private String attachments;

    /**
     * 备注
     */
    private String remark;
}
