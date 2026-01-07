package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.DoorAccessLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门禁日志服务接口
 * 支持对接海康、大华等门禁系统API
 */
public interface DoorAccessLogService extends IService<DoorAccessLog> {

    /**
     * 分页查询门禁日志
     */
    Page<DoorAccessLog> queryPage(Long roomId, Long staffId, String direction,
                                    LocalDateTime startTime, LocalDateTime endTime,
                                    int pageNum, int pageSize);

    /**
     * 从海康门禁系统同步门禁日志
     */
    int syncFromHikvision(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 从大华门禁系统同步门禁日志
     */
    int syncFromDahua(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 核验巡检任务开始前人员是否已进入机房
     */
    DoorAccessLog verifyEntryBeforeInspection(Long inspectionTaskId, Long staffId,
                                               Long roomId, LocalDateTime plannedStartTime);

    /**
     * 核验巡检任务完成后人员是否已离开机房
     */
    DoorAccessLog verifyExitAfterInspection(Long inspectionTaskId, Long staffId,
                                             Long roomId, LocalDateTime plannedEndTime);

    /**
     * 计算人员在机房内的停留时长
     */
    Map<String, Object> calculateStayDuration(Long staffId, Long roomId,
                                                 LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 分析巡检人员进出记录
     */
    Map<String, Object> analyzeInspectionAccess(Long inspectionTaskId, Long staffId,
                                                   Long roomId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取机房访问统计
     */
    List<Map<String, Object>> getRoomAccessStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 生成巡检核验报告（门禁部分）
     */
    String generateAccessReport(Long inspectionTaskId);

    /**
     * 手动录入门禁记录
     */
    boolean manualAdd(DoorAccessLog doorAccessLog);
}
