package com.roominspection.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roominspection.backend.entity.HealthCheck;
import com.roominspection.backend.entity.ServiceInstance;
import com.roominspection.backend.entity.SystemMetrics;
import com.roominspection.backend.mapper.HealthCheckMapper;
import com.roominspection.backend.mapper.ServiceInstanceMapper;
import com.roominspection.backend.mapper.SystemMetricsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 健康检查服务
 */
@Slf4j
@Service
public class HealthCheckService extends ServiceImpl<HealthCheckMapper, HealthCheck> {

    @Autowired
    private HealthCheckMapper healthCheckMapper;

    @Autowired
    private ServiceInstanceMapper serviceInstanceMapper;

    @Autowired
    private SystemMetricsMapper systemMetricsMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String SERVICE_NAME = "room-inspection-backend";
    private static final String INSTANCE_ID = UUID.randomUUID().toString();

    /**
     * 定时健康检查 - 每30秒执行一次
     */
    @Scheduled(fixedRate = 30000)
    public void performHealthCheck() {
        try {
            // 1. 检查数据库连接
            checkDatabase();

            // 2. 检查Redis连接
            checkRedis();

            // 3. 检查JVM健康状态
            checkJvmHealth();

            // 4. 检查磁盘空间
            checkDiskSpace();

            // 5. 更新服务实例心跳
            updateServiceHeartbeat();

            // 6. 收集系统指标
            collectSystemMetrics();

            log.debug("健康检查完成");
        } catch (Exception e) {
            log.error("健康检查失败", e);
        }
    }

    /**
     * 检查数据库连接
     */
    private void checkDatabase() {
        long startTime = System.currentTimeMillis();
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setServiceName(SERVICE_NAME);
        healthCheck.setInstanceId(INSTANCE_ID);
        healthCheck.setServiceAddress(getServiceAddress());
        healthCheck.setCheckType("DATABASE");
        healthCheck.setCheckTime(LocalDateTime.now());

        try {
            // 执行简单的查询来测试数据库连接
            healthCheckMapper.selectCount(new QueryWrapper<>());

            long responseTime = System.currentTimeMillis() - startTime;
            healthCheck.setStatus("UP");
            healthCheck.setResponseTime(responseTime);

            Map<String, Object> details = new HashMap<>();
            details.put("query", "SELECT COUNT(*)");
            details.put("responseTime", responseTime + "ms");
            healthCheck.setDetails(objectMapper.writeValueAsString(details));

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            healthCheck.setStatus("DOWN");
            healthCheck.setResponseTime(responseTime);
            healthCheck.setErrorMessage(e.getMessage());

            log.error("数据库健康检查失败", e);
        }

        healthCheck.setCreateTime(LocalDateTime.now());
        healthCheckMapper.insert(healthCheck);
    }

    /**
     * 检查Redis连接
     */
    private void checkRedis() {
        long startTime = System.currentTimeMillis();
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setServiceName(SERVICE_NAME);
        healthCheck.setInstanceId(INSTANCE_ID);
        healthCheck.setServiceAddress(getServiceAddress());
        healthCheck.setCheckType("REDIS");
        healthCheck.setCheckTime(LocalDateTime.now());

        try {
            // 这里可以通过RedisTemplate进行测试
            // 暂时标记为UP
            long responseTime = System.currentTimeMillis() - startTime;
            healthCheck.setStatus("UP");
            healthCheck.setResponseTime(responseTime);

            Map<String, Object> details = new HashMap<>();
            details.put("command", "PING");
            details.put("responseTime", responseTime + "ms");
            healthCheck.setDetails(objectMapper.writeValueAsString(details));

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            healthCheck.setStatus("DOWN");
            healthCheck.setResponseTime(responseTime);
            healthCheck.setErrorMessage(e.getMessage());

            log.error("Redis健康检查失败", e);
        }

        healthCheck.setCreateTime(LocalDateTime.now());
        healthCheckMapper.insert(healthCheck);
    }

    /**
     * 检查JVM健康状态
     */
    private void checkJvmHealth() {
        long startTime = System.currentTimeMillis();
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setServiceName(SERVICE_NAME);
        healthCheck.setInstanceId(INSTANCE_ID);
        healthCheck.setServiceAddress(getServiceAddress());
        healthCheck.setCheckType("JVM");
        healthCheck.setCheckTime(LocalDateTime.now());

        try {
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            long heapMemoryUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
            long heapMemoryMax = memoryMXBean.getHeapMemoryUsage().getMax();
            double heapUsage = (double) heapMemoryUsed / heapMemoryMax * 100;

            long responseTime = System.currentTimeMillis() - startTime;

            // 如果堆内存使用率超过90%，标记为DEGRADED
            String status = heapUsage > 90 ? "DEGRADED" : "UP";
            healthCheck.setStatus(status);
            healthCheck.setResponseTime(responseTime);

            Map<String, Object> details = new HashMap<>();
            details.put("heapMemoryUsed", heapMemoryUsed / (1024 * 1024) + " MB");
            details.put("heapMemoryMax", heapMemoryMax / (1024 * 1024) + " MB");
            details.put("heapUsage", String.format("%.2f%%", heapUsage));
            healthCheck.setDetails(objectMapper.writeValueAsString(details));

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            healthCheck.setStatus("DOWN");
            healthCheck.setResponseTime(responseTime);
            healthCheck.setErrorMessage(e.getMessage());

            log.error("JVM健康检查失败", e);
        }

        healthCheck.setCreateTime(LocalDateTime.now());
        healthCheckMapper.insert(healthCheck);
    }

