package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.MonitorTask;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 监控任务服务接口
 */
public interface MonitorTaskService extends IService<MonitorTask> {

    /**
     * 创建监控任务
     *
     * @param task 任务对象
     * @return 是否成功
     */
    boolean createTask(MonitorTask task);

    /**
     * 更新任务状态
     *
     * @param taskId     任务ID
     * @param status     状态
     * @param dataCount  数据条数
     * @param errorMessage 错误信息
     * @return 是否成功
     */
    boolean updateTaskStatus(String taskId, String status, Integer dataCount, String errorMessage);

    /**
     * 标记任务开始
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean markTaskStart(String taskId);

    /**
     * 标记任务完成
     *
     * @param taskId    任务ID
     * @param dataCount 数据条数
     * @param success   是否成功
     * @return 是否成功
     */
    boolean markTaskComplete(String taskId, Integer dataCount, boolean success);

    /**
     * 获取设备最新任务
     *
     * @param deviceId 设备ID
     * @return 任务对象
     */
    MonitorTask getLatestTaskByDeviceId(String deviceId);

    /**
     * 获取任务统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计信息
     */
    Map<String, Object> getTaskStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取耗时统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 耗时统计
     */
    Map<String, Object> getDurationStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 清理历史任务
     *
     * @param days 保留天数
     * @return 删除数量
     */
    int cleanHistoryTasks(int days);
}
