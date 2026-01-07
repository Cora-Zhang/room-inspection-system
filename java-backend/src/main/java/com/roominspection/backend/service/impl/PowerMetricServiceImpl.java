package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.PowerMetric;
import com.roominspection.backend.mapper.PowerMetricMapper;
import com.roominspection.backend.service.PowerMetricService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 电力指标服务实现类
 */
@Slf4j
@Service
public class PowerMetricServiceImpl extends ServiceImpl<PowerMetricMapper, PowerMetric>
        implements PowerMetricService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMetric(PowerMetric metric) {
        return this.save(metric);
    }

    @Override
    public PowerMetric getLatestByDeviceId(Long powerSystemId) {
        return this.getOne(new LambdaQueryWrapper<PowerMetric>()
                .eq(PowerMetric::getPowerSystemId, powerSystemId)
                .orderByDesc(PowerMetric::getCollectTime)
                .last("LIMIT 1"));
    }

    @Override
    public boolean checkThreshold(PowerMetric metric) {
        // 检查UPS负载阈值（默认80%）
        if (metric.getUpsLoadPercent() != null) {
            if (metric.getUpsLoadPercent().compareTo(new BigDecimal("80")) > 0) {
                metric.setAlertInfo("{\"type\":\"load_high\",\"message\":\"UPS负载超过80%\"}");
                return true;
            }
        }
        
        // 检查PDU负载阈值
        if (metric.getPduLoadStatus() != null) {
            if ("overload".equals(metric.getPduLoadStatus())) {
                metric.setAlertInfo("{\"type\":\"pdu_overload\",\"message\":\"PDU过载\"}");
                return true;
            }
        }
        
        // 检查总负载阈值
        if (metric.getTotalLoadPercent() != null) {
            if (metric.getTotalLoadPercent().compareTo(new BigDecimal("90")) > 0) {
                metric.setAlertInfo("{\"type\":\"total_load_high\",\"message\":\"总负载超过90%\"}");
                return true;
            }
        }
        
        // 检查电压异常
        if (metric.getInputVoltageA() != null) {
            if (metric.getInputVoltageA().compareTo(new BigDecimal("210")) < 0 ||
                metric.getInputVoltageA().compareTo(new BigDecimal("230")) > 0) {
                metric.setAlertInfo("{\"type\":\"voltage_abnormal\",\"message\":\"输入电压异常\"}");
                return true;
            }
        }
        
        return false;
    }
}
