package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.Job;
import org.apache.ibatis.annotations.Mapper;

/**
 * 职位Mapper
 */
@Mapper
public interface JobMapper extends BaseMapper<Job> {
}
