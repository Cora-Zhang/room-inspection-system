package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 值班人员实体
 */
@Data
@TableName("staffs")
public class Staff {

    /**
     * 值班人员ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 员工工号
     */
    private String employeeId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 部门
     */
    private String department;

    /**
     * 职位
     */
    private String position;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 人员状态：ACTIVE-在职，INACTIVE-离职，ON_LEAVE-请假
     */
    private String status;

    /**
     * 入职日期
     */
    private LocalDateTime joinedDate;

    /**
     * 技能
     */
    private String skills;

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
