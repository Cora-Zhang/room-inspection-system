package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.InspectionVerification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 巡检核验Mapper
 */
@Mapper
public interface InspectionVerificationMapper extends BaseMapper<InspectionVerification> {
}
