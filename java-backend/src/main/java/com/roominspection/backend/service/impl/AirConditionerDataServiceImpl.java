package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.AirConditionerData;
import com.roominspection.backend.mapper.AirConditionerDataMapper;
import com.roominspection.backend.service.AirConditionerDataService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 空调运行数据Service实现
 */
@Service
public class AirConditionerDataServiceImpl extends ServiceImpl<AirConditionerDataMapper, AirConditionerData>
        implements AirConditionerDataService {

    @Override
    public AirConditionerData getLatestByAcId(Long acId) {
        LambdaQueryWrapper<AirConditionerData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AirConditionerData::getAcId, acId);
        wrapper.orderByDesc(AirConditionerData::getCollectTime);
        wrapper.last("LIMIT 1");
        return getOne(wrapper);
    }

    @Override
    public List<AirConditionerData> listByAcId(Long acId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AirConditionerData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AirConditionerData::getAcId, acId);
        if (startTime != null) {
            wrapper.ge(AirConditionerData::getCollectTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AirConditionerData::getCollectTime, endTime);
        }
        wrapper.orderByDesc(AirConditionerData::getCollectTime);
        return list(wrapper);
    }

    @Override
    public List<AirConditionerData> listAbnormalData(Long acId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AirConditionerData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AirConditionerData::getAcId, acId);
        wrapper.eq(AirConditionerData::getIsDiffAbnormal, 1); // 1-温差异常
        if (startTime != null) {
            wrapper.ge(AirConditionerData::getCollectTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AirConditionerData::getCollectTime, endTime);
        }
        wrapper.orderByDesc(AirConditionerData::getCollectTime);
        return list(wrapper);
    }

    @Override
    public List<Map<String, Object>> getTemperatureTrend(Long acId, LocalDateTime startTime, LocalDateTime endTime) {
        List<AirConditionerData> dataList = listByAcId(acId, startTime, endTime);

        // 按时间分组统计（每小时一个点）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        Map<String, List<AirConditionerData>> groupedData = dataList.stream()
                .collect(Collectors.groupingBy(data -> data.getCollectTime().format(formatter)));

        List<Map<String, Object>> trend = new ArrayList<>();
        groupedData.forEach((timeKey, list) -> {
            Map<String, Object> point = new HashMap<>();
            point.put("time", timeKey + ":00");

            // 计算平均值
            double avgSetTemp = list.stream().mapToDouble(AirConditionerData::getSetTemperature).average().orElse(0);
            double avgReturnTemp = list.stream().mapToDouble(AirConditionerData::getReturnTemperature).average().orElse(0);
            double avgSupplyTemp = list.stream().mapToDouble(AirConditionerData::getSupplyTemperature).average().orElse(0);

            point.put("setTemperature", Math.round(avgSetTemp * 10) / 10.0);
            point.put("returnTemperature", Math.round(avgReturnTemp * 10) / 10.0);
            point.put("supplyTemperature", Math.round(avgSupplyTemp * 10) / 10.0);

            trend.add(point);
        });

        // 按时间排序
        trend.sort((a, b) -> ((String) a.get("time")).compareTo((String) b.get("time")));

        return trend;
    }

    @Override
    public List<Map<String, Object>> getPowerTrend(Long acId, LocalDateTime startTime, LocalDateTime endTime) {
        List<AirConditionerData> dataList = listByAcId(acId, startTime, endTime);

        // 按时间分组统计（每小时一个点）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        Map<String, List<AirConditionerData>> groupedData = dataList.stream()
                .collect(Collectors.groupingBy(data -> data.getCollectTime().format(formatter)));

        List<Map<String, Object>> trend = new ArrayList<>();
        groupedData.forEach((timeKey, list) -> {
            Map<String, Object> point = new HashMap<>();
            point.put("time", timeKey + ":00");

            // 计算平均功率
            double avgPower = list.stream().mapToDouble(AirConditionerData::getCurrentPower).average().orElse(0);

            point.put("power", Math.round(avgPower * 10) / 10.0);

            trend.add(point);
        });

        // 按时间排序
        trend.sort((a, b) -> ((String) a.get("time")).compareTo((String) b.get("time")));

        return trend;
    }
}
