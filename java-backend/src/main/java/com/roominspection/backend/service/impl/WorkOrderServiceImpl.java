package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.AlarmRecord;
import com.roominspection.backend.entity.Device;
import com.roominspection.backend.entity.WorkOrder;
import com.roominspection.backend.entity.WorkOrderFlow;
import com.roominspection.backend.mapper.DeviceMapper;
import com.roominspection.backend.mapper.WorkOrderFlowMapper;
import com.roominspection.backend.mapper.WorkOrderMapper;
import com.roominspection.backend.service.AlarmRecordService;
import com.roominspection.backend.service.WorkOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 工单Service实现
 */
@Slf4j
@Service
public class WorkOrderServiceImpl extends ServiceImpl<WorkOrderMapper, WorkOrder>
        implements WorkOrderService {

    @Autowired
    private WorkOrderFlowMapper workOrderFlowMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private AlarmRecordService alarmRecordService;

    @Override
    public List<WorkOrder> listByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.findByTimeRange(startTime, endTime);
    }

    @Override
    public List<WorkOrder> listByStatus(String status) {
        return baseMapper.findByStatus(status);
    }

    @Override
    public List<WorkOrder> listByType(String type) {
        return baseMapper.findByType(type);
    }

    @Override
    public List<WorkOrder> listByOwnerId(String ownerId) {
        return baseMapper.findByOwnerId(ownerId);
    }

    @Override
    @Transactional
    public boolean createWorkOrder(WorkOrder workOrder) {
        workOrder.setId(UUID.randomUUID().toString().replace("-", ""));
        workOrder.setOrderCode("WO-" + System.currentTimeMillis());
        workOrder.setStatus("PENDING");
        workOrder.setIsOverdue(false);
        workOrder.setCreatedAt(LocalDateTime.now());
        workOrder.setUpdatedAt(LocalDateTime.now());

        // 填充机房和设备名称
        if (workOrder.getRoomId() != null && workOrder.getRoomName() == null) {
            // 查询机房名称
        }
        if (workOrder.getDeviceId() != null && workOrder.getDeviceName() == null) {
            Device device = deviceMapper.selectById(workOrder.getDeviceId());
            if (device != null) {
                workOrder.setDeviceName(device.getDeviceName());
            }
        }

        boolean result = save(workOrder);

        // 记录创建流程
        if (result) {
            recordFlow(workOrder.getId(), workOrder.getOrderCode(), "CREATE",
                    null, "PENDING", workOrder.getCreatedBy(),
                    workOrder.getCreatedByName(), "创建工单", null);
        }

        return result;
    }

    @Override
    @Transactional
    public WorkOrder autoCreateFromAlarm(String alarmId) {
        AlarmRecord alarm = alarmRecordService.getById(alarmId);
        if (alarm == null) {
            throw new RuntimeException("告警记录不存在");
        }

        WorkOrder workOrder = new WorkOrder();
        workOrder.setTitle("告警处理工单：" + alarm.getTitle());
        workOrder.setDescription(alarm.getContent());
        workOrder.setType("MAINTENANCE"); // 维修工单

        // 根据告警级别设置优先级
        String priority = "MEDIUM";
        if ("CRITICAL".equals(alarm.getLevel())) {
            priority = "URGENT";
        } else if ("MAJOR".equals(alarm.getLevel())) {
            priority = "HIGH";
        }
        workOrder.setPriority(priority);

        workOrder.setRoomId(alarm.getRoomId());
        workOrder.setDeviceId(alarm.getDeviceId());
        workOrder.setDeviceName(alarm.getDeviceName());

        // 创建工单
        boolean created = createWorkOrder(workOrder);
        if (created) {
            // 关联告警
            alarmRecordService.linkWorkOrder(alarmId, workOrder.getId());
        }

        return workOrder;
    }

    @Override
    @Transactional
    public boolean assignWorkOrder(String id, String assignedTo, String assignedToName, String priority) {
        WorkOrder workOrder = getById(id);
        if (workOrder == null) {
            throw new RuntimeException("工单不存在");
        }

        String oldStatus = workOrder.getStatus();

        workOrder.setStatus("ASSIGNED");
        workOrder.setAssignedTo(assignedTo);
        workOrder.setAssignedToName(assignedToName);
        workOrder.setAssignedAt(LocalDateTime.now());

        if (priority != null) {
            workOrder.setPriority(priority);
        }

        workOrder.setUpdatedAt(LocalDateTime.now());

        boolean result = updateById(workOrder);

        // 记录指派流程
        if (result) {
            recordFlow(workOrder.getId(), workOrder.getOrderCode(), "ASSIGN",
                    oldStatus, "ASSIGNED", assignedTo, assignedToName,
                    "工单已指派", null);
        }

        return result;
    }

    @Override
    @Transactional
    public boolean startWorkOrder(String id, String ownerId, String ownerName) {
        WorkOrder workOrder = getById(id);
        if (workOrder == null) {
            throw new RuntimeException("工单不存在");
        }

        String oldStatus = workOrder.getStatus();

        workOrder.setStatus("IN_PROGRESS");
        workOrder.setOwnerId(ownerId);
        workOrder.setOwnerName(ownerName);
        workOrder.setActualStartTime(LocalDateTime.now());
        workOrder.setUpdatedAt(LocalDateTime.now());

        boolean result = updateById(workOrder);

        // 记录开始流程
        if (result) {
            recordFlow(workOrder.getId(), workOrder.getOrderCode(), "START",
                    oldStatus, "IN_PROGRESS", ownerId, ownerName,
                    "开始处理工单", null);
        }

        return result;
    }

    @Override
    @Transactional
    public boolean completeWorkOrder(String id, String handleResult, String handleDescription, Double duration) {
        WorkOrder workOrder = getById(id);
        if (workOrder == null) {
            throw new RuntimeException("工单不存在");
        }

        String oldStatus = workOrder.getStatus();

        workOrder.setStatus("COMPLETED");
        workOrder.setHandleResult(handleResult);
        workOrder.setHandleDescription(handleDescription);
        workOrder.setDuration(duration);
        workOrder.setActualEndTime(LocalDateTime.now());
        workOrder.setUpdatedAt(LocalDateTime.now());

        boolean result = updateById(workOrder);

        // 记录完成流程
        if (result) {
            recordFlow(workOrder.getId(), workOrder.getOrderCode(), "COMPLETE",
                    oldStatus, "COMPLETED", workOrder.getOwnerId(),
                    workOrder.getOwnerName(), "完成工单: " + handleResult, null);
        }

        return result;
    }

    @Override
    @Transactional
    public boolean cancelWorkOrder(String id, String cancelReason) {
        WorkOrder workOrder = getById(id);
        if (workOrder == null) {
            throw new RuntimeException("工单不存在");
        }

        String oldStatus = workOrder.getStatus();

        workOrder.setStatus("CANCELLED");
        workOrder.setCloseReason(cancelReason);
        workOrder.setActualEndTime(LocalDateTime.now());
        workOrder.setUpdatedAt(LocalDateTime.now());

        boolean result = updateById(workOrder);

        // 记录取消流程
        if (result) {
            recordFlow(workOrder.getId(), workOrder.getOrderCode(), "CANCEL",
                    oldStatus, "CANCELLED", workOrder.getAssignedTo(),
                    workOrder.getAssignedToName(), "取消工单: " + cancelReason, null);
        }

        return result;
    }

    @Override
    @Transactional
    public boolean closeWorkOrder(String id, String closeReason, String closedBy, String closedByName) {
        WorkOrder workOrder = getById(id);
        if (workOrder == null) {
            throw new RuntimeException("工单不存在");
        }

        String oldStatus = workOrder.getStatus();

        workOrder.setStatus("CLOSED");
        workOrder.setCloseReason(closeReason);
        workOrder.setClosedBy(closedBy);
        workOrder.setClosedByName(closedByName);
        workOrder.setClosedAt(LocalDateTime.now());
        workOrder.setUpdatedAt(LocalDateTime.now());

        boolean result = updateById(workOrder);

        // 记录关闭流程
        if (result) {
            recordFlow(workOrder.getId(), workOrder.getOrderCode(), "CLOSE",
                    oldStatus, "CLOSED", closedBy, closedByName,
                    "关闭工单: " + closeReason, null);
        }

        return result;
    }

    @Override
    public List<WorkOrder> getOverdueWorkOrders() {
        return baseMapper.findOverdue();
    }

    @Override
    public List<WorkOrder> getPendingWorkOrders() {
        return baseMapper.findPending();
    }

    @Override
    public List<WorkOrder> getUrgentWorkOrders() {
        return baseMapper.findUrgent();
    }

    @Override
    public Map<String, Object> getStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();

        List<Map<String, Object>> statusStats = countByStatus(startTime, endTime);
        Map<String, Long> statusMap = new HashMap<>();
        for (Map<String, Object> item : statusStats) {
            String status = (String) item.get("status");
            Long count = ((Number) item.get("count")).longValue();
            statusMap.put(status, count);
        }

        stats.put("pending", statusMap.getOrDefault("PENDING", 0L));
        stats.put("assigned", statusMap.getOrDefault("ASSIGNED", 0L));
        stats.put("inProgress", statusMap.getOrDefault("IN_PROGRESS", 0L));
        stats.put("completed", statusMap.getOrDefault("COMPLETED", 0L));
        stats.put("overdue", (long) getOverdueWorkOrders().size());

        return stats;
    }

    @Override
    public List<Map<String, Object>> countByStatus(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.countByStatus(startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> countByType(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.countByType(startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> countByPriority(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.countByPriority(startTime, endTime);
    }

    @Override
    public List<WorkOrderFlow> getWorkOrderFlows(String workOrderId) {
        return workOrderFlowMapper.findByWorkOrderId(workOrderId);
    }

    @Override
    public int checkWorkOrderOverdue() {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WorkOrder::getStatus, "PENDING", "ASSIGNED", "IN_PROGRESS", "WAITING");
        wrapper.isNotNull(WorkOrder::getExpectedEndTime);

        List<WorkOrder> workOrders = list(wrapper);
        int overdueCount = 0;

        for (WorkOrder workOrder : workOrders) {
            if (workOrder.getExpectedEndTime() != null &&
                LocalDateTime.now().isAfter(workOrder.getExpectedEndTime())) {

                workOrder.setIsOverdue(true);
                long minutes = Duration.between(workOrder.getExpectedEndTime(), LocalDateTime.now()).toMinutes();
                workOrder.setOverdueMinutes((int) minutes);
                workOrder.setUpdatedAt(LocalDateTime.now());
                updateById(workOrder);
                overdueCount++;
            }
        }

        return overdueCount;
    }

    /**
     * 记录工单流转
     */
    private void recordFlow(String workOrderId, String orderCode, String actionType,
                            String fromStatus, String toStatus, String operatorId,
                            String operatorName, String content, String comment) {
        WorkOrderFlow flow = new WorkOrderFlow();
        flow.setId(UUID.randomUUID().toString().replace("-", ""));
        flow.setWorkOrderId(workOrderId);
        flow.setOrderCode(orderCode);
        flow.setActionType(actionType);
        flow.setFromStatus(fromStatus);
        flow.setToStatus(toStatus);
        flow.setOperatorId(operatorId);
        flow.setOperatorName(operatorName);
        flow.setContent(content);
        flow.setComment(comment);
        flow.setOperatedAt(LocalDateTime.now());

        workOrderFlowMapper.insert(flow);
    }
}
