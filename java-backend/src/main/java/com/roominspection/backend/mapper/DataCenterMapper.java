package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.DataCenter;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据中心Mapper接口
 */
@Mapper
public interface DataCenterMapper extends BaseMapper<DataCenter> {
}
