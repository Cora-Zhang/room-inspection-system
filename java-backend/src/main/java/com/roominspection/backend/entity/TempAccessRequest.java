package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 临时门禁权限申请实体
 * 用于管理临时权限申请与审批流程
 */
@Data
@TableName("temp_access_request")
public class TempAccessRequest {

    /**
     * 申请ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请人工号
     */
    private String applicantNo;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 申请人部门
     */
    private String department;

    /**
     * 申请人联系电话
     */
    private String phone;

    /**
     * 访问机房ID
     */
    private Long roomId;

    /**
     * 访问机房名称
     */
    private String roomName;

    /**
     * 访问事由
     */
    private String reason;

    /**
     * 访问开始时间
     */
    private LocalDateTime accessStartTime;

    /**
     * 访问结束时间
     */
    private LocalDateTime accessEndTime;

    /**
     * 审批状态（0-待审批 1-审批通过 2-审批拒绝 3-已撤销）
     */
    private Integer approvalStatus;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 审批意见
     */
    private String approvalComment;

    /**
     * 权限下发状态（0-未下发 1-已下发 2-下发失败）
     */
    private Integer syncStatus;

    /**
     * 权限生效状态（0-未生效 1-生效中 2-已过期 3-已回收）
     */
    private Integer effectiveStatus;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    private Integer deleted;
}
