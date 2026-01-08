package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.InspectionTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 巡检模板Mapper接口
 */
@Mapper
public interface InspectionTemplateMapper extends BaseMapper<InspectionTemplate> {
}
