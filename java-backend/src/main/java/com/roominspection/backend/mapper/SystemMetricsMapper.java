package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.SystemMetrics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统监控指标Mapper
 */
@Mapper
public interface SystemMetricsMapper extends BaseMapper<SystemMetrics> {

    /**
     * 查询最近的小时指标
     */
    List<SystemMetrics> selectRecentMetrics(@Param("serviceName") String serviceName,
                                            @Param("hours") Integer hours);

    /**
     * 删除过期的指标数据
     */
    int deleteExpiredMetrics(@Param("expireTime") LocalDateTime expireTime);
}
