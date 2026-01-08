package com.roominspection.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.AuditLog;
import com.roominspection.backend.mapper.AuditLogMapper;
import com.roominspection.backend.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务
 */
@Service
public class AuditLogService extends ServiceImpl<AuditLogMapper, AuditLog> {

    @Autowired
    private EncryptionUtil encryptionUtil;

    /**
     * 分页查询操作日志
     *
     * @param page         页码
     * @param pageSize     每页大小
     * @param userId       用户ID
     * @param username     用户名
     * @param operationType 操作类型
     * @param module       操作模块
     * @param status       操作状态
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return 操作日志列表
     */
    public IPage<AuditLog> queryAuditLogs(int page, int pageSize,
                                          String userId, String username,
                                          String operationType, String module,
                                          String status,
                                          LocalDateTime startTime, LocalDateTime endTime) {
        Page<AuditLog> pageParam = new Page<>(page, pageSize);

        LambdaQueryWrapper<AuditLog> queryWrapper = new LambdaQueryWrapper<>();

        if (userId != null && !userId.isEmpty()) {
            queryWrapper.eq(AuditLog::getUserId, userId);
        }
        if (username != null && !username.isEmpty()) {
            queryWrapper.like(AuditLog::getUsername, username);
        }
        if (operationType != null && !operationType.isEmpty()) {
            queryWrapper.eq(AuditLog::getOperationType, operationType);
        }
        if (module != null && !module.isEmpty()) {
            queryWrapper.eq(AuditLog::getModule, module);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(AuditLog::getStatus, status);
        }
        if (startTime != null) {
            queryWrapper.ge(AuditLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(AuditLog::getCreateTime, endTime);
        }

        // 按时间倒序
        queryWrapper.orderByDesc(AuditLog::getCreateTime);

        IPage<AuditLog> result = page(pageParam, queryWrapper);

        // 解密参数和结果
        result.getRecords().forEach(this::decryptSensitiveData);

        return result;
    }

    /**
     * 查询用户操作日志
     */
    public List<AuditLog> queryUserAuditLogs(String userId, int limit) {
        LambdaQueryWrapper<AuditLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AuditLog::getUserId, userId);
        queryWrapper.orderByDesc(AuditLog::getCreateTime);
        queryWrapper.last("LIMIT " + limit);

        List<AuditLog> logs = list(queryWrapper);

        // 解密敏感数据
        logs.forEach(this::decryptSensitiveData);

        return logs;
    }

    /**
     * 统计用户操作次数
     */
    public long countUserOperations(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AuditLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AuditLog::getUserId, userId);
        if (startTime != null) {
            queryWrapper.ge(AuditLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(AuditLog::getCreateTime, endTime);
        }
        return count(queryWrapper);
    }

    /**
     * 统计操作失败次数
     */
    public long countFailedOperations(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AuditLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AuditLog::getStatus, "FAILED");
        if (startTime != null) {
            queryWrapper.ge(AuditLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(AuditLog::getCreateTime, endTime);
        }
        return count(queryWrapper);
    }

    /**
     * 获取平均执行时间
     */
    public Double getAverageExecutionTime(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AuditLog> queryWrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            queryWrapper.ge(AuditLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(AuditLog::getCreateTime, endTime);
        }

        List<AuditLog> logs = list(queryWrapper);
        if (logs.isEmpty()) {
            return 0.0;
        }

        return logs.stream()
                .mapToLong(AuditLog::getExecutionTime)
                .average()
                .orElse(0.0);
    }

    /**
     * 解密敏感数据
     */
    private void decryptSensitiveData(AuditLog auditLog) {
        try {
            if (auditLog.getParams() != null && !auditLog.getParams().isEmpty()) {
                auditLog.setParams(encryptionUtil.decryptJson(auditLog.getParams()));
            }
            if (auditLog.getResult() != null && !auditLog.getResult().isEmpty()) {
                auditLog.setResult(encryptionUtil.decryptJson(auditLog.getResult()));
            }
        } catch (Exception e) {
            // 解密失败，保持原样
        }
    }

    /**
     * 清理旧日志（保留指定天数）
     */
    public int cleanOldLogs(int keepDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(keepDays);
        return baseMapper.deleteByIds(
                baseMapper.selectIds(new LambdaQueryWrapper<AuditLog>()
                        .lt(AuditLog::getCreateTime, cutoffDate))
        );
    }
}
