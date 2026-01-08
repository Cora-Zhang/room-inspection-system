package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.DatabaseBackup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据库备份Mapper
 */
@Mapper
public interface DatabaseBackupMapper extends BaseMapper<DatabaseBackup> {

    /**
     * 查询最近的备份记录
     */
    List<DatabaseBackup> selectRecentBackups(@Param("limit") Integer limit);

    /**
     * 查询指定时间范围内的备份记录
     */
    List<DatabaseBackup> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);
}
