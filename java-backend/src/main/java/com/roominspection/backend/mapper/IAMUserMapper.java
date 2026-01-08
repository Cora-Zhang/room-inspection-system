package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.IAMUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * IAM用户Mapper
 */
@Mapper
public interface IAMUserMapper extends BaseMapper<IAMUser> {
}
