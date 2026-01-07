package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.PowerMetric;

/**
 * 电力指标服务接口
 */
public interface PowerMetricService extends IService<PowerMetric> {

    /**
     * 保存采集的指标数据
     */
    boolean saveMetric(PowerMetric metric);

    /**
     * 查询设备最新指标
     */
    PowerMetric getLatestByDeviceId(Long powerSystemId);

    /**
     * 检查指标是否超出阈值
     */
    boolean checkThreshold(PowerMetric metric);
}
