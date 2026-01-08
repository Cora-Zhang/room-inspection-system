package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roominspection.backend.entity.LocalCacheRecord;
import com.roominspection.backend.mapper.LocalCacheRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存服务（监控服务故障时的本地缓存与断点续传）
 */
@Slf4j
@Service
public class LocalCacheService extends ServiceImpl<LocalCacheRecordMapper, LocalCacheRecord> {

    @Autowired
    private LocalCacheRecordMapper localCacheRecordMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "local_cache:";
    private static final int MAX_RETRY_COUNT = 5;

    /**
     * 缓存监控数据（监控服务故障时）
     */
    public void cacheMonitorData(String cacheType, String dataSourceType,
                                   Long dataSourceId, Object data) {
        try {
            // 1. 保存到数据库
            LocalCacheRecord record = new LocalCacheRecord();
            record.setCacheType(cacheType);
            record.setDataSourceType(dataSourceType);
            record.setDataSourceId(dataSourceId);
            record.setDataContent(objectMapper.writeValueAsString(data));
            record.setDataTimestamp(System.currentTimeMillis());
            record.setStatus("PENDING");
            record.setSyncFailCount(0);
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());

            localCacheRecordMapper.insert(record);

            // 2. 保存到Redis（临时缓存）
            String redisKey = CACHE_PREFIX + cacheType + ":" + dataSourceType + ":" + dataSourceId;
            redisTemplate.opsForValue().set(redisKey, data, 7, TimeUnit.DAYS);

            log.debug("缓存监控数据成功: type={}, source={}, id={}",
                    cacheType, dataSourceType, dataSourceId);
        } catch (Exception e) {
            log.error("缓存监控数据失败", e);
        }
    }

    /**
     * 同步缓存数据到Redis（服务恢复后）
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void syncCachedData() {
        try {
            // 1. 查询待同步的记录
            List<LocalCacheRecord> pendingRecords = localCacheRecordMapper.selectPendingSync(100);

            if (pendingRecords.isEmpty()) {
                return;
            }

            log.info("开始同步缓存数据，待同步记录数: {}", pendingRecords.size());

            // 2. 逐条同步到Redis
            for (LocalCacheRecord record : pendingRecords) {
                try {
                    syncToRedis(record);
                } catch (Exception e) {
                    log.error("同步单条缓存数据失败: id={}", record.getId(), e);
                    // 更新失败次数
                    record.setSyncFailCount(record.getSyncFailCount() + 1);
                    record.setUpdateTime(LocalDateTime.now());
                    localCacheRecordMapper.updateById(record);

                    // 如果失败次数超过阈值，标记为同步失败
                    if (record.getSyncFailCount() >= MAX_RETRY_COUNT) {
                        record.setStatus("FAILED");
                        localCacheRecordMapper.updateById(record);
                    }
                }
            }

            log.info("缓存数据同步完成");
        } catch (Exception e) {
            log.error("同步缓存数据失败", e);
        }
    }

    /**
     * 同步单条记录到Redis
     */
    private void syncToRedis(LocalCacheRecord record) throws Exception {
        // 解析数据内容
        Object data = objectMapper.readValue(record.getDataContent(), Object.class);

        // 生成Redis Key
        String redisKey = CACHE_PREFIX + record.getCacheType() + ":"
                + record.getDataSourceType() + ":" + record.getDataSourceId();

        // 保存到Redis
        redisTemplate.opsForValue().set(redisKey, data, 7, TimeUnit.DAYS);

        // 更新记录状态
        record.setStatus("SYNCED");
        record.setLastSyncTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        localCacheRecordMapper.updateById(record);

        log.debug("同步缓存数据成功: id={}, key={}", record.getId(), redisKey);
    }

    /**
     * 从本地缓存获取数据
     */
    public Object getFromCache(String cacheType, String dataSourceType, Long dataSourceId) {
        try {
            // 1. 先从Redis获取
            String redisKey = CACHE_PREFIX + cacheType + ":" + dataSourceType + ":" + dataSourceId;
            Object data = redisTemplate.opsForValue().get(redisKey);

            if (data != null) {
                return data;
            }

            // 2. 如果Redis中没有，从数据库获取
            LocalCacheRecord record = localCacheRecordMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<LocalCacheRecord>()
                            .eq("cache_type", cacheType)
                            .eq("data_source_type", dataSourceType)
                            .eq("data_source_id", dataSourceId)
                            .orderByDesc("create_time")
                            .last("LIMIT 1")
            );

            if (record != null && "SYNCED".equals(record.getStatus())) {
                // 解析数据并缓存到Redis
                data = objectMapper.readValue(record.getDataContent(), Object.class);
                redisTemplate.opsForValue().set(redisKey, data, 7, TimeUnit.DAYS);
                return data;
            }

            return null;
        } catch (Exception e) {
            log.error("从缓存获取数据失败", e);
            return null;
        }
    }

    /**
     * 重试失败的同步记录
     */
    @Scheduled(cron = "0 */10 * * * ?") // 每10分钟执行一次
    public void retryFailedSync() {
        try {
            // 查询失败次数未超过阈值的记录
            List<LocalCacheRecord> failedRecords = localCacheRecordMapper.selectFailedSync(MAX_RETRY_COUNT);

            if (failedRecords.isEmpty()) {
                return;
            }

            log.info("开始重试失败的同步记录，数量: {}", failedRecords.size());

            for (LocalCacheRecord record : failedRecords) {
                try {
                    // 重置状态为PENDING，重新同步
                    record.setStatus("PENDING");
                    record.setSyncFailCount(0);
                    record.setUpdateTime(LocalDateTime.now());
                    localCacheRecordMapper.updateById(record);
                } catch (Exception e) {
                    log.error("重置同步记录失败: id={}", record.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("重试失败的同步记录失败", e);
        }
    }

    /**
     * 清理过期的缓存记录
     */
    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点执行
    public void cleanupExpiredRecords() {
        try {
            // 删除30天前的记录
            LocalDateTime expireTime = LocalDateTime.now().minusDays(30);
            int deletedCount = localCacheRecordMapper.deleteExpiredRecords(expireTime);

            log.info("清理过期缓存记录完成，删除数量: {}", deletedCount);
        } catch (Exception e) {
            log.error("清理过期缓存记录失败", e);
        }
    }

    /**
     * 获取待同步记录统计
     */
    public Map<String, Object> getCacheStats() {
        try {
            List<LocalCacheRecord> pendingRecords = localCacheRecordMapper.selectPendingSync(1000);
            List<LocalCacheRecord> failedRecords = localCacheRecordMapper.selectFailedSync(MAX_RETRY_COUNT);

            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("pendingCount", pendingRecords.size());
            stats.put("failedCount", failedRecords.size());

            return stats;
        } catch (Exception e) {
            log.error("获取缓存统计失败", e);
            return null;
        }
    }

    /**
     * 手动触发同步
     */
    public void triggerSync() {
        syncCachedData();
    }

    /**
     * 清除指定类型的数据缓存
     */
    public void clearCache(String cacheType) {
        try {
            // 清除Redis缓存
            String pattern = CACHE_PREFIX + cacheType + ":*";
            redisTemplate.delete(redisTemplate.keys(pattern));

            log.info("清除缓存成功: type={}", cacheType);
        } catch (Exception e) {
            log.error("清除缓存失败", e);
        }
    }
}
