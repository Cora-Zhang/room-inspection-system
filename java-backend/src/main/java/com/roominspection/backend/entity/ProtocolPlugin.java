package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 协议插件实体类
 */
@Data
@TableName("protocol_plugin")
public class ProtocolPlugin {

    /**
     * 插件ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 协议名称
     */
    private String protocolName;

    /**
     * 协议类型（tcp/udp/http/mqtt等）
     */
    private String protocolType;

    /**
     * 协议版本
     */
    private String version;

    /**
     * 协议描述
     */
    private String description;

    /**
     * 配置参数JSON（Schema定义）
     */
    private String configSchema;

    /**
     * 插件JAR文件路径
     */
    private String jarFilePath;

    /**
     * 实现类全限定名
     */
    private String implementationClass;

    /**
     * 状态（enabled/disabled）
     */
    private String status;

    /**
     * 作者
     */
    private String author;

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
