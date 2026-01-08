package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消防主机日志实体
 */
@Data
@TableName("fire_host_log")
public class FireHostLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 消防主机ID
     */
    private Long hostId;

    /**
     * 消防主机编号
     */
    private String hostCode;

    /**
     * 机房ID
     */
    private Long roomId;

    /**
     * 信号类型：1-一般信号，2-火警信号，3-故障信号，4-故障恢复
     */
    private Integer signalType;

    /**
     * 回路号
     */
    private String loopNo;

    /**
     * 探测器地址
     */
    private String detectorAddress;

    /**
     * 探测器类型：1-感烟，2-感温，3-感光，4-手动报警按钮，5-输入模块，6-输出模块
     */
    private Integer detectorType;

    /**
     * 探测器位置
     */
    private String detectorLocation;

    /**
     * 信号描述
     */
    private String signalDescription;

    /**
     * 信号时间
     */
    private LocalDateTime signalTime;

    /**
     * 是否已确认：0-未确认，1-已确认
     */
    private Integer confirmed;

    /**
     * 确认人
     */
    private String confirmer;

    /**
     * 确认时间
     */
    private LocalDateTime confirmTime;

    /**
     * 确认备注
     */
    private String confirmRemark;

    /**
     * 是否已处理：0-未处理，1-已处理
     */
    private Integer handled;

    /**
     * 处理人
     */
    private String handler;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 告警状态：0-未发送，1-已发送
     */
    private Integer alertStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
