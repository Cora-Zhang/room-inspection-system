package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.AlarmRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警记录Service
 */
public interface AlarmRecordService extends IService<AlarmRecord> {

    /**
     * 根据时间范围查询告警记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 告警记录列表
     */
    List<AlarmRecord> listByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据机房和时间范围查询告警记录
     *
     * @param roomId 机房ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 告警记录列表
     */
    List<AlarmRecord> listByRoomAndTimeRange(String roomId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据级别查询告警记录
     *
     * @param level 级别
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 告警记录列表
     */
    List<AlarmRecord> listByLevel(String level, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 创建告警记录
     *
     * @param alarmRecord 告警记录对象
     * @return 创建结果
     */
    boolean createAlarm(AlarmRecord alarmRecord);

    /**
     * 确认告警
     *
     * @param id 告警ID
     * @param acknowledgedBy 确认人ID
     * @param acknowledgedByName 确认人姓名
     * @return 确认结果
     */
    boolean acknowledgeAlarm(String id, String acknowledgedBy, String acknowledgedByName);

    /**
     * 解决告警
     *
     * @param id 告警ID
     * @param handledBy 处理人ID
     * @param handledByName 处理人姓名
     * @param handleResult 处理结果
     * @return 解决结果
     */
    boolean resolveAlarm(String id, String handledBy, String handledByName, String handleResult);

    /**
     * 获取未处理告警列表
     *
     * @return 未处理告警列表
     */
    List<AlarmRecord> getUnresolvedAlarms();

    /**
     * 获取紧急告警列表
     *
     * @return 紧急告警列表
     */
    List<AlarmRecord> getCriticalAlarms();

    /**
     * 获取告警统计信息
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    Map<String, Object> getStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按级别统计告警数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> countByLevel(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按类型统计告警数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> countByType(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按状态统计告警数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> countByStatus(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 发送告警通知
     *
     * @param alarmRecord 告警记录
     * @return 发送结果
     */
    boolean sendAlarmNotification(AlarmRecord alarmRecord);

    /**
     * 关联工单
     *
     * @param alarmId 告警ID
     * @param workOrderId 工单ID
     * @return 关联结果
     */
    boolean linkWorkOrder(String alarmId, String workOrderId);
}
