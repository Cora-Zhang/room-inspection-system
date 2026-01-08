package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.DatabaseBackup;
import com.roominspection.backend.mapper.DatabaseBackupMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 数据库备份服务
 */
@Slf4j
@Service
public class DatabaseBackupService extends ServiceImpl<DatabaseBackupMapper, DatabaseBackup> {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${backup.path:/tmp/backups}")
    private String backupPath;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * 定时自动备份 - 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoBackup() {
        try {
            log.info("开始执行自动数据库备份");
            String backupName = performBackup("FULL", true);
            log.info("自动数据库备份成功: {}", backupName);
        } catch (Exception e) {
            log.error("自动数据库备份失败", e);
        }
    }

    /**
     * 执行数据库备份
     */
    public String performBackup(String backupType, boolean isAutoBackup) throws Exception {
        // 解析数据库连接信息
        String databaseName = extractDatabaseName(datasourceUrl);
        String host = extractHost(datasourceUrl);
        String port = extractPort(datasourceUrl);

        // 创建备份目录
        File backupDir = new File(backupPath);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        // 生成备份文件名
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String backupFileName = "room_inspection_" + backupType.toLowerCase() + "_" + timestamp + ".sql";
        String backupFilePath = backupDir.getAbsolutePath() + File.separator + backupFileName;

        // 创建备份记录
        DatabaseBackup backup = new DatabaseBackup();
        backup.setBackupName(backupFileName);
        backup.setBackupPath(backupFilePath);
        backup.setBackupType(backupType);
        backup.setStatus("IN_PROGRESS");
        backup.setStartTime(LocalDateTime.now());
        backup.setIsAutoBackup(isAutoBackup);
        backup.setCreateTime(LocalDateTime.now());
        this.save(backup);

        try {
            // 执行mysqldump命令
            String[] command = {
                "mysqldump",
                "-h" + host,
                "-P" + port,
                "-u" + datasourceUsername,
                "-p" + datasourcePassword,
                "--single-transaction",
                "--routines",
                "--triggers",
                "--events",
                databaseName
            };

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectOutput(new File(backupFilePath));

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                // 获取备份文件大小
                File backupFile = new File(backupFilePath);
                long fileSize = backupFile.length();

                // 更新备份记录
                backup.setStatus("SUCCESS");
                backup.setEndTime(LocalDateTime.now());
                backup.setDuration((backup.getEndTime().getSecond() - backup.getStartTime().getSecond()));
                backup.setFileSize(fileSize);
                this.updateById(backup);

                log.info("数据库备份成功: {}", backupFilePath);
                return backupFileName;
            } else {
                // 读取错误信息
                StringBuilder errorMsg = new StringBuilder();
                try (BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorMsg.append(line).append("\n");
                    }
                }

                // 更新备份记录
                backup.setStatus("FAILED");
                backup.setEndTime(LocalDateTime.now());
                backup.setDuration((backup.getEndTime().getSecond() - backup.getStartTime().getSecond()));
                backup.setErrorMessage(errorMsg.toString());
                this.updateById(backup);

                throw new Exception("mysqldump执行失败: " + errorMsg.toString());
            }
        } catch (Exception e) {
            // 更新备份记录
            backup.setStatus("FAILED");
            backup.setEndTime(LocalDateTime.now());
            backup.setDuration((backup.getEndTime().getSecond() - backup.getStartTime().getSecond()));
            backup.setErrorMessage(e.getMessage());
            this.updateById(backup);

            // 删除失败的备份文件
            File failedFile = new File(backupFilePath);
            if (failedFile.exists()) {
                failedFile.delete();
            }

            throw e;
        }
    }

    /**
     * 恢复数据库
     */
    public void restoreDatabase(String backupFileName, String description) throws Exception {
        // 查询备份记录
        DatabaseBackup backup = this.lambdaQuery()
                .eq(DatabaseBackup::getBackupName, backupFileName)
                .one();

        if (backup == null) {
            throw new Exception("备份文件不存在: " + backupFileName);
        }

        if (!"SUCCESS".equals(backup.getStatus())) {
            throw new Exception("备份文件状态无效: " + backup.getStatus());
        }

        File backupFile = new File(backup.getBackupPath());
        if (!backupFile.exists()) {
            throw new Exception("备份文件不存在: " + backup.getBackupPath());
        }

        // 解析数据库连接信息
        String databaseName = extractDatabaseName(datasourceUrl);
        String host = extractHost(datasourceUrl);
        String port = extractPort(datasourceUrl);

        try {
            // 执行mysql命令恢复数据
            String[] command = {
                "mysql",
                "-h" + host,
                "-P" + port,
                "-u" + datasourceUsername,
                "-p" + datasourcePassword,
                databaseName
            };

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectInput(backupFile);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("数据库恢复成功: {}", backupFileName);
            } else {
                // 读取错误信息
                StringBuilder errorMsg = new StringBuilder();
                try (BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorMsg.append(line).append("\n");
                    }
                }

                throw new Exception("数据库恢复失败: " + errorMsg.toString());
            }
        } catch (Exception e) {
            log.error("数据库恢复失败", e);
            throw e;
        }
    }

    /**
     * 删除备份文件
     */
    public void deleteBackup(Long backupId) throws Exception {
        DatabaseBackup backup = this.getById(backupId);
        if (backup == null) {
            throw new Exception("备份记录不存在");
        }

        // 删除备份文件
        File backupFile = new File(backup.getBackupPath());
        if (backupFile.exists()) {
            if (!backupFile.delete()) {
                throw new Exception("删除备份文件失败");
            }
        }

        // 删除备份记录
        this.removeById(backupId);

        log.info("删除备份成功: {}", backup.getBackupName());
    }

    /**
     * 获取最近的备份记录
     */
    public List<DatabaseBackup> getRecentBackups(Integer limit) {
        return baseMapper.selectRecentBackups(limit != null ? limit : 20);
    }

    /**
     * 从数据库URL中提取数据库名称
     */
    private String extractDatabaseName(String url) {
        // jdbc:mysql://localhost:3306/room_inspection?...
        int start = url.lastIndexOf("/");
        int end = url.indexOf("?");
        if (end == -1) {
            end = url.length();
        }
        return url.substring(start + 1, end);
    }

    /**
     * 从数据库URL中提取主机地址
     */
    private String extractHost(String url) {
        // jdbc:mysql://localhost:3306/...
        int start = url.indexOf("://") + 3;
        int end = url.indexOf(":");
        return url.substring(start, end);
    }

    /**
     * 从数据库URL中提取端口
     */
    private String extractPort(String url) {
        // jdbc:mysql://localhost:3306/...
        int start = url.indexOf(":") + 1;
        int end = url.indexOf("/", start);
        return url.substring(start, end);
    }
}
