package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.AlertRule;
import com.roominspection.backend.entity.Device;
import com.roominspection.backend.entity.DeviceMetric;
import com.roominspection.backend.mapper.AlertRuleMapper;
import com.roominspection.backend.service.AlertRuleService;
import com.roominspection.backend.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 告警规则服务实现类
 */
@Slf4j
@Service
public class AlertRuleServiceImpl extends ServiceImpl<AlertRuleMapper, AlertRule> implements AlertRuleService {

    @Autowired(required = false)
    private DeviceService deviceService;

    @Override
    public List<AlertRule> checkAlert(DeviceMetric metric) {
        List<AlertRule> triggeredRules = new ArrayList<>();

        try {
            // 获取启用的告警规则
            LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AlertRule::getStatus, 1);
            wrapper.eq(AlertRule::getAlertType, metric.getMetricType());

            // 按设备ID、设备类型、机房ID筛选
            wrapper.and(w -> w.isNull(AlertRule::getDeviceId)
                    .or().eq(AlertRule::getDeviceId, metric.getDeviceId()));

            wrapper.and(w -> w.isNull(AlertRule::getDeviceType)
                    .or().eq(AlertRule::getDeviceType, metric.getDeviceType()));

            wrapper.orderByDesc(AlertRule::getPriority);

            List<AlertRule> rules = list(wrapper);

            // 检查每个规则是否触发
            for (AlertRule rule : rules) {
                if (isTriggered(rule, metric)) {
                    triggeredRules.add(rule);
                    // 发送告警通知
                    sendAlert(rule, metric, null);
                }
            }

        } catch (Exception e) {
            log.error("检查告警失败", e);
        }

        return triggeredRules;
    }

    @Override
    public List<Map<String, Object>> checkDeviceAlerts(String deviceId) {
        List<Map<String, Object>> alerts = new ArrayList<>();

        try {
            Device device = deviceService.getById(deviceId);
            if (device == null) {
                return alerts;
            }

            // TODO: 获取设备最新的指标数据并检查告警
            // 临时返回空列表

        } catch (Exception e) {
            log.error("检查设备告警失败: deviceId={}", deviceId, e);
        }

        return alerts;
    }

    @Override
    public boolean sendAlert(AlertRule alertRule, DeviceMetric metric, Object device) {
        try {
            // TODO: 根据告警规则的通知方式发送告警
            // 邮件、短信、钉钉等

            log.info("发送告警: rule={}, device={}, metric={}",
                    alertRule.getRuleName(),
                    metric.getDeviceName(),
                    metric.getMetricValue());

            return true;
        } catch (Exception e) {
            log.error("发送告警失败", e);
            return false;
        }
    }

    @Override
    public List<AlertRule> getActiveAlertRules(String deviceId) {
        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRule::getStatus, 1);

        if (deviceId != null && !deviceId.isEmpty()) {
            wrapper.and(w -> w.isNull(AlertRule::getDeviceId)
                    .or().eq(AlertRule::getDeviceId, deviceId));
        }

        wrapper.orderByDesc(AlertRule::getPriority);
        return list(wrapper);
    }

    @Override
    public boolean testAlertRule(Long ruleId) {
        AlertRule rule = getById(ruleId);
        if (rule == null) {
            return false;
        }

        try {
            // TODO: 模拟触发告警规则进行测试
            log.info("测试告警规则: ruleId={}", ruleId);
            return true;
        } catch (Exception e) {
            log.error("测试告警规则失败", e);
            return false;
        }
    }

    /**
     * 判断指标是否触发告警规则
     */
    private boolean isTriggered(AlertRule rule, DeviceMetric metric) {
        if (metric.getMetricValue() == null) {
            return false;
        }

        double value = metric.getMetricValue();
        String condition = rule.getCondition();
        double thresholdUpper = rule.getThresholdUpper() != null ? rule.getThresholdUpper() : 0;
        double thresholdLower = rule.getThresholdLower() != null ? rule.getThresholdLower() : 0;

        switch (condition) {
            case "GT": // 大于
                return value > thresholdUpper;
            case "LT": // 小于
                return value < thresholdUpper;
            case "EQ": // 等于
                return value == thresholdUpper;
            case "GE": // 大于等于
                return value >= thresholdUpper;
            case "LE": // 小于等于
                return value <= thresholdUpper;
            case "NE": // 不等于
                return value != thresholdUpper;
            case "BETWEEN": // 在区间内
                return value >= thresholdLower && value <= thresholdUpper;
            case "NOT_BETWEEN": // 不在区间内
                return value < thresholdLower || value > thresholdUpper;
            default:
                return false;
        }
    }
}
