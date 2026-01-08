package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.PerformanceMetric;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 性能监控服务接口
 */
public interface PerformanceMonitorService extends IService<PerformanceMetric> {

    /**
     * 记录性能指标
     *
     * @param metricName   指标名称
     * @param metricType   指标类型
     * @param metricValue  指标值
     * @param unit         单位
     * @param objectId     对象ID
     * @param objectType   对象类型
     */
    void recordMetric(String metricName, String metricType, Double metricValue,
                      String unit, String objectId, String objectType);

    /**
     * 记录API响应时间
     *
     * @param apiPath    API路径
     * @param duration   响应时间（毫秒）
     * @param extraInfo  额外信息
     */
    void recordApiResponse(String apiPath, Long duration, String extraInfo);

    /**
     * 记录数据库查询时间
     *
     * @param sqlStatement SQL语句
     * @param duration     查询时间（毫秒）
     * @param extraInfo    额外信息
     */
    void recordDbQuery(String sqlStatement, Long duration, String extraInfo);

    /**
     * 记录监控数据采集时间
     *
     * @param deviceId   设备ID
     * @param duration   采集时间（毫秒）
     * @param dataCount  数据条数
     */
    void recordMonitorCollect(String deviceId, Long duration, Integer dataCount);

    /**
     * 记录工单处理时间
     *
     * @param workOrderId 工单ID
     * @param action      操作类型
     * @param duration    处理时间（毫秒）
     */
    void recordWorkOrderProcess(String workOrderId, String action, Long duration);

    /**
     * 记录缓存命中率
     *
     * @param cacheName  缓存名称
     * @param hitRate    命中率（百分比）
     */
    void recordCacheHit(String cacheName, Double hitRate);

    /**
     * 获取性能统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计信息
     */
    Map<String, Object> getPerformanceStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取指定类型的指标
     *
     * @param metricType 指标类型
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 指标列表
     */
    List<PerformanceMetric> getMetricsByType(String metricType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取慢查询列表
     *
     * @param threshold 阈值（毫秒）
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 慢查询列表
     */
    List<Map<String, Object>> getSlowQueries(Long threshold, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 清理历史性能数据
     *
     * @param days 保留天数
     * @return 删除数量
     */
    int cleanHistoryMetrics(int days);
}
