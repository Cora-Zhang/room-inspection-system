package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.SSOConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * SSO配置Mapper
 */
@Mapper
public interface SSOConfigMapper extends BaseMapper<SSOConfig> {
}
