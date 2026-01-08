package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.WorkOrder;
import com.roominspection.backend.entity.WorkOrderFlow;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 工单Service
 */
public interface WorkOrderService extends IService<WorkOrder> {

    /**
     * 根据时间范围查询工单列表
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 工单列表
     */
    List<WorkOrder> listByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据状态查询工单列表
     *
     * @param status 状态
     * @return 工单列表
     */
    List<WorkOrder> listByStatus(String status);

    /**
     * 根据类型查询工单列表
     *
     * @param type 类型
     * @return 工单列表
     */
    List<WorkOrder> listByType(String type);

    /**
     * 根据负责人查询工单列表
     *
     * @param ownerId 负责人ID
     * @return 工单列表
     */
    List<WorkOrder> listByOwnerId(String ownerId);

    /**
     * 创建工单
     *
     * @param workOrder 工单对象
     * @return 创建结果
     */
    boolean createWorkOrder(WorkOrder workOrder);

    /**
     * 自动创建工单（基于告警）
     *
     * @param alarmId 告警ID
     * @return 创建结果
     */
    WorkOrder autoCreateFromAlarm(String alarmId);

    /**
     * 指派工单
     *
     * @param id 工单ID
     * @param assignedTo 指派人ID
     * @param assignedToName 指派人姓名
     * @param priority 优先级
     * @return 指派结果
     */
    boolean assignWorkOrder(String id, String assignedTo, String assignedToName, String priority);

    /**
     * 开始处理工单
     *
     * @param id 工单ID
     * @param ownerId 负责人ID
     * @param ownerName 负责人姓名
     * @return 开始结果
     */
    boolean startWorkOrder(String id, String ownerId, String ownerName);

    /**
     * 完成工单
     *
     * @param id 工单ID
     * @param handleResult 处理结果
     * @param handleDescription 处理说明
     * @param duration 工作时长
     * @return 完成结果
     */
    boolean completeWorkOrder(String id, String handleResult, String handleDescription, Double duration);

    /**
     * 取消工单
     *
     * @param id 工单ID
     * @param cancelReason 取消原因
     * @return 取消结果
     */
    boolean cancelWorkOrder(String id, String cancelReason);

    /**
     * 关闭工单
     *
     * @param id 工单ID
     * @param closeReason 关闭原因
     * @param closedBy 关闭人ID
     * @param closedByName 关闭人姓名
     * @return 关闭结果
     */
    boolean closeWorkOrder(String id, String closeReason, String closedBy, String closedByName);

    /**
     * 获取超时工单列表
     *
     * @return 超时工单列表
     */
    List<WorkOrder> getOverdueWorkOrders();

    /**
     * 获取待处理工单列表
     *
     * @return 待处理工单列表
     */
    List<WorkOrder> getPendingWorkOrders();

    /**
     * 获取紧急工单列表
     *
     * @return 紧急工单列表
     */
    List<WorkOrder> getUrgentWorkOrders();

    /**
     * 获取工单统计信息
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    Map<String, Object> getStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按状态统计工单数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> countByStatus(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按类型统计工单数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> countByType(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按优先级统计工单数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> countByPriority(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取工单流转记录
     *
     * @param workOrderId 工单ID
     * @return 流转记录列表
     */
    List<WorkOrderFlow> getWorkOrderFlows(String workOrderId);

    /**
     * 检查工单超时
     *
     * @return 检查结果（超时工单数量）
     */
    int checkWorkOrderOverdue();
}
