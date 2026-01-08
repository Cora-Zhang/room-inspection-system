package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.FireHostLog;
import com.roominspection.backend.mapper.FireHostLogMapper;
import com.roominspection.backend.service.FireHostLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 消防主机日志Service实现
 */
@Service
public class FireHostLogServiceImpl extends ServiceImpl<FireHostLogMapper, FireHostLog>
        implements FireHostLogService {

    @Override
    @Transactional
    public void receiveSignal(Long hostId, Integer signalType, String loopNo, String detectorAddress,
                              Integer detectorType, String detectorLocation, String signalDescription) {
        FireHostLog log = new FireHostLog();
        log.setHostId(hostId);
        log.setSignalType(signalType);
        log.setLoopNo(loopNo);
        log.setDetectorAddress(detectorAddress);
        log.setDetectorType(detectorType);
        log.setDetectorLocation(detectorLocation);
        log.setSignalDescription(signalDescription);
        log.setSignalTime(LocalDateTime.now());
        log.setConfirmed(0); // 0-未确认
        log.setHandled(0); // 0-未处理
        log.setAlertStatus(0); // 0-未发送告警
        log.setCreateTime(LocalDateTime.now());

        save(log);

        // 如果是火警信号（2）或故障信号（3），立即告警
        if (signalType == 2 || signalType == 3) {
            sendAlert(log);
        }
    }

    /**
     * 发送告警通知
     */
    private void sendAlert(FireHostLog log) {
        // TODO: 通过钉钉、短信、邮件等渠道发送告警通知
        log.setAlertStatus(1); // 1-已发送
        updateById(log);

        String signalTypeName = getSignalTypeName(log.getSignalType());
        log.info("消防告警: signalType={}, location={}, description={}",
                signalTypeName, log.getDetectorLocation(), log.getSignalDescription());
    }

    private String getSignalTypeName(Integer signalType) {
        switch (signalType) {
            case 1: return "一般信号";
            case 2: return "火警信号";
            case 3: return "故障信号";
            case 4: return "故障恢复";
            default: return "未知信号";
        }
    }

    @Override
    public List<FireHostLog> listByRoomId(Long roomId) {
        LambdaQueryWrapper<FireHostLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(roomId != null, FireHostLog::getRoomId, roomId);
        wrapper.orderByDesc(FireHostLog::getSignalTime);
        return list(wrapper);
    }

    @Override
    public List<FireHostLog> listBySignalType(Integer signalType) {
        LambdaQueryWrapper<FireHostLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(signalType != null, FireHostLog::getSignalType, signalType);
        wrapper.orderByDesc(FireHostLog::getSignalTime);
        return list(wrapper);
    }

    @Override
    public List<FireHostLog> listByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<FireHostLog> wrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            wrapper.ge(FireHostLog::getSignalTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(FireHostLog::getSignalTime, endTime);
        }
        wrapper.orderByDesc(FireHostLog::getSignalTime);
        return list(wrapper);
    }

    @Override
    @Transactional
    public void confirmSignal(Long logId, String confirmer, String confirmRemark) {
        FireHostLog log = getById(logId);
        if (log == null) {
            throw new RuntimeException("日志记录不存在");
        }

        if (log.getConfirmed() == 1) {
            throw new RuntimeException("信号已确认");
        }

        log.setConfirmed(1); // 1-已确认
        log.setConfirmer(confirmer);
        log.setConfirmTime(LocalDateTime.now());
        log.setConfirmRemark(confirmRemark);
        log.setUpdateTime(LocalDateTime.now());

        updateById(log);
    }

    @Override
    @Transactional
    public void handleSignal(Long logId, String handler, String handleResult) {
        FireHostLog log = getById(logId);
        if (log == null) {
            throw new RuntimeException("日志记录不存在");
        }

        log.setHandled(1); // 1-已处理
        log.setHandler(handler);
        log.setHandleTime(LocalDateTime.now());
        log.setHandleResult(handleResult);
        log.setUpdateTime(LocalDateTime.now());

        updateById(log);
    }

    @Override
    public List<FireHostLog> listUnconfirmedSignals() {
        LambdaQueryWrapper<FireHostLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(FireHostLog::getSignalType, Arrays.asList(2, 3)); // 火警信号、故障信号
        wrapper.eq(FireHostLog::getConfirmed, 0); // 0-未确认
        wrapper.orderByDesc(FireHostLog::getSignalTime);
        return list(wrapper);
    }

    @Override
    public List<FireHostLog> listUnhandledSignals() {
        LambdaQueryWrapper<FireHostLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(FireHostLog::getSignalType, Arrays.asList(2, 3)); // 火警信号、故障信号
        wrapper.eq(FireHostLog::getHandled, 0); // 0-未处理
        wrapper.orderByDesc(FireHostLog::getSignalTime);
        return list(wrapper);
    }
}