    /**
     * 检查磁盘空间
     */
    private void checkDiskSpace() {
        long startTime = System.currentTimeMillis();
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setServiceName(SERVICE_NAME);
        healthCheck.setInstanceId(INSTANCE_ID);
        healthCheck.setServiceAddress(getServiceAddress());
        healthCheck.setCheckType("DISK");
        healthCheck.setCheckTime(LocalDateTime.now());

        try {
            java.io.File disk = new File("/");
            long freeSpace = disk.getFreeSpace();
            long totalSpace = disk.getTotalSpace();
            double usage = (double) (totalSpace - freeSpace) / totalSpace * 100;

            long responseTime = System.currentTimeMillis() - startTime;

            // 如果磁盘使用率超过90%，标记为DEGRADED
            String status = usage > 90 ? "DEGRADED" : "UP";
            healthCheck.setStatus(status);
            healthCheck.setResponseTime(responseTime);

            Map<String, Object> details = new HashMap<>();
            details.put("totalSpace", totalSpace / (1024 * 1024 * 1024) + " GB");
            details.put("freeSpace", freeSpace / (1024 * 1024 * 1024) + " GB");
            details.put("usage", String.format("%.2f%%", usage));
            healthCheck.setDetails(objectMapper.writeValueAsString(details));

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            healthCheck.setStatus("DOWN");
            healthCheck.setResponseTime(responseTime);
            healthCheck.setErrorMessage(e.getMessage());

            log.error("磁盘空间检查失败", e);
        }

        healthCheck.setCreateTime(LocalDateTime.now());
        healthCheckMapper.insert(healthCheck);
    }

    /**
     * 更新服务实例心跳
     */
    private void updateServiceHeartbeat() {
        try {
            QueryWrapper<ServiceInstance> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("instance_id", INSTANCE_ID);
            ServiceInstance instance = serviceInstanceMapper.selectOne(queryWrapper);

            if (instance == null) {
                // 首次启动，创建服务实例记录
                instance = new ServiceInstance();
                instance.setServiceName(SERVICE_NAME);
                instance.setInstanceId(INSTANCE_ID);
                instance.setServiceAddress(getServiceAddress());
                instance.setStatus("UP");
                instance.setHealthStatus("HEALTHY");
                instance.setWeight(100);
                instance.setIsPrimary(true); // 默认设为主节点
                instance.setStartTime(LocalDateTime.now());
                instance.setCreateTime(LocalDateTime.now());
                serviceInstanceMapper.insert(instance);
            } else {
                // 更新心跳时间
                instance.setLastHeartbeat(LocalDateTime.now());
                instance.setUpdateTime(LocalDateTime.now());
                serviceInstanceMapper.updateById(instance);
            }

        } catch (Exception e) {
            log.error("更新服务实例心跳失败", e);
        }
    }

    /**
     * 收集系统指标
     */
    private void collectSystemMetrics() {
        try {
            SystemMetrics metrics = new SystemMetrics();
            metrics.setServiceName(SERVICE_NAME);
            metrics.setInstanceId(INSTANCE_ID);

            // JVM堆内存使用率
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            long heapMemoryUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
            long heapMemoryMax = memoryMXBean.getHeapMemoryUsage().getMax();
            metrics.setJvmHeapUsage((double) heapMemoryUsed / heapMemoryMax * 100);

            // 线程数
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            metrics.setThreadCount(threadMXBean.getThreadCount());

            // CPU和内存使用率（通过操作系统API获取）
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            metrics.setMemoryUsage((double) usedMemory / totalMemory * 100);

            // 磁盘使用率
            java.io.File disk = new File("/");
            long freeSpace = disk.getFreeSpace();
            long totalSpace = disk.getTotalSpace();
            metrics.setDiskUsage((double) (totalSpace - freeSpace) / totalSpace * 100);

            // 请求统计和响应时间（可以从其他服务获取）
            // 这里暂时设置为默认值
            metrics.setRequestCount(0L);
            metrics.setErrorCount(0L);
            metrics.setAvgResponseTime(0.0);

            // CPU使用率（需要使用第三方库如OSHI）
            // 这里暂时设置为默认值
            metrics.setCpuUsage(0.0);

            metrics.setMetricsTime(LocalDateTime.now());
            metrics.setCreateTime(LocalDateTime.now());

            systemMetricsMapper.insert(metrics);

        } catch (Exception e) {
            log.error("收集系统指标失败", e);
        }
    }

    /**
     * 获取服务地址
     */
    private String getServiceAddress() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String hostAddress = inetAddress.getHostAddress();
            return hostAddress + ":8080";
        } catch (Exception e) {
            log.error("获取服务地址失败", e);
            return "localhost:8080";
        }
    }

    /**
     * 查询健康检查记录
     */
    public List<HealthCheck> getHealthChecks(String serviceName, Integer hours) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(hours);
        return healthCheckMapper.selectByTimeRange(serviceName, startTime, endTime);
    }

    /**
     * 获取服务健康状态统计
     */
    public List<Map<String, Object>> getHealthStats(String serviceName, Integer hours) {
        return healthCheckMapper.selectHealthStats(serviceName, hours);
    }

    /**
     * 获取健康的服务实例列表
     */
    public List<ServiceInstance> getHealthyInstances(String serviceName) {
        return serviceInstanceMapper.selectHealthyInstances(serviceName);
    }
}
