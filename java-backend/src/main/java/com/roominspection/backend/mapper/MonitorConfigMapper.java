package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.MonitorConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 监控配置Mapper接口
 */
@Mapper
public interface MonitorConfigMapper extends BaseMapper<MonitorConfig> {

    /**
     * 根据设备类型查询启用的监控配置
     *
     * @param deviceType 设备类型
     * @return 监控配置列表
     */
    @Select("SELECT * FROM monitor_config WHERE device_type = #{deviceType} AND status = 1")
    List<MonitorConfig> selectByDeviceType(@Param("deviceType") String deviceType);

    /**
     * 查询所有启用的监控配置
     *
     * @return 监控配置列表
     */
    @Select("SELECT * FROM monitor_config WHERE status = 1")
    List<MonitorConfig> selectAllEnabled();
}
