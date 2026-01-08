package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据中心实体类
 */
@Data
@TableName("data_center")
public class DataCenter {

    /**
     * 数据中心ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据中心编码
     */
    private String datacenterCode;

    /**
     * 数据中心名称
     */
    private String datacenterName;

    /**
     * 所属省份
     */
    private String province;

    /**
     * 所属城市
     */
    private String city;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 内网IP地址
     */
    private String internalIp;

    /**
     * 外网IP地址
     */
    private String externalIp;

    /**
     * VPN IP地址
     */
    private String vpnIp;

    /**
     * 总机柜数
     */
    private Integer totalRacks;

    /**
     * 可用机柜数
     */
    private Integer availableRacks;

    /**
     * 总电力（KW）
     */
    private Double totalPower;

    /**
     * 可用电力（KW）
     */
    private Double availablePower;

    /**
     * 总设备数
     */
    private Integer totalDevices;

    /**
     * 在线设备数
     */
    private Integer onlineDevices;

    /**
     * 离线设备数
     */
    private Integer offlineDevices;

    /**
     * 告警总数
     */
    private Integer totalAlarms;

    /**
     * 严重告警数
     */
    private Integer criticalAlarms;

    /**
     * 警告告警数
     */
    private Integer warningAlarms;

    /**
     * 数据中心状态（active/inactive/maintenance）
     */
    private String status;

    /**
     * 是否为主数据中心
     */
    private Boolean isPrimary;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

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
