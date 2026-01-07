package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.PowerMetric;
import com.roominspection.backend.entity.PowerSystem;
import com.roominspection.backend.mapper.PowerSystemMapper;
import com.roominspection.backend.service.PowerMetricService;
import com.roominspection.backend.service.PowerSystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 电力系统监控服务实现类
 */
@Slf4j
@Service
public class PowerSystemServiceImpl extends ServiceImpl<PowerSystemMapper, PowerSystem>
        implements PowerSystemService {

    @Autowired
    private PowerMetricService powerMetricService;

    @Override
    public Page<PowerSystem> queryPage(String deviceType, Long roomId, String status,
                                         int pageNum, int pageSize) {
        LambdaQueryWrapper<PowerSystem> wrapper = new LambdaQueryWrapper<>();
        
        if (deviceType != null) {
            wrapper.eq(PowerSystem::getDeviceType, deviceType);
        }
        if (roomId != null) {
            wrapper.eq(PowerSystem::getRoomId, roomId);
        }
        if (status != null) {
            wrapper.eq(PowerSystem::getStatus, status);
        }
        
        wrapper.orderByDesc(PowerSystem::getCreateTime);
        
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PowerMetric collectBySnmp(Long powerSystemId) {
        log.info("通过SNMP采集电力设备{}的数据", powerSystemId);
        
        PowerSystem device = this.getById(powerSystemId);
        if (device == null) {
            log.error("电力设备不存在：{}", powerSystemId);
            return null;
        }
        
        PowerMetric metric = new PowerMetric();
        metric.setPowerSystemId(powerSystemId);
        metric.setDeviceCode(device.getDeviceCode());
        metric.setDeviceType(device.getDeviceType());
        metric.setCollectTime(LocalDateTime.now());
        metric.setDataSource("snmp");
        
        try {
            // TODO: 集成SNMP4J库进行实际的数据采集
            // 这里提供模拟数据
            
            // 模拟市电输入参数
            metric.setInputVoltageA(java.math.BigDecimal.valueOf(220.5));
            metric.setInputVoltageB(java.math.BigDecimal.valueOf(219.8));
            metric.setInputVoltageC(java.math.BigDecimal.valueOf(220.2));
            metric.setInputFrequency(java.math.BigDecimal.valueOf(50.0));
            
            // 根据设备类型设置不同参数
            if ("ups".equals(device.getDeviceType())) {
                // UPS参数
                metric.setUpsLoadPercent(java.math.BigDecimal.valueOf(65.5));
                metric.setUpsBackupTime(45);
                metric.setOutputVoltageA(java.math.BigDecimal.valueOf(220.0));
                metric.setBatteryCapacity(java.math.BigDecimal.valueOf(95.0));
                metric.setBatteryStatus("charging");
                metric.setTotalLoadPercent(java.math.BigDecimal.valueOf(65.5));
                
            } else if ("pdu".equals(device.getDeviceType())) {
                // PDU参数
                metric.setPduTotalCurrent(java.math.BigDecimal.valueOf(12.5));
                metric.setPduLoadStatus("normal");
                metric.setPduOutputVoltage(java.math.BigDecimal.valueOf(220.0));
                metric.setTotalLoadPercent(java.math.BigDecimal.valueOf(56.8));
                
            } else if ("main_power".equals(device.getDeviceType())) {
                // 市电参数
                metric.setTotalActivePower(java.math.BigDecimal.valueOf(450.5));
                metric.setTotalLoadPercent(java.math.BigDecimal.valueOf(71.5));
            }
            
            // 检查阈值
            boolean isAlert = powerMetricService.checkThreshold(metric);
            metric.setIsAlert(isAlert);
            
            // 保存指标
            powerMetricService.saveMetric(metric);
            
            // 更新设备状态
            device.setStatus("online");
            device.setLastCollectTime(LocalDateTime.now());
            this.updateById(device);
            
            log.info("SNMP数据采集完成，设备：{}", device.getDeviceCode());
            return metric;
            
        } catch (Exception e) {
            log.error("SNMP数据采集失败", e);
            
            // 更新设备状态为离线
            device.setStatus("offline");
            this.updateById(device);
            
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PowerMetric collectByModbus(Long powerSystemId) {
        log.info("通过Modbus采集电力设备{}的数据", powerSystemId);
        
        PowerSystem device = this.getById(powerSystemId);
        if (device == null) {
            log.error("电力设备不存在：{}", powerSystemId);
            return null;
        }
        
        PowerMetric metric = new PowerMetric();
        metric.setPowerSystemId(powerSystemId);
        metric.setDeviceCode(device.getDeviceCode());
        metric.setDeviceType(device.getDeviceType());
        metric.setCollectTime(LocalDateTime.now());
        metric.setDataSource("modbus");
        
        try {
            // TODO: 集成Modbus4J库进行实际的数据采集
            // 这里提供模拟数据
            
            // 模拟PDU参数
            metric.setPduTotalCurrent(java.math.BigDecimal.valueOf(15.3));
            metric.setPduLoadStatus("normal");
            metric.setPduOutputVoltage(java.math.BigDecimal.valueOf(220.0));
            metric.setTotalLoadPercent(java.math.BigDecimal.valueOf(69.6));
            
            // 检查阈值
            boolean isAlert = powerMetricService.checkThreshold(metric);
            metric.setIsAlert(isAlert);
            
            // 保存指标
            powerMetricService.saveMetric(metric);
            
            // 更新设备状态
            device.setStatus("online");
            device.setLastCollectTime(LocalDateTime.now());
            this.updateById(device);
            
            log.info("Modbus数据采集完成，设备：{}", device.getDeviceCode());
            return metric;
            
        } catch (Exception e) {
            log.error("Modbus数据采集失败", e);
            
            // 更新设备状态为离线
            device.setStatus("offline");
            this.updateById(device);
            
            return null;
        }
    }

    @Override
    public Map<String, Object> batchCollectAll() {
        log.info("批量采集所有电力设备数据");
        
        List<PowerSystem> devices = this.list(
                new LambdaQueryWrapper<PowerSystem>()
                        .eq(PowerSystem::getStatus, "online")
                        .or()
                        .eq(PowerSystem::getStatus, "unknown")
        );
        
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (PowerSystem device : devices) {
            try {
                if ("snmp".equals(device.getProtocol())) {
                    collectBySnmp(device.getId());
                } else if ("modbus".equals(device.getProtocol())) {
                    collectByModbus(device.getId());
                }
                successCount++;
            } catch (Exception e) {
                log.error("采集设备{}数据失败", device.getDeviceCode(), e);
                failCount++;
                errors.add(device.getDeviceCode() + ": " + e.getMessage());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalDevices", devices.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        
        log.info("批量采集完成，成功：{}，失败：{}", successCount, failCount);
        return result;
    }

    @Override
    public PowerMetric getLatestMetric(Long powerSystemId) {
        return powerMetricService.getLatestByDeviceId(powerSystemId);
    }

    @Override
    public Page<PowerMetric> queryMetrics(Long powerSystemId, LocalDateTime startTime,
                                           LocalDateTime endTime, int pageNum, int pageSize) {
        // TODO: 实现历史指标查询
        return new Page<>(pageNum, pageSize);
    }

    @Override
    public Map<String, Object> getDeviceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalCount = this.count();
        long onlineCount = this.count(new LambdaQueryWrapper<PowerSystem>()
                .eq(PowerSystem::getStatus, "online"));
        long offlineCount = this.count(new LambdaQueryWrapper<PowerSystem>()
                .eq(PowerSystem::getStatus, "offline"));
        
        stats.put("totalCount", totalCount);
        stats.put("onlineCount", onlineCount);
        stats.put("offlineCount", offlineCount);
        
        // 按类型统计
        Map<String, Long> typeStats = new HashMap<>();
        typeStats.put("main_power", this.count(new LambdaQueryWrapper<PowerSystem>()
                .eq(PowerSystem::getDeviceType, "main_power")));
        typeStats.put("ups", this.count(new LambdaQueryWrapper<PowerSystem>()
                .eq(PowerSystem::getDeviceType, "ups")));
        typeStats.put("pdu", this.count(new LambdaQueryWrapper<PowerSystem>()
                .eq(PowerSystem::getDeviceType, "pdu")));
        typeStats.put("generator", this.count(new LambdaQueryWrapper<PowerSystem>()
                .eq(PowerSystem::getDeviceType, "generator")));
        
        stats.put("typeStats", typeStats);
        
        return stats;
    }
}
