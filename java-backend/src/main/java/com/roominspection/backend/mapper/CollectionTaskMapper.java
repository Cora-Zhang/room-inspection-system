package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.CollectionTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采集任务Mapper接口
 */
@Mapper
public interface CollectionTaskMapper extends BaseMapper<CollectionTask> {
}
