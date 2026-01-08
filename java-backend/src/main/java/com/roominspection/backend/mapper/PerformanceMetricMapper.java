package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.PerformanceMetric;
import org.apache.ibatis.annotations.Mapper;

/**
 * 性能指标Mapper接口
 */
@Mapper
public interface PerformanceMetricMapper extends BaseMapper<PerformanceMetric> {
}
