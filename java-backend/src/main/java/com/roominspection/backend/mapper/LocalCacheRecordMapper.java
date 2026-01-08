package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.LocalCacheRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地缓存记录Mapper
 */
@Mapper
public interface LocalCacheRecordMapper extends BaseMapper<LocalCacheRecord> {

    /**
     * 查询待同步的缓存记录
     */
    List<LocalCacheRecord> selectPendingSync(@Param("limit") Integer limit);

    /**
     * 查询同步失败的记录
     */
    List<LocalCacheRecord> selectFailedSync(@Param("maxFailCount") Integer maxFailCount);

    /**
     * 清理过期的缓存记录
     */
    int deleteExpiredRecords(@Param("expireTime") LocalDateTime expireTime);
}
