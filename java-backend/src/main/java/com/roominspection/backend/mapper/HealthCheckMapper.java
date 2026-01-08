package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.HealthCheck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 健康检查Mapper
 */
@Mapper
public interface HealthCheckMapper extends BaseMapper<HealthCheck> {

    /**
     * 查询指定时间范围内的健康检查记录
     */
    List<HealthCheck> selectByTimeRange(@Param("serviceName") String serviceName,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 查询服务健康状态统计
     */
    List<Map<String, Object>> selectHealthStats(@Param("serviceName") String serviceName,
                                                 @Param("hours") Integer hours);
}
