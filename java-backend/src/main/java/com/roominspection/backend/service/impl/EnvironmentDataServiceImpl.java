package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.EnvironmentData;
import com.roominspection.backend.entity.EnvironmentSensor;
import com.roominspection.backend.entity.HeatmapData;
import com.roominspection.backend.mapper.EnvironmentDataMapper;
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
 * 环境数据服务实现类
 */
@Slf4j
@Service
public class EnvironmentDataServiceImpl extends ServiceImpl<EnvironmentDataMapper, EnvironmentData> implements EnvironmentDataService {

    @Autowired
    private EnvironmentSensorService environmentSensorService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveEnvironmentData(EnvironmentData data) {
        data.setCreateTime(LocalDateTime.now());
        return save(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchEnvironmentData(List<EnvironmentData> dataList) {
        LocalDateTime now = LocalDateTime.now();
        dataList.forEach(data -> data.setCreateTime(now));
        return saveBatch(dataList);
    }

    @Override
    public List<EnvironmentData> getLatestDataBySensorId(Long sensorId) {
        LambdaQueryWrapper<EnvironmentData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvironmentData::getSensorId, sensorId)
            .orderByDesc(EnvironmentData::getCollectTime)
            .last("LIMIT 10");
        return list(wrapper);
    }

    @Override
    public List<EnvironmentData> getLatestDataByRoomId(Long roomId) {
        // 获取机房所有传感器
        List<EnvironmentSensor> sensors = environmentSensorService.getSensorsByRoomId(roomId);
        if (sensors.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Long> sensorIds = sensors.stream().map(EnvironmentSensor::getId).collect(Collectors.toList());
        
        LambdaQueryWrapper<EnvironmentData> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(EnvironmentData::getSensorId, sensorIds)
            .eq(EnvironmentData::getIsAlarm, 0)
            .orderByDesc(EnvironmentData::getCollectTime)
            .last("LIMIT 100");
        return list(wrapper);
    }

    @Override
    public List<EnvironmentData> getDataByType(Long roomId, String dataType, LocalDateTime startTime, LocalDateTime endTime) {
        // 获取机房所有传感器
        List<EnvironmentSensor> sensors = environmentSensorService.getSensorsByRoomId(roomId);
        if (sensors.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Long> sensorIds = sensors.stream().map(EnvironmentSensor::getId).collect(Collectors.toList());
        
        LambdaQueryWrapper<EnvironmentData> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(EnvironmentData::getSensorId, sensorIds)
            .eq(EnvironmentData::getDataType, dataType)
            .between(EnvironmentData::getCollectTime, startTime, endTime)
            .orderByAsc(EnvironmentData::getCollectTime);
        return list(wrapper);
    }

    @Override
    public List<Map<String, Object>> getDataTrend(Long roomId, String dataType, Integer hours) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(hours);
        
        List<EnvironmentData> dataList = getDataByType(roomId, dataType, startTime, endTime);
        
        // 按小时聚合数据
        Map<Integer, List<EnvironmentData>> groupedData = dataList.stream()
            .collect(Collectors.groupingBy(data -> data.getCollectTime().getHour()));
        
        List<Map<String, Object>> trendData = new ArrayList<>();
        for (int i = 0; i < hours; i++) {
            LocalDateTime hourTime = startTime.plusHours(i);
            int hour = hourTime.getHour();
            
            List<EnvironmentData> hourData = groupedData.get(hour);
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("time", hourTime);
            dataPoint.put("hour", hour);
            
            if (hourData != null && !hourData.isEmpty()) {
                double avgValue = hourData.stream().mapToDouble(EnvironmentData::getValue).average().orElse(0.0);
                double maxValue = hourData.stream().mapToDouble(EnvironmentData::getValue).max().orElse(0.0);
                double minValue = hourData.stream().mapToDouble(EnvironmentData::getValue).min().orElse(0.0);
                
                dataPoint.put("avgValue", avgValue);
                dataPoint.put("maxValue", maxValue);
                dataPoint.put("minValue", minValue);
                dataPoint.put("count", hourData.size());
            } else {
                dataPoint.put("avgValue", 0.0);
                dataPoint.put("maxValue", 0.0);
                dataPoint.put("minValue", 0.0);
                dataPoint.put("count", 0);
            }
            
            trendData.add(dataPoint);
        }
        
        return trendData;
    }

    @Override
    public List<EnvironmentData> getAlarmDataList(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        // 获取机房所有传感器
        List<EnvironmentSensor> sensors = environmentSensorService.getSensorsByRoomId(roomId);
        if (sensors.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Long> sensorIds = sensors.stream().map(EnvironmentSensor::getId).collect(Collectors.toList());
        
        LambdaQueryWrapper<EnvironmentData> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(EnvironmentData::getSensorId, sensorIds)
            .eq(EnvironmentData::getIsAlarm, 1)
            .between(EnvironmentData::getCollectTime, startTime, endTime)
            .orderByDesc(EnvironmentData::getCollectTime);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<HeatmapData> generateHeatmapData(Long roomId, String heatmapType, LocalDateTime dataTime) {
        // 获取机房所有温湿度传感器
        List<EnvironmentSensor> sensors = environmentSensorService.getSensorsByType("TEMPERATURE");
        if (sensors.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 筛选指定机房的传感器
        sensors = sensors.stream()
            .filter(sensor -> sensor.getRoomId().equals(roomId))
            .filter(sensor -> sensor.getCoordinateX() != null && sensor.getCoordinateY() != null)
            .collect(Collectors.toList());
        
        List<HeatmapData> heatmapDataList = new ArrayList<>();
        
        // 获取传感器数据
        for (EnvironmentSensor sensor : sensors) {
            List<EnvironmentData> dataList = getLatestDataBySensorId(sensor.getId());
            if (dataList.isEmpty()) {
                continue;
            }
            
            // 根据热力图类型筛选数据
            Optional<EnvironmentData> dataOpt = dataList.stream()
                .filter(data -> "TEMPERATURE".equals(heatmapType) ? "TEMPERATURE".equals(data.getDataType()) : "HUMIDITY".equals(data.getDataType()))
                .findFirst();
            
            if (!dataOpt.isPresent()) {
                continue;
            }
            
            EnvironmentData data = dataOpt.get();
            
            HeatmapData heatmapData = new HeatmapData();
            heatmapData.setRoomId(roomId);
            heatmapData.setHeatmapType(heatmapType);
            heatmapData.setCoordinateX(sensor.getCoordinateX());
            heatmapData.setCoordinateY(sensor.getCoordinateY());
            heatmapData.setTemperature("TEMPERATURE".equals(heatmapType) ? data.getValue() : null);
            heatmapData.setHumidity("HUMIDITY".equals(heatmapType) ? data.getValue() : null);
            
            // 计算热力值（归一化到0-1之间）
            double heatValue = calculateHeatValue(data.getValue(), heatmapType);
            heatmapData.setHeatValue(heatValue);
            
            // 判断是否异常
            boolean isAbnormal = isAbnormalValue(data.getValue(), heatmapType, sensor);
            heatmapData.setIsAbnormal(isAbnormal ? 1 : 0);
            if (isAbnormal) {
                heatmapData.setAbnormalType(identifyAbnormalType(data.getValue(), heatmapType, sensor));
            }
            
            heatmapData.setDataTime(dataTime != null ? dataTime : data.getCollectTime());
            heatmapData.setCreateTime(LocalDateTime.now());
            
            heatmapDataList.add(heatmapData);
        }
        
        // 插值生成更密集的热力图数据
        List<HeatmapData> interpolatedData = interpolateHeatmapData(heatmapDataList);
        
        // 保存热力图数据
        saveHeatmapData(interpolatedData);
        
        return interpolatedData;
    }

    @Override
    public List<Map<String, Object>> identifyAbnormalAreas(Long roomId, String heatmapType, LocalDateTime dataTime) {
        List<HeatmapData> heatmapDataList = generateHeatmapData(roomId, heatmapType, dataTime);
        
        return heatmapDataList.stream()
            .filter(data -> data.getIsAbnormal() == 1)
            .map(data -> {
                Map<String, Object> abnormalArea = new HashMap<>();
                abnormalArea.put("coordinateX", data.getCoordinateX());
                abnormalArea.put("coordinateY", data.getCoordinateY());
                abnormalArea.put("value", "TEMPERATURE".equals(heatmapType) ? data.getTemperature() : data.getHumidity());
                abnormalArea.put("abnormalType", data.getAbnormalType());
                abnormalArea.put("heatValue", data.getHeatValue());
                return abnormalArea;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanHistoryData(LocalDateTime beforeTime) {
        LambdaQueryWrapper<EnvironmentData> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(EnvironmentData::getCollectTime, beforeTime);
        return baseMapper.delete(wrapper);
    }

    @Override
    public Map<String, Object> getDataStatistics(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        // 获取机房所有传感器
        List<EnvironmentSensor> sensors = environmentSensorService.getSensorsByRoomId(roomId);
        if (sensors.isEmpty()) {
            return Collections.emptyMap();
        }
        
        List<Long> sensorIds = sensors.stream().map(EnvironmentSensor::getId).collect(Collectors.toList());
        
        LambdaQueryWrapper<EnvironmentData> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(EnvironmentData::getSensorId, sensorIds)
            .between(EnvironmentData::getCollectTime, startTime, endTime);
        List<EnvironmentData> dataList = list(wrapper);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", dataList.size());
        statistics.put("alarmCount", dataList.stream().filter(data -> data.getIsAlarm() == 1).count());
        
        // 按数据类型统计
        Map<String, Long> countByType = dataList.stream()
            .collect(Collectors.groupingBy(EnvironmentData::getDataType, Collectors.counting()));
        statistics.put("countByType", countByType);
        
        // 按告警级别统计
        Map<String, Long> countByLevel = dataList.stream()
            .filter(data -> data.getAlarmLevel() != null)
            .collect(Collectors.groupingBy(EnvironmentData::getAlarmLevel, Collectors.counting()));
        statistics.put("countByLevel", countByLevel);
        
        return statistics;
    }

    /**
     * 计算热力值
     */
    private double calculateHeatValue(double value, String heatmapType) {
        double minValue, maxValue;
        
        if ("TEMPERATURE".equals(heatmapType)) {
            minValue = 18.0;
            maxValue = 30.0;
        } else {
            minValue = 40.0;
            maxValue = 70.0;
        }
        
        // 归一化到0-1之间
        return (value - minValue) / (maxValue - minValue);
    }

    /**
     * 判断是否异常
     */
    private boolean isAbnormalValue(double value, String heatmapType, EnvironmentSensor sensor) {
        if ("TEMPERATURE".equals(heatmapType)) {
            return value > sensor.getTempThresholdHigh() || value < sensor.getTempThresholdLow();
        } else {
            return value > sensor.getHumidityThresholdHigh() || value < sensor.getHumidityThresholdLow();
        }
    }

    /**
     * 识别异常类型
     */
    private String identifyAbnormalType(double value, String heatmapType, EnvironmentSensor sensor) {
        if ("TEMPERATURE".equals(heatmapType)) {
            if (value > sensor.getTempThresholdHigh()) {
                return "HIGH_TEMP";
            } else if (value < sensor.getTempThresholdLow()) {
                return "LOW_TEMP";
            }
        } else {
            if (value > sensor.getHumidityThresholdHigh()) {
                return "HIGH_HUMIDITY";
            } else if (value < sensor.getHumidityThresholdLow()) {
                return "LOW_HUMIDITY";
            }
        }
        return null;
    }

    /**
     * 插值生成更密集的热力图数据
     */
    private List<HeatmapData> interpolateHeatmapData(List<HeatmapData> originalData) {
        // 使用反距离加权插值法（IDW）生成更多数据点
        List<HeatmapData> interpolatedData = new ArrayList<>(originalData);
        
        // 这里简化处理，实际应用中可使用更复杂的插值算法
        // 在每个原始点周围生成插值点
        for (HeatmapData data : originalData) {
            if (data.getIsAbnormal() == 1) {
                // 在异常区域周围生成更多数据点
                double[][] offsets = {
                    {10, 0}, {-10, 0}, {0, 10}, {0, -10},
                    {7, 7}, {7, -7}, {-7, 7}, {-7, -7}
                };
                
                for (double[] offset : offsets) {
                    HeatmapData newPoint = new HeatmapData();
                    newPoint.setRoomId(data.getRoomId());
                    newPoint.setHeatmapType(data.getHeatmapType());
                    newPoint.setCoordinateX(data.getCoordinateX() + offset[0]);
                    newPoint.setCoordinateY(data.getCoordinateY() + offset[1]);
                    newPoint.setTemperature(data.getTemperature());
                    newPoint.setHumidity(data.getHumidity());
                    newPoint.setHeatValue(data.getHeatValue() * 0.8); // 热力值衰减
                    newPoint.setIsAbnormal(0); // 插值点不标记为异常
                    newPoint.setDataTime(data.getDataTime());
                    newPoint.setCreateTime(LocalDateTime.now());
                    
                    interpolatedData.add(newPoint);
                }
            }
        }
        
        return interpolatedData;
    }

    /**
     * 保存热力图数据
     */
    private void saveHeatmapData(List<HeatmapData> heatmapDataList) {
        // TODO: 实现保存热力图数据到数据库
        // 这里先不实现，避免循环依赖
        log.debug("保存热力图数据，数量: {}", heatmapDataList.size());
    }
}
