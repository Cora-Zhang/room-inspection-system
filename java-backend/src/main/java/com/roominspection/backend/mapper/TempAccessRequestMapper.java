package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.TempAccessRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 临时门禁权限申请Mapper
 */
@Mapper
public interface TempAccessRequestMapper extends BaseMapper<TempAccessRequest> {
}
