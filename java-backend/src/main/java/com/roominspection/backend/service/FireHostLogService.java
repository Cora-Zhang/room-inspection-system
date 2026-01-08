package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.FireHostLog;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 消防主机日志Service
 */
public interface FireHostLogService extends IService<FireHostLog> {

    /**
     * 解析消防主机通信协议，接收信号
     */
    void receiveSignal(Long hostId, Integer signalType, String loopNo, String detectorAddress,
                       Integer detectorType, String detectorLocation, String signalDescription);

    /**
     * 根据机房ID查询日志列表
     */
    List<FireHostLog> listByRoomId(Long roomId);

    /**
     * 根据信号类型查询日志列表
     */
    List<FireHostLog> listBySignalType(Integer signalType);

    /**
     * 根据时间范围查询日志列表
     */
    List<FireHostLog> listByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 确认信号
     */
    void confirmSignal(Long logId, String confirmer, String confirmRemark);

    /**
     * 处理信号
     */
    void handleSignal(Long logId, String handler, String handleResult);

    /**
     * 获取未确认的火警/故障信号
     */
    List<FireHostLog> listUnconfirmedSignals();

    /**
     * 获取未处理的火警/故障信号
     */
    List<FireHostLog> listUnhandledSignals();
}
