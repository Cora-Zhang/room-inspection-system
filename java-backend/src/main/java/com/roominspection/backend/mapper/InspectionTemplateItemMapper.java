package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.InspectionTemplateItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 巡检模板项Mapper接口
 */
@Mapper
public interface InspectionTemplateItemMapper extends BaseMapper<InspectionTemplateItem> {
}
