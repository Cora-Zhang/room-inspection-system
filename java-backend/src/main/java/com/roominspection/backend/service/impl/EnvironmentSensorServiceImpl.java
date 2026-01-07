package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.EnvironmentData;
import com.roominspection.backend.entity.EnvironmentSensor;
import com.roominspection.backend.mapper.EnvironmentSensorMapper;
import com.roominspection.backend.service.EnvironmentDataService;
import com.roominspection.backend.service.EnvironmentSensorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 环境传感器服务实现类
 */
@Slf4j
@Service
public class EnvironmentSensorServiceImpl extends ServiceImpl<EnvironmentSensorMapper, EnvironmentSensor> implements EnvironmentSensorService {

    @Autowired
    private EnvironmentDataService environmentDataService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addSensor(EnvironmentSensor sensor) {
        sensor.setCreateTime(LocalDateTime.now());
        sensor.setUpdateTime(LocalDateTime.now());
        sensor.setStatus("NORMAL");
        save(sensor);
        log.info("添加环境传感器成功，传感器ID: {}", sensor.getId());
        return sensor.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSensor(EnvironmentSensor sensor) {
        sensor.setUpdateTime(LocalDateTime.now());
        boolean result = updateById(sensor);
        if (result) {
            log.info("更新环境传感器成功，传感器ID: {}", sensor.getId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSensor(Long id) {
        boolean result = removeById(id);
        if (result) {
            log.info("删除环境传感器成功，传感器ID: {}", id);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteSensors(List<Long> ids) {
        boolean result = removeByIds(ids);
        if (result) {
            log.info("批量删除环境传感器成功，数量: {}", ids.size());
        }
        return result;
    }

    @Override
    public EnvironmentSensor getSensorDetail(Long id) {
        return getById(id);
    }

    @Override
    public List<EnvironmentSensor> getSensorList(Map<String, Object> params) {
        LambdaQueryWrapper<EnvironmentSensor> wrapper = new LambdaQueryWrapper<>();
        
        if (params.get("roomId") != null) {
            wrapper.eq(EnvironmentSensor::getRoomId, params.get("roomId"));
        }
        if (params.get("sensorType") != null) {
            wrapper.eq(EnvironmentSensor::getSensorType, params.get("sensorType"));
        }
        if (params.get("status") != null) {
            wrapper.eq(EnvironmentSensor::getStatus, params.get("status"));
        }
        if (params.get("keyword") != null) {
            String keyword = params.get("keyword").toString();
            wrapper.and(w -> w.like(EnvironmentSensor::getSensorName, keyword)
                .or().like(EnvironmentSensor::getLocation, keyword));
        }
        
        wrapper.orderByDesc(EnvironmentSensor::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<EnvironmentSensor> getSensorsByRoomId(Long roomId) {
        LambdaQueryWrapper<EnvironmentSensor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvironmentSensor::getRoomId, roomId);
        return list(wrapper);
    }

    @Override
    public List<EnvironmentSensor> getSensorsByType(String sensorType) {
        LambdaQueryWrapper<EnvironmentSensor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvironmentSensor::getSensorType, sensorType);
        return list(wrapper);
    }

    @Override
    public void batchCollectData() {
        List<EnvironmentSensor> sensors = list();
        sensors.forEach(this::collectSensorData);
        log.info("批量采集环境传感器数据完成，采集数量: {}", sensors.size());
    }

    @Override
    public void collectSensorData(Long sensorId) {
        EnvironmentSensor sensor = getById(sensorId);
        if (sensor == null) {
            log.warn("传感器不存在，ID: {}", sensorId);
            return;
        }
        
        try {
            // 根据协议类型采集数据
            collectDataByProtocol(sensor);
            
            // 更新最后采集时间
            sensor.setLastCollectTime(LocalDateTime.now());
            sensor.setStatus("NORMAL");
            updateById(sensor);
            
            log.info("采集传感器数据成功，传感器ID: {}, 名称: {}", sensorId, sensor.getSensorName());
        } catch (Exception e) {
            log.error("采集传感器数据失败，传感器ID: {}, 错误: {}", sensorId, e.getMessage(), e);
            sensor.setStatus("OFFLINE");
            updateById(sensor);
        }
    }

    @Override
    public void manualCollectData(Long sensorId) {
        collectSensorData(sensorId);
    }

    @Override
    public Map<String, Object> getSensorLatestData(Long sensorId) {
        EnvironmentSensor sensor = getById(sensorId);
        if (sensor == null) {
            return Collections.emptyMap();
        }
        
        // 获取传感器最新数据
        List<EnvironmentData> dataList = environmentDataService.getLatestDataBySensorId(sensorId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("sensorId", sensor.getId());
        result.put("sensorName", sensor.getSensorName());
        result.put("sensorType", sensor.getSensorType());
        result.put("location", sensor.getLocation());
        result.put("status", sensor.getStatus());
        result.put("lastCollectTime", sensor.getLastCollectTime());
        result.put("dataList", dataList);
        
        return result;
    }

    @Override
    public Map<String, Object> getRoomSensorStatistics(Long roomId) {
        List<EnvironmentSensor> sensors = getSensorsByRoomId(roomId);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", sensors.size());
        statistics.put("normalCount", sensors.stream().filter(s -> "NORMAL".equals(s.getStatus())).count());
        statistics.put("alarmCount", sensors.stream().filter(s -> "ALARM".equals(s.getStatus())).count());
        statistics.put("offlineCount", sensors.stream().filter(s -> "OFFLINE".equals(s.getStatus())).count());
        
        // 按类型统计
        Map<String, Long> countByType = sensors.stream()
            .collect(Collectors.groupingBy(EnvironmentSensor::getSensorType, Collectors.counting()));
        statistics.put("countByType", countByType);
        
        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSensorStatus(Long id, String status) {
        EnvironmentSensor sensor = getById(id);
        if (sensor != null) {
            sensor.setStatus(status);
            sensor.setUpdateTime(LocalDateTime.now());
            return updateById(sensor);
        }
        return false;
    }

    /**
     * 根据协议类型采集数据
     */
    private void collectDataByProtocol(EnvironmentSensor sensor) {
        String protocol = sensor.getProtocolType();
        
        switch (protocol.toUpperCase()) {
            case "MODBUS":
                collectDataByModbus(sensor);
                break;
            case "SNMP":
                collectDataBySnmp(sensor);
                break;
            case "BACNET":
                collectDataByBacnet(sensor);
                break;
            case "HTTP":
                collectDataByHttp(sensor);
                break;
            default:
                log.warn("不支持的协议类型: {}", protocol);
        }
    }

    /**
     * 通过Modbus协议采集数据
     */
    private void collectDataByModbus(EnvironmentSensor sensor) {
        // TODO: 实现Modbus协议数据采集
        // 这里模拟数据采集
        simulateDataCollection(sensor);
    }

    /**
     * 通过SNMP协议采集数据
     */
    private void collectDataBySnmp(EnvironmentSensor sensor) {
        // TODO: 实现SNMP协议数据采集
        // 这里模拟数据采集
        simulateDataCollection(sensor);
    }

    /**
     * 通过BACnet协议采集数据
     */
    private void collectDataByBacnet(EnvironmentSensor sensor) {
        // TODO: 实现BACnet协议数据采集
        // 这里模拟数据采集
        simulateDataCollection(sensor);
    }

    /**
     * 通过HTTP协议采集数据
     */
    private void collectDataByHttp(EnvironmentSensor sensor) {
        // TODO: 实现HTTP协议数据采集
        // 这里模拟数据采集
        simulateDataCollection(sensor);
    }

    /**
     * 模拟数据采集（生产环境中应替换为真实的协议采集逻辑）
     */
    private void simulateDataCollection(EnvironmentSensor sensor) {
        List<EnvironmentData> dataList = new ArrayList<>();
        LocalDateTime collectTime = LocalDateTime.now();
        
        // 根据传感器类型生成模拟数据
        if ("TEMPERATURE".equals(sensor.getSensorType())) {
            // 温湿度数据
            EnvironmentData tempData = new EnvironmentData();
            tempData.setSensorId(sensor.getId());
            tempData.setRoomId(sensor.getRoomId());
            tempData.setDataType("TEMPERATURE");
            tempData.setValue(generateRandomValue(20.0, 25.0));
            tempData.setUnit("℃");
            tempData.setIsAlarm(0);
            tempData.setCollectTime(collectTime);
            dataList.add(tempData);
            
            EnvironmentData humidityData = new EnvironmentData();
            humidityData.setSensorId(sensor.getId());
            humidityData.setRoomId(sensor.getRoomId());
            humidityData.setDataType("HUMIDITY");
            humidityData.setValue(generateRandomValue(50.0, 60.0));
            humidityData.setUnit("%");
            humidityData.setIsAlarm(0);
            humidityData.setCollectTime(collectTime);
            dataList.add(humidityData);
        } else if ("WATER".equals(sensor.getSensorType())) {
            // 漏水数据
            EnvironmentData data = new EnvironmentData();
            data.setSensorId(sensor.getId());
            data.setRoomId(sensor.getRoomId());
            data.setDataType("WATER");
            data.setValue(generateRandomValue(0.0, 1.0));
            data.setUnit("");
            data.setIsAlarm(data.getValue() > 0.5 ? 1 : 0);
            if (data.getIsAlarm() == 1) {
                data.setAlarmLevel("CRITICAL");
                data.setAlarmMessage("检测到漏水");
            }
            data.setCollectTime(collectTime);
            dataList.add(data);
        } else if ("SMOKE".equals(sensor.getSensorType())) {
            // 烟感数据
            EnvironmentData data = new EnvironmentData();
            data.setSensorId(sensor.getId());
            data.setRoomId(sensor.getRoomId());
            data.setDataType("SMOKE");
            data.setValue(generateRandomValue(0.0, 1.0));
            data.setUnit("");
            data.setIsAlarm(data.getValue() > 0.5 ? 1 : 0);
            if (data.getIsAlarm() == 1) {
                data.setAlarmLevel("EMERGENCY");
                data.setAlarmMessage("检测到烟雾");
            }
            data.setCollectTime(collectTime);
            dataList.add(data);
        }
        
        // 保存采集数据
        environmentDataService.saveBatch(dataList);
    }

    /**
     * 生成随机值
     */
    private double generateRandomValue(double min, double max) {
        Random random = new Random();
        return min + (max - min) * random.nextDouble();
    }
}
