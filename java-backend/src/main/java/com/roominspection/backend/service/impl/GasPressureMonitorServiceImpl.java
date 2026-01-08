package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.GasPressureMonitor;
import com.roominspection.backend.mapper.GasPressureMonitorMapper;
import com.roominspection.backend.service.GasPressureMonitorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 气体压力监控Service实现
 */
@Service
public class GasPressureMonitorServiceImpl extends ServiceImpl<GasPressureMonitorMapper, GasPressureMonitor>
        implements GasPressureMonitorService {

    @Override
    public List<GasPressureMonitor> listByRoomId(Long roomId) {
        LambdaQueryWrapper<GasPressureMonitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(roomId != null, GasPressureMonitor::getRoomId, roomId);
        wrapper.orderByAsc(GasPressureMonitor::getSystemCode);
        return list(wrapper);
    }

    @Override
    @Transactional
    public void batchCollectData() {
        // 查询所有在线气体灭火系统
        LambdaQueryWrapper<GasPressureMonitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GasPressureMonitor::getStatus, 0); // 0-正常在线
        List<GasPressureMonitor> systemList = list(wrapper);

        for (GasPressureMonitor gpm : systemList) {
            try {
                collectData(gpm.getId());
                // 检查压力是否异常
                checkPressureAbnormal(gpm.getId());
            } catch (Exception e) {
                log.error("采集气体压力数据失败: systemCode={}, error={}", gpm.getSystemCode(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void collectData(Long systemId) {
        GasPressureMonitor gpm = getById(systemId);
        if (gpm == null) {
            throw new RuntimeException("气体灭火系统不存在");
        }

        // 模拟采集压力数据
        double pressure = gpm.getRatedPressure() * (0.96 + Math.random() * 0.07); // 额定压力的96%-103%
        updatePressure(systemId, pressure);

        gpm.setLastCollectTime(LocalDateTime.now());
        updateById(gpm);
    }

    @Override
    @Transactional
    public void updatePressure(Long systemId, Double pressure) {
        GasPressureMonitor gpm = getById(systemId);
        if (gpm == null) {
            throw new RuntimeException("气体灭火系统不存在");
        }

        gpm.setCurrentPressure(pressure);

        // 判断压力状态
        if (pressure < gpm.getAbnormalThreshold()) {
            gpm.setPressureStatus(3); // 3-异常
            gpm.setStatus(1); // 1-告警
        } else if (pressure < gpm.getAlertThreshold()) {
            gpm.setPressureStatus(2); // 2-预警
            gpm.setStatus(0); // 0-正常
        } else if (pressure < gpm.getRatedPressure() * 0.95) {
            gpm.setPressureStatus(1); // 1-偏低
            gpm.setStatus(0); // 0-正常
        } else {
            gpm.setPressureStatus(0); // 0-正常
            gpm.setStatus(0); // 0-正常
        }

        updateById(gpm);
    }

    @Override
    @Transactional
    public void checkPressureAbnormal(Long systemId) {
        GasPressureMonitor gpm = getById(systemId);
        if (gpm == null || gpm.getPressureStatus() < 2) { // 2-预警及以上才需要处理
            return;
        }

        // TODO: 如果压力异常（预警或异常），推送"确认气体灭火系统压力"工单
        // 这里可以创建一个工单或者发送告警通知
        String alertLevel = gpm.getPressureStatus() == 3 ? "异常" : "预警";
        log.warn("气体灭火系统压力告警: systemCode={}, currentPressure={}MPa, alertLevel={}",
                gpm.getSystemCode(), gpm.getCurrentPressure(), alertLevel);

        // 实际应用中，这里应该调用工单服务创建工单或调用告警服务发送通知
        if (gpm.getPressureStatus() == 2) { // 预警
            // 发送预警通知
            sendPressureAlert(gpm, "预警");
        } else if (gpm.getPressureStatus() == 3) { // 异常
            // 发送异常告警
            sendPressureAlert(gpm, "异常");
        }
    }

    /**
     * 发送压力告警通知
     */
    private void sendPressureAlert(GasPressureMonitor gpm, String alertLevel) {
        // TODO: 通过钉钉、短信、邮件等渠道发送告警通知
        log.info("气体灭火系统压力{}告警: systemCode={}, currentPressure={}MPa, ratedPressure={}MPa",
                alertLevel, gpm.getSystemCode(), gpm.getCurrentPressure(), gpm.getRatedPressure());
    }

    @Override
    public Map<String, Object> getStatistics(Long roomId) {
        LambdaQueryWrapper<GasPressureMonitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(roomId != null, GasPressureMonitor::getRoomId, roomId);
        List<GasPressureMonitor> gpmList = list(wrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", gpmList.size());

        long onlineCount = gpmList.stream().filter(gpm -> gpm.getStatus() == 0).count();
        long alarmCount = gpmList.stream().filter(gpm -> gpm.getStatus() == 1).count();
        long offlineCount = gpmList.stream().filter(gpm -> gpm.getStatus() == 2).count();

        long normalCount = gpmList.stream().filter(gpm -> gpm.getPressureStatus() == 0).count();
        long lowCount = gpmList.stream().filter(gpm -> gpm.getPressureStatus() == 1).count();
        long alertCount = gpmList.stream().filter(gpm -> gpm.getPressureStatus() == 2).count();
        long abnormalCount = gpmList.stream().filter(gpm -> gpm.getPressureStatus() == 3).count();

        stats.put("online", onlineCount);
        stats.put("alarm", alarmCount);
        stats.put("offline", offlineCount);
        stats.put("pressureNormal", normalCount);
        stats.put("pressureLow", lowCount);
        stats.put("pressureAlert", alertCount);
        stats.put("pressureAbnormal", abnormalCount);

        return stats;
    }
}
