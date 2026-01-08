package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.Organization;
import org.apache.ibatis.annotations.Mapper;

/**
 * 组织Mapper
 */
@Mapper
public interface OrganizationMapper extends BaseMapper<Organization> {
}
