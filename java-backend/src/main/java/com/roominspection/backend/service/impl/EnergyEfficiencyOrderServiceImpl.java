package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.EnergyEfficiencyOrder;
import com.roominspection.backend.mapper.EnergyEfficiencyOrderMapper;
import com.roominspection.backend.service.EnergyEfficiencyOrderService;
import com.roominspection.backend.service.EnvironmentDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 能效优化工单服务实现类
 */
@Slf4j
@Service
public class EnergyEfficiencyOrderServiceImpl extends ServiceImpl<EnergyEfficiencyOrderMapper, EnergyEfficiencyOrder> implements EnergyEfficiencyOrderService {

    @Autowired
    private EnvironmentDataService environmentDataService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(EnergyEfficiencyOrder order) {
        // 生成工单编号
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setStatus("PENDING");
        
        save(order);
        log.info("创建能效工单成功，工单编号: {}", orderNo);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrder(EnergyEfficiencyOrder order) {
        order.setUpdateTime(LocalDateTime.now());
        boolean result = updateById(order);
        if (result) {
            log.info("更新能效工单成功，工单ID: {}", order.getId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrder(Long id) {
        boolean result = removeById(id);
        if (result) {
            log.info("删除能效工单成功，工单ID: {}", id);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteOrders(List<Long> ids) {
        boolean result = removeByIds(ids);
        if (result) {
            log.info("批量删除能效工单成功，数量: {}", ids.size());
        }
        return result;
    }

    @Override
    public EnergyEfficiencyOrder getOrderDetail(Long id) {
        return getById(id);
    }

    @Override
    public List<EnergyEfficiencyOrder> getOrderList(Map<String, Object> params) {
        LambdaQueryWrapper<EnergyEfficiencyOrder> wrapper = new LambdaQueryWrapper<>();
        
        if (params.get("roomId") != null) {
            wrapper.eq(EnergyEfficiencyOrder::getRoomId, params.get("roomId"));
        }
        if (params.get("orderType") != null) {
            wrapper.eq(EnergyEfficiencyOrder::getOrderType, params.get("orderType"));
        }
        if (params.get("status") != null) {
            wrapper.eq(EnergyEfficiencyOrder::getStatus, params.get("status"));
        }
        if (params.get("priority") != null) {
            wrapper.eq(EnergyEfficiencyOrder::getPriority, params.get("priority"));
        }
        if (params.get("assigneeId") != null) {
            wrapper.eq(EnergyEfficiencyOrder::getAssigneeId, params.get("assigneeId"));
        }
        if (params.get("startTime") != null && params.get("endTime") != null) {
            wrapper.between(EnergyEfficiencyOrder::getTriggerTime, params.get("startTime"), params.get("endTime"));
        }
        
        wrapper.orderByDesc(EnergyEfficiencyOrder::getTriggerTime);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignOrder(Long orderId, Long assigneeId, String assigneeName) {
        EnergyEfficiencyOrder order = getById(orderId);
        if (order == null) {
            log.warn("工单不存在，ID: {}", orderId);
            return false;
        }
        
        order.setAssigneeId(assigneeId);
        order.setAssigneeName(assigneeName);
        order.setAssignTime(LocalDateTime.now());
        order.setStatus("PROCESSING");
        order.setUpdateTime(LocalDateTime.now());
        
        boolean result = updateById(order);
        if (result) {
            log.info("指派能效工单成功，工单ID: {}, 负责人: {}", orderId, assigneeName);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeOrder(Long orderId, String result, String suggestion) {
        EnergyEfficiencyOrder order = getById(orderId);
        if (order == null) {
            log.warn("工单不存在，ID: {}", orderId);
            return false;
        }
        
        order.setResult(result);
        order.setSuggestion(suggestion);
        order.setActualCompleteTime(LocalDateTime.now());
        order.setStatus("COMPLETED");
        order.setUpdateTime(LocalDateTime.now());
        
        boolean updateResult = updateById(order);
        if (updateResult) {
            log.info("完成能效工单成功，工单ID: {}", orderId);
        }
        return updateResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closeOrder(Long orderId) {
        EnergyEfficiencyOrder order = getById(orderId);
        if (order == null) {
            log.warn("工单不存在，ID: {}", orderId);
            return false;
        }
        
        order.setStatus("CLOSED");
        order.setUpdateTime(LocalDateTime.now());
        
        boolean result = updateById(order);
        if (result) {
            log.info("关闭能效工单成功，工单ID: {}", orderId);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long triggerHighTempOrder(Long roomId, Long abnormalAreaId, String abnormalArea, Double triggerValue) {
        EnergyEfficiencyOrder order = new EnergyEfficiencyOrder();
        order.setRoomId(roomId);
        order.setOrderType("HIGH_TEMP");
        order.setTitle("高温区域检查工单");
        order.setDescription("持续高温区域自动触发，请检查冷通道封闭或设备风道");
        order.setTriggerCondition("持续高温");
        order.setTriggerValue(triggerValue);
        order.setTriggerTime(LocalDateTime.now());
        order.setAbnormalAreaId(abnormalAreaId);
        order.setAbnormalArea(abnormalArea);
        order.setPriority("HIGH");
        order.setSuggestion("1. 检查冷通道封闭是否完好\n2. 检查设备风道是否正常\n3. 检查空调设备运行状态\n4. 必要时调整空调温度设置");
        
        return createOrder(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long triggerHumidityRiseOrder(Long roomId, Long abnormalAreaId, String abnormalArea, Double triggerValue) {
        EnergyEfficiencyOrder order = new EnergyEfficiencyOrder();
        order.setRoomId(roomId);
        order.setOrderType("HUMIDITY_RISE");
        order.setTitle("湿度预警排查工单");
        order.setDescription("湿度缓升但未达报警值，推送预防性工单");
        order.setTriggerCondition("湿度缓升");
        order.setTriggerValue(triggerValue);
        order.setTriggerTime(LocalDateTime.now());
        order.setAbnormalAreaId(abnormalAreaId);
        order.setAbnormalArea(abnormalArea);
        order.setPriority("MEDIUM");
        order.setSuggestion("1. 排查潜在冷凝风险\n2. 检查除湿设备运行状态\n3. 检查机房密封情况\n4. 检查外部环境湿度变化");
        
        return createOrder(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long triggerColdAirCheckOrder(Long roomId, Long abnormalAreaId, String abnormalArea) {
        EnergyEfficiencyOrder order = new EnergyEfficiencyOrder();
        order.setRoomId(roomId);
        order.setOrderType("COLD_AIR");
        order.setTitle("冷通道检查工单");
        order.setDescription("检查冷通道封闭情况");
        order.setTriggerCondition("热力图分析");
        order.setTriggerTime(LocalDateTime.now());
        order.setAbnormalAreaId(abnormalAreaId);
        order.setAbnormalArea(abnormalArea);
        order.setPriority("MEDIUM");
        order.setSuggestion("1. 检查冷通道封闭板是否完好\n2. 检查冷通道门是否关闭\n3. 检查冷通道是否有冷气泄漏\n4. 记录冷通道温度数据");
        
        return createOrder(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long triggerAirDuctCheckOrder(Long roomId, Long abnormalAreaId, String abnormalArea) {
        EnergyEfficiencyOrder order = new EnergyEfficiencyOrder();
        order.setRoomId(roomId);
        order.setOrderType("AIR_DUCT");
        order.setTitle("风道检查工单");
        order.setDescription("检查设备风道运行情况");
        order.setTriggerCondition("热力图分析");
        order.setTriggerTime(LocalDateTime.now());
        order.setAbnormalAreaId(abnormalAreaId);
        order.setAbnormalArea(abnormalArea);
        order.setPriority("MEDIUM");
        order.setSuggestion("1. 检查风道是否畅通\n2. 检查风机运行状态\n3. 检查风道温度分布\n4. 检查风道是否有堵塞");
        
        return createOrder(order);
    }

    @Override
    public void autoCheckAndTriggerOrders() {
        log.info("开始自动检查并触发能效工单");
        
        // 获取所有机房的热力图异常区域
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        
        // 检查温度异常区域
        List<Map<String, Object>> tempAbnormalAreas = environmentDataService.identifyAbnormalAreas(null, "TEMPERATURE", now);
        for (Map<String, Object> area : tempAbnormalAreas) {
            Double temperature = (Double) area.get("value");
            String abnormalType = (String) area.get("abnormalType");
            
            if ("HIGH_TEMP".equals(abnormalType) && temperature > 28.0) {
                // 触发高温工单
                String abnormalArea = String.format("坐标(%.0f, %.0f)", area.get("coordinateX"), area.get("coordinateY"));
                triggerHighTempOrder(1L, null, abnormalArea, temperature);
            } else if ("HIGH_TEMP".equals(abnormalType)) {
                // 触发冷通道检查工单
                String abnormalArea = String.format("坐标(%.0f, %.0f)", area.get("coordinateX"), area.get("coordinateY"));
                triggerColdAirCheckOrder(1L, null, abnormalArea);
            }
        }
        
        // 检查湿度异常区域（缓升但未达报警值）
        List<Map<String, Object>> humidityAbnormalAreas = environmentDataService.identifyAbnormalAreas(null, "HUMIDITY", now);
        for (Map<String, Object> area : humidityAbnormalAreas) {
            Double humidity = (Double) area.get("value");
            String abnormalType = (String) area.get("abnormalType");
            
            if ("HIGH_HUMIDITY".equals(abnormalType) && humidity >= 60.0 && humidity < 70.0) {
                // 触发湿度预警工单
                String abnormalArea = String.format("坐标(%.0f, %.0f)", area.get("coordinateX"), area.get("coordinateY"));
                triggerHumidityRiseOrder(1L, null, abnormalArea, humidity);
            }
        }
        
        log.info("自动检查并触发能效工单完成");
    }

    @Override
    public Map<String, Object> getOrderStatistics(Long roomId) {
        LambdaQueryWrapper<EnergyEfficiencyOrder> wrapper = new LambdaQueryWrapper<>();
        if (roomId != null) {
            wrapper.eq(EnergyEfficiencyOrder::getRoomId, roomId);
        }
        
        List<EnergyEfficiencyOrder> orders = list(wrapper);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", orders.size());
        statistics.put("pendingCount", orders.stream().filter(o -> "PENDING".equals(o.getStatus())).count());
        statistics.put("processingCount", orders.stream().filter(o -> "PROCESSING".equals(o.getStatus())).count());
        statistics.put("completedCount", orders.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count());
        statistics.put("closedCount", orders.stream().filter(o -> "CLOSED".equals(o.getStatus())).count());
        
        // 按工单类型统计
        Map<String, Long> countByType = orders.stream()
            .collect(Collectors.groupingBy(EnergyEfficiencyOrder::getOrderType, Collectors.counting()));
        statistics.put("countByType", countByType);
        
        // 按优先级统计
        Map<String, Long> countByPriority = orders.stream()
            .collect(Collectors.groupingBy(EnergyEfficiencyOrder::getPriority, Collectors.counting()));
        statistics.put("countByPriority", countByPriority);
        
        return statistics;
    }

    @Override
    public List<Map<String, Object>> getOrderTrend(Long roomId, Integer days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);
        
        LambdaQueryWrapper<EnergyEfficiencyOrder> wrapper = new LambdaQueryWrapper<>();
        if (roomId != null) {
            wrapper.eq(EnergyEfficiencyOrder::getRoomId, roomId);
        }
        wrapper.between(EnergyEfficiencyOrder::getTriggerTime, startTime, endTime);
        
        List<EnergyEfficiencyOrder> orders = list(wrapper);
        
        // 按日期聚合数据
        Map<String, List<EnergyEfficiencyOrder>> groupedData = orders.stream()
            .collect(Collectors.groupingBy(order -> order.getTriggerTime().toLocalDate().toString()));
        
        List<Map<String, Object>> trendData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = 0; i < days; i++) {
            LocalDateTime dayTime = startTime.plusDays(i);
            String dateKey = dayTime.toLocalDate().format(formatter);
            
            List<EnergyEfficiencyOrder> dayOrders = groupedData.get(dateKey);
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("date", dateKey);
            dataPoint.put("count", dayOrders != null ? dayOrders.size() : 0);
            dataPoint.put("highPriorityCount", dayOrders != null ? dayOrders.stream().filter(o -> "HIGH".equals(o.getPriority()) || "URGENT".equals(o.getPriority())).count() : 0);
            dataPoint.put("completedCount", dayOrders != null ? dayOrders.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count() : 0);
            
            trendData.add(dataPoint);
        }
        
        return trendData;
    }

    /**
     * 生成工单编号
     */
    private String generateOrderNo() {
        AtomicInteger counter = new AtomicInteger(0);
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "EE" + dateStr + String.format("%06d", counter.incrementAndGet());
    }
}
