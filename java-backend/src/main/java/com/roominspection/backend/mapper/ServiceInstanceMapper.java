package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.ServiceInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务实例Mapper
 */
@Mapper
public interface ServiceInstanceMapper extends BaseMapper<ServiceInstance> {

    /**
     * 查询所有健康的服务实例
     */
    List<ServiceInstance> selectHealthyInstances(@Param("serviceName") String serviceName);

    /**
     * 更新服务状态
     */
    int updateStatus(@Param("instanceId") String instanceId,
                     @Param("status") String status,
                     @Param("healthStatus") String healthStatus);
}
