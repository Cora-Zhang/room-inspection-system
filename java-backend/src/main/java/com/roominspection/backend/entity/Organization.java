package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 组织实体（IAM数据同步）
 */
@Data
@TableName("organization")
public class Organization {

    /**
     * 组织ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 组织编码（orgCode）
     */
    private String orgCode;

    /**
     * 组织名称（orgName）
     */
    private String orgName;

    /**
     * 组织名称1（orgName1）
     */
    private String orgName1;

    /**
     * 组织简称（abbr）
     */
    private String abbr;

    /**
     * 组织全路径（orgPath）
     */
    private String orgPath;

    /**
     * 父节点编码（parentCode）
     */
    private String parentCode;

    /**
     * 父节点名称（parentName）
     */
    private String parentName;

    /**
     * 组织状态（status）：1-正常，0-停用
     */
    private Integer status;

    /**
     * 排序号（orderNum）
     */
    private Integer orderNum;

    /**
     * 公司编码（companyCode）
     */
    private String companyCode;

    /**
     * 公司名称（companyName）
     */
    private String companyName;

    /**
     * 组织性质（nature）
     */
    private String nature;

    /**
     * 内部/外部（internal）
     */
    private String internal;

    /**
     * 地点编码（addressCode）
     */
    private String addressCode;

    /**
     * 地点地址（addressName）
     */
    private String addressName;

    /**
     * 部门负责人编码（directorCode）
     */
    private String directorCode;

    /**
     * 部门负责人姓名（directorName）
     */
    private String directorName;

    /**
     * 部门分管领导编码（leaderCode）
     */
    private String leaderCode;

    /**
     * 部门分管领导姓名（leaderName）
     */
    private String leaderName;

    /**
     * 成立日期（incorporationTime）
     */
    private String incorporationTime;

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
