package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 职位实体（IAM数据同步）
 */
@Data
@TableName("job")
public class Job {

    /**
     * 职位ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 职位编码（code）
     */
    private String code;

    /**
     * 职位名称（name）
     */
    private String name;

    /**
     * 职位等级名称（jobLevel）
     */
    private String jobLevel;

    /**
     * 职位等级编码（jobCode）
     */
    private String jobCode;

    /**
     * 部门编码（orgCode）
     */
    private String orgCode;

    /**
     * 部门名称（orgName）
     */
    private String orgName;

    /**
     * 公司编码（companyCode）
     */
    private String companyCode;

    /**
     * 公司名称（companyName）
     */
    private String companyName;

    /**
     * 职务等级编码（dutyCode）
     */
    private String dutyCode;

    /**
     * 职务等级名称（duty）
     */
    private String duty;

    /**
     * 职级排序号（dutyLevel）
     */
    private String dutyLevel;

    /**
     * 状态（status）：1-正常，0-停用
     */
    private Integer status;

    /**
     * 序列编号（sequenceCode）
     */
    private String sequenceCode;

    /**
     * 序列名称（sequenceName）
     */
    private String sequenceName;

    /**
     * 通道编号（passagewayCode）
     */
    private String passagewayCode;

    /**
     * 通道名称（passagewayName）
     */
    private String passagewayName;

    /**
     * 岗位编码（postCode）
     */
    private String postCode;

    /**
     * 岗位名称（postName）
     */
    private String postName;

    /**
     * 组织列表（orgs，JSON格式）
     */
    private String orgs;

    /**
     * 操作标识（actionFlag）：0-新增，1-修改，2-删除
     */
    private Integer actionFlag;

    /**
     * 操作描述（actionDesc）
     */
    private String actionDesc;

    /**
     * 操作ID（actionId）
     */
    private Long actionId;

    /**
     * 同步时间
     */
    private LocalDateTime syncTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
