package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.PowerTrend;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 电力趋势服务接口
 * 支持趋势分析和容量预警
 */
public interface PowerTrendService extends IService<PowerTrend> {

    /**
     * 分析设备趋势数据
     */
    PowerTrend analyzeTrend(Long powerSystemId, LocalDate statisticDate, String period);

    /**
     * 批量分析所有设备趋势
     */
    Map<String, Object> batchAnalyzeAll(LocalDate statisticDate);

    /**
     * 生成容量预警分析报告
     */
    String generateCapacityAlertReport(Long powerSystemId);

    /**
     * 查询设备趋势数据
     */
    Page<PowerTrend> queryTrends(Long powerSystemId, LocalDate startDate, LocalDate endDate,
                                  String period, int pageNum, int pageSize);

    /**
     * 获取需要容量预警的设备列表
     */
    List<PowerTrend> getDevicesNeedCapacityAlert();

    /**
     * 生成扩容建议
     */
    Map<String, Object> generateExpansionSuggestion(Long powerSystemId);
}
