package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.AlarmRecord;
import com.roominspection.backend.mapper.AlarmRecordMapper;
import com.roominspection.backend.service.AlarmRecordService;
import com.roominspection.backend.service.ApiConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 告警记录Service实现
 */
@Slf4j
@Service
public class AlarmRecordServiceImpl extends ServiceImpl<AlarmRecordMapper, AlarmRecord>
        implements AlarmRecordService {

    @Autowired
    private ApiConfigService apiConfigService;

    @Override
    public List<AlarmRecord> listByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.findByTimeRange(startTime, endTime);
    }

    @Override
    public List<AlarmRecord> listByRoomAndTimeRange(String roomId, LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.findByRoomAndTimeRange(roomId, startTime, endTime);
    }

    @Override
    public List<AlarmRecord> listByLevel(String level, LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.findByLevelAndTimeRange(level, startTime, endTime);
    }

    @Override
    @Transactional
    public boolean createAlarm(AlarmRecord alarmRecord) {
        alarmRecord.setId(UUID.randomUUID().toString().replace("-", ""));
        alarmRecord.setAlarmCode("ALM-" + System.currentTimeMillis());
        alarmRecord.setStatus("ACTIVE");
        alarmRecord.setAlarmTime(LocalDateTime.now());
        alarmRecord.setCreatedAt(LocalDateTime.now());
        alarmRecord.setUpdatedAt(LocalDateTime.now());

        boolean result = save(alarmRecord);

        // 发送告警通知
        if (result) {
            sendAlarmNotification(alarmRecord);
        }

        return result;
    }

    @Override
    @Transactional
    public boolean acknowledgeAlarm(String id, String acknowledgedBy, String acknowledgedByName) {
        AlarmRecord alarm = getById(id);
        if (alarm == null) {
            throw new RuntimeException("告警记录不存在");
        }

        alarm.setStatus("ACKNOWLEDGED");
        alarm.setAcknowledgedBy(acknowledgedBy);
        alarm.setAcknowledgedAt(LocalDateTime.now());
        alarm.setUpdatedAt(LocalDateTime.now());

        return updateById(alarm);
    }

    @Override
    @Transactional
    public boolean resolveAlarm(String id, String handledBy, String handledByName, String handleResult) {
        AlarmRecord alarm = getById(id);
        if (alarm == null) {
            throw new RuntimeException("告警记录不存在");
        }

        alarm.setStatus("RESOLVED");
        alarm.setHandledBy(handledBy);
        alarm.setHandledByName(handledByName);
        alarm.setHandleResult(handleResult);
        alarm.setHandledAt(LocalDateTime.now());
        alarm.setUpdatedAt(LocalDateTime.now());

        return updateById(alarm);
    }

    @Override
    public List<AlarmRecord> getUnresolvedAlarms() {
        return baseMapper.findUnresolved();
    }

    @Override
    public List<AlarmRecord> getCriticalAlarms() {
        return baseMapper.findCriticalUnresolved();
    }

    @Override
    public Map<String, Object> getStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();

        List<Map<String, Object>> levelStats = countByLevel(startTime, endTime);
        Map<String, Long> levelMap = new HashMap<>();
        for (Map<String, Object> item : levelStats) {
            String level = (String) item.get("level");
            Long count = ((Number) item.get("count")).longValue();
            levelMap.put(level, count);
        }

        stats.put("critical", levelMap.getOrDefault("CRITICAL", 0L));
        stats.put("major", levelMap.getOrDefault("MAJOR", 0L));
        stats.put("minor", levelMap.getOrDefault("MINOR", 0L));

        List<Map<String, Object>> statusStats = countByStatus(startTime, endTime);
        Map<String, Long> statusMap = new HashMap<>();
        for (Map<String, Object> item : statusStats) {
            String status = (String) item.get("status");
            Long count = ((Number) item.get("count")).longValue();
            statusMap.put(status, count);
        }

        stats.put("active", statusMap.getOrDefault("ACTIVE", 0L));
        stats.put("acknowledged", statusMap.getOrDefault("ACKNOWLEDGED", 0L));
        stats.put("resolved", statusMap.getOrDefault("RESOLVED", 0L));

        return stats;
    }

    @Override
    public List<Map<String, Object>> countByLevel(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.countByLevel(startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> countByType(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.countByType(startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> countByStatus(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.countByStatus(startTime, endTime);
    }

    @Override
    public boolean sendAlarmNotification(AlarmRecord alarmRecord) {
        try {
            // 发送钉钉通知
            if (!Boolean.TRUE.equals(alarmRecord.getDingTalkSent())) {
                sendDingTalkNotification(alarmRecord);
                alarmRecord.setDingTalkSent(true);
            }

            // 发送短信通知
            if (!Boolean.TRUE.equals(alarmRecord.getSmsSent()) && "CRITICAL".equals(alarmRecord.getLevel())) {
                sendSMSNotification(alarmRecord);
                alarmRecord.setSmsSent(true);
            }

            // 发送邮件通知
            if (!Boolean.TRUE.equals(alarmRecord.getEmailSent())) {
                sendEmailNotification(alarmRecord);
                alarmRecord.setEmailSent(true);
            }

            updateById(alarmRecord);
            return true;
        } catch (Exception e) {
            log.error("发送告警通知失败: alarmId={}, error={}", alarmRecord.getId(), e.getMessage());
            return false;
        }
    }

    private void sendDingTalkNotification(AlarmRecord alarm) {
        // 调用钉钉API发送通知
        // 实际实现需要集成钉钉SDK
        log.info("发送钉钉告警通知: alarmCode={}, level={}", alarm.getAlarmCode(), alarm.getLevel());
    }

    private void sendSMSNotification(AlarmRecord alarm) {
        // 调用短信API发送通知
        // 实际实现需要集成短信SDK（如阿里云短信）
        log.info("发送短信告警通知: alarmCode={}, level={}", alarm.getAlarmCode(), alarm.getLevel());
    }

    private void sendEmailNotification(AlarmRecord alarm) {
        // 调用邮件API发送通知
        log.info("发送邮件告警通知: alarmCode={}, level={}", alarm.getAlarmCode(), alarm.getLevel());
    }

    @Override
    @Transactional
    public boolean linkWorkOrder(String alarmId, String workOrderId) {
        AlarmRecord alarm = getById(alarmId);
        if (alarm == null) {
            throw new RuntimeException("告警记录不存在");
        }

        alarm.setWorkOrderId(workOrderId);
        alarm.setUpdatedAt(LocalDateTime.now());

        return updateById(alarm);
    }
}
