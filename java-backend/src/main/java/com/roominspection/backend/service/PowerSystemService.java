package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.PowerMetric;
import com.roominspection.backend.entity.PowerSystem;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 电力系统监控服务接口
 * 支持SNMP/Modbus协议实时采集电力设备参数
 */
public interface PowerSystemService extends IService<PowerSystem> {

    /**
     * 分页查询电力设备
     */
    Page<PowerSystem> queryPage(String deviceType, Long roomId, String status,
                                   int pageNum, int pageSize);

    /**
     * 通过SNMP采集电力设备数据
     */
    PowerMetric collectBySnmp(Long powerSystemId);

    /**
     * 通过Modbus采集电力设备数据
     */
    PowerMetric collectByModbus(Long powerSystemId);

    /**
     * 批量采集所有设备数据
     */
    Map<String, Object> batchCollectAll();

    /**
     * 获取设备最新指标
     */
    PowerMetric getLatestMetric(Long powerSystemId);

    /**
     * 查询设备历史指标
     */
    Page<PowerMetric> queryMetrics(Long powerSystemId, java.time.LocalDateTime startTime,
                                    java.time.LocalDateTime endTime, int pageNum, int pageSize);

    /**
     * 获取设备统计信息
     */
    Map<String, Object> getDeviceStatistics();
}
