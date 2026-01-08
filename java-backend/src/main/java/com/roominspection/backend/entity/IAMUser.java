package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * IAM用户实体（IAM数据同步）
 */
@Data
@TableName("iam_user")
public class IAMUser {

    /**
     * 用户ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * IAM登录名（uid）
     */
    private String uid;

    /**
     * 应用系统登录账号（accountNo，默认与uid一致）
     */
    private String accountNo;

    /**
     * 姓名（userName）
     */
    private String userName;

    /**
     * 英文名（englishName）
     */
    private String englishName;

    /**
     * 性别（gender）：0-男，1-女
     */
    private Integer gender;

    /**
     * 手机号（mobile）
     */
    private String mobile;

    /**
     * 办公电话（officePhone）
     */
    private String officePhone;

    /**
     * 电子邮箱（email）
     */
    private String email;

    /**
     * 身份证号码（identityCode）
     */
    private String identityCode;

    /**
     * 籍贯（censusRegister）
     */
    private String censusRegister;

    /**
     * 国籍（nationality）
     */
    private String nationality;

    /**
     * 部门编码（orgCode）
     */
    private String orgCode;

    /**
     * 部门名称（orgName）
     */
    private String orgName;

    /**
     * 部门名称1（orgName1）
     */
    private String orgName1;

    /**
     * 公司编码（companyCode）
     */
    private String companyCode;

    /**
     * 公司名称（companyName）
     */
    private String companyName;

    /**
     * 入司时间（hireDate）
     */
    private LocalDate hireDate;

    /**
     * 用工性质（employmentNature）
     */
    private String employmentNature;

    /**
     * 是否二次入司（back）
     */
    private String back;

    /**
     * 政治面貌（politicalStatus）
     */
    private String politicalStatus;

    /**
     * 职位编码（jobCode）
     */
    private String jobCode;

    /**
     * 职位名称（jobName）
     */
    private String jobName;

    /**
     * 职级（jobLevelName）
     */
    private String jobLevelName;

    /**
     * 职级分类（jobLevelType）
     */
    private String jobLevelType;

    /**
     * 层级（levelName）
     */
    private String levelName;

    /**
     * 管理职务（managementDutyName）
     */
    private String managementDutyName;

    /**
     * 最高职务说明（topDutyDesc）
     */
    private String topDutyDesc;

    /**
     * 挂职岗位（temporaryPost）
     */
    private String temporaryPost;

    /**
     * 技能职务（skillDutyName）
     */
    private String skillDutyName;

    /**
     * 用户类型列表（userTypes）
     */
    private String userTypes;

    /**
     * 职位列表（jobs，JSON格式）
     */
    private String jobs;

    /**
     * 组织列表（orgs，JSON格式）
     */
    private String orgs;

    /**
     * 账号状态（status）：1-正常，0-停用
     */
    private Integer status;

    /**
     * 备注（remark）
     */
    private String remark;

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
