package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.DeviceMetric;
import com.roominspection.backend.mapper.DeviceMetricMapper;
import com.roominspection.backend.service.DeviceMetricService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备监控指标服务实现
 */
@Slf4j
@Service
public class DeviceMetricServiceImpl extends ServiceImpl<DeviceMetricMapper, DeviceMetric>
        implements DeviceMetricService {

    @Autowired
    private DeviceMetricMapper deviceMetricMapper;

    /**
     * 批量保存指标
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchMetrics(List<DeviceMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return false;
        }

        int count = deviceMetricMapper.insertBatch(metrics);
        log.info("批量保存设备指标成功: count={}", count);
        return count > 0;
    }

    /**
     * 获取设备最新指标
     */
    @Override
    public List<DeviceMetric> getLatestMetricsByDeviceId(String deviceId) {
        return deviceMetricMapper.selectLatestByDeviceId(deviceId);
    }

    /**
     * 获取机房所有最新指标
     */
    @Override
    public List<DeviceMetric> getLatestMetricsByRoomId(String roomId) {
        return deviceMetricMapper.selectLatestByRoomId(roomId);
    }

    /**
     * 获取告警指标
     */
    @Override
    public List<DeviceMetric> getAlarmMetrics(LocalDateTime startTime, LocalDateTime endTime) {
        return deviceMetricMapper.selectAlarmMetrics(startTime, endTime);
    }

    /**
     * 清理历史指标数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanHistoryMetrics(int days) {
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(days);
        int count = deviceMetricMapper.deleteBeforeTime(beforeTime);
        log.info("清理历史指标数据: days={}, count={}", days, count);
        return count;
    }

    /**
     * 根据阈值判断指标状态
     */
    @Override
    public String evaluateMetricStatus(DeviceMetric metric, Double thresholdUpper, Double thresholdLower) {
        Double value = metric.getMetricValue();
        if (value == null) {
            return "UNKNOWN";
        }

        boolean exceeded = false;

        if (thresholdUpper != null && value > thresholdUpper) {
            exceeded = true;
        }

        if (thresholdLower != null && value < thresholdLower) {
            exceeded = true;
        }

        metric.setExceededThreshold(exceeded ? 1 : 0);

        if (exceeded) {
            // 判断严重程度
            if (thresholdUpper != null && value > thresholdUpper * 1.2) {
                metric.setStatus("CRITICAL");
            } else if (thresholdLower != null && value < thresholdLower * 0.8) {
                metric.setStatus("CRITICAL");
            } else {
                metric.setStatus("WARNING");
            }
        } else {
            metric.setStatus("NORMAL");
        }

        return metric.getStatus();
    }
}
