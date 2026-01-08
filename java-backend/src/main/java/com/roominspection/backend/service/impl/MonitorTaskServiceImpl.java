package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.MonitorTask;
import com.roominspection.backend.mapper.MonitorTaskMapper;
import com.roominspection.backend.service.MonitorTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 监控任务服务实现
 */
@Slf4j
@Service
public class MonitorTaskServiceImpl extends ServiceImpl<MonitorTaskMapper, MonitorTask>
        implements MonitorTaskService {

    @Autowired
    private MonitorTaskMapper monitorTaskMapper;

    /**
     * 创建监控任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createTask(MonitorTask task) {
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return monitorTaskMapper.insert(task) > 0;
    }

    /**
     * 更新任务状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTaskStatus(String taskId, String status, Integer dataCount, String errorMessage) {
        QueryWrapper<MonitorTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        MonitorTask task = monitorTaskMapper.selectOne(queryWrapper);

        if (task == null) {
            log.warn("任务不存在: taskId={}", taskId);
            return false;
        }

        task.setTaskStatus(status);
        task.setUpdatedAt(LocalDateTime.now());

        if (status.equals("RUNNING") && task.getStartTime() == null) {
            task.setStartTime(LocalDateTime.now());
        }

        if (status.equals("SUCCESS") || status.equals("FAILED") || status.equals("TIMEOUT")) {
            task.setEndTime(LocalDateTime.now());
            if (task.getStartTime() != null) {
                task.setDuration(java.time.Duration.between(task.getStartTime(), task.getEndTime()).toMillis());
            }
        }

        if (dataCount != null) {
            task.setDataCount(dataCount);
        }

        if (errorMessage != null) {
            task.setErrorMessage(errorMessage);
        }

        return monitorTaskMapper.updateById(task) > 0;
    }

    /**
     * 标记任务开始
     */
    @Override
    public boolean markTaskStart(String taskId) {
        return updateTaskStatus(taskId, "RUNNING", null, null);
    }

    /**
     * 标记任务完成
     */
    @Override
    public boolean markTaskComplete(String taskId, Integer dataCount, boolean success) {
        String status = success ? "SUCCESS" : "FAILED";
        return updateTaskStatus(taskId, status, dataCount, null);
    }

    /**
     * 获取设备最新任务
     */
    @Override
    public MonitorTask getLatestTaskByDeviceId(String deviceId) {
        return monitorTaskMapper.selectLatestByDeviceId(deviceId);
    }

    /**
     * 获取任务统计
     */
    @Override
    public Map<String, Object> getTaskStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<Map<String, Object>> statusList = monitorTaskMapper.statisticsByStatus(startTime, endTime);

        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("PENDING", 0L);
        byStatus.put("RUNNING", 0L);
        byStatus.put("SUCCESS", 0L);
        byStatus.put("FAILED", 0L);
        byStatus.put("TIMEOUT", 0L);

        long total = 0;
        for (Map<String, Object> item : statusList) {
            String status = (String) item.get("task_status");
            Long count = ((Number) item.get("count")).longValue();
            byStatus.put(status, count);
            total += count;
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", total);
        statistics.put("byStatus", byStatus);

        // 计算成功率
        long successCount = byStatus.get("SUCCESS");
        double successRate = total > 0 ? (double) successCount / total * 100 : 0;
        statistics.put("successRate", String.format("%.2f%%", successRate));

        return statistics;
    }

    /**
     * 获取耗时统计
     */
    @Override
    public Map<String, Object> getDurationStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> durationStats = monitorTaskMapper.statisticsDuration(startTime, endTime);
        return durationStats;
    }

    /**
     * 清理历史任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanHistoryTasks(int days) {
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(days);
        int count = monitorTaskMapper.deleteBeforeTime(beforeTime);
        log.info("清理历史任务记录: days={}, count={}", days, count);
        return count;
    }
}
