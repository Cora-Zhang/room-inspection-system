package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.EfficiencyCheckOrder;
import com.roominspection.backend.mapper.EfficiencyCheckOrderMapper;
import com.roominspection.backend.service.EfficiencyCheckOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 效率核查工单Service实现
 */
@Service
public class EfficiencyCheckOrderServiceImpl extends ServiceImpl<EfficiencyCheckOrderMapper, EfficiencyCheckOrder>
        implements EfficiencyCheckOrderService {

    @Override
    public List<EfficiencyCheckOrder> listByAcId(Long acId) {
        LambdaQueryWrapper<EfficiencyCheckOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EfficiencyCheckOrder::getAcId, acId);
        wrapper.orderByDesc(EfficiencyCheckOrder::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<EfficiencyCheckOrder> listByRoomId(Long roomId) {
        LambdaQueryWrapper<EfficiencyCheckOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(roomId != null, EfficiencyCheckOrder::getRoomId, roomId);
        wrapper.orderByDesc(EfficiencyCheckOrder::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<EfficiencyCheckOrder> listByStatus(Integer status) {
        LambdaQueryWrapper<EfficiencyCheckOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, EfficiencyCheckOrder::getStatus, status);
        wrapper.orderByDesc(EfficiencyCheckOrder::getCreateTime);
        return list(wrapper);
    }

    @Override
    @Transactional
    public void assignOrder(Long orderId, String assignee) {
        EfficiencyCheckOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("工单不存在");
        }

        if (order.getStatus() != 0) {
            throw new RuntimeException("工单状态不允许指派");
        }

        order.setAssignee(assignee);
        order.setAssignTime(LocalDateTime.now());
        order.setStatus(1); // 1-待处理
        order.setUpdateTime(LocalDateTime.now());

        updateById(order);
    }

    @Override
    @Transactional
    public void startOrder(Long orderId) {
        EfficiencyCheckOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("工单不存在");
        }

        if (order.getStatus() != 1) {
            throw new RuntimeException("工单状态不允许开始处理");
        }

        order.setStatus(2); // 2-处理中
        order.setStartTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        updateById(order);
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId, String handleResult, String handleDescription, String photos) {
        EfficiencyCheckOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("工单不存在");
        }

        if (order.getStatus() != 2) {
            throw new RuntimeException("工单状态不允许完成");
        }

        order.setStatus(3); // 3-已完成
        order.setCompleteTime(LocalDateTime.now());
        order.setHandleResult(handleResult);
        order.setHandleDescription(handleDescription);
        order.setPhotos(photos);
        order.setUpdateTime(LocalDateTime.now());

        updateById(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        EfficiencyCheckOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("工单不存在");
        }

        if (order.getStatus() == 3) {
            throw new RuntimeException("已完成的工单不能取消");
        }

        order.setStatus(4); // 4-已取消
        order.setHandleResult(reason);
        order.setUpdateTime(LocalDateTime.now());

        updateById(order);
    }

    @Override
    public boolean hasOpenOrder(Long acId, Integer orderType) {
        LambdaQueryWrapper<EfficiencyCheckOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EfficiencyCheckOrder::getAcId, acId);
        wrapper.eq(orderType != null, EfficiencyCheckOrder::getOrderType, orderType);
        wrapper.in(EfficiencyCheckOrder::getStatus, 0, 1, 2); // 待指派、待处理、处理中
        wrapper.orderByDesc(EfficiencyCheckOrder::getCreateTime);
        wrapper.last("LIMIT 1");
        return count(wrapper) > 0;
    }

    @Override
    public Map<String, Object> getStatistics(Long roomId) {
        LambdaQueryWrapper<EfficiencyCheckOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(roomId != null, EfficiencyCheckOrder::getRoomId, roomId);
        List<EfficiencyCheckOrder> orderList = list(wrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", orderList.size());

        long pendingCount = orderList.stream().filter(o -> o.getStatus() == 0).count();
        long assignedCount = orderList.stream().filter(o -> o.getStatus() == 1).count();
        long processingCount = orderList.stream().filter(o -> o.getStatus() == 2).count();
        long completedCount = orderList.stream().filter(o -> o.getStatus() == 3).count();
        long cancelledCount = orderList.stream().filter(o -> o.getStatus() == 4).count();

        stats.put("pending", pendingCount);
        stats.put("assigned", assignedCount);
        stats.put("processing", processingCount);
        stats.put("completed", completedCount);
        stats.put("cancelled", cancelledCount);

        // 按类型统计
        Map<Integer, Long> typeStats = orderList.stream()
                .collect(Collectors.groupingBy(EfficiencyCheckOrder::getOrderType, Collectors.counting()));
        stats.put("typeStats", typeStats);

        return stats;
    }
}
