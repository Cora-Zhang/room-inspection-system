package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.AlertRule;
import com.roominspection.backend.entity.DeviceMetric;

import java.util.List;

/**
 * 告警规则服务接口
 */
public interface AlertRuleService extends IService<AlertRule> {

    /**
     * 检查指标是否触发告警
     *
     * @param metric 指标数据
     * @return 触发的告警规则列表
     */
    List<AlertRule> checkAlert(DeviceMetric metric);

    /**
     * 检查设备所有指标是否触发告警
     *
     * @param deviceId 设备ID
     * @return 触发的告警列表
     */
    List<Map<String, Object>> checkDeviceAlerts(String deviceId);

    /**
     * 发送告警通知
     *
     * @param alertRule 告警规则
     * @param metric    指标数据
     * @param device    设备信息
     * @return 是否发送成功
     */
    boolean sendAlert(AlertRule alertRule, DeviceMetric metric, Object device);

    /**
     * 获取启用的告警规则
     *
     * @param deviceId 设备ID
     * @return 告警规则列表
     */
    List<AlertRule> getActiveAlertRules(String deviceId);

    /**
     * 测试告警规则
     *
     * @param ruleId 规则ID
     * @return 测试结果
     */
    boolean testAlertRule(Long ruleId);
}
