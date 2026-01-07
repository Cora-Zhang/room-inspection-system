package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.DoorAccessPermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门禁权限Mapper
 */
@Mapper
public interface DoorAccessPermissionMapper extends BaseMapper<DoorAccessPermission> {
}
