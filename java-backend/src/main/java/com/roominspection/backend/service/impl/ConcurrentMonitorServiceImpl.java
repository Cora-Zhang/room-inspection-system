package com.roominspection.backend.service.impl;

import com.roominspection.backend.entity.Device;
import com.roominspection.backend.entity.DeviceMetric;
import com.roominspection.backend.entity.MonitorConfig;
import com.roominspection.backend.entity.MonitorTask;
import com.roominspection.backend.mapper.DeviceMapper;
import com.roominspection.backend.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 并发监控数据采集服务实现
 * 支持200+设备并发采集，性能优化核心
 */
@Slf4j
@Service
public class ConcurrentMonitorServiceImpl implements ConcurrentMonitorService {

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private SnmpMonitorService snmpMonitorService;

    @Autowired
    private MonitorConfigService monitorConfigService;

    @Autowired
    private MonitorTaskService monitorTaskService;

    @Autowired
    private DeviceMetricService deviceMetricService;

    // 线程池配置
    private static final int CORE_POOL_SIZE = 20;
    private static final int MAX_POOL_SIZE = 50;
    private static final int QUEUE_CAPACITY = 200;
    private static final long KEEP_ALIVE_TIME = 60L;

    // 采集线程池
    private ThreadPoolExecutor collectionExecutor;

    // 是否启动定时任务
    private volatile boolean scheduledTasksStarted = false;

    // 性能统计
    private final AtomicLong totalCollected = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);
    private final AtomicLong totalDuration = new AtomicLong(0);
    private final ConcurrentHashMap<String, Long> deviceCollectTimes = new ConcurrentHashMap<>();

    // 当前运行任务数
    private final AtomicInteger runningTaskCount = new AtomicInteger(0);

    /**
     * 初始化线程池
     */
    private void initExecutor() {
        if (collectionExecutor == null) {
            synchronized (this) {
                if (collectionExecutor == null) {
                    collectionExecutor = new ThreadPoolExecutor(
                            CORE_POOL_SIZE,
                            MAX_POOL_SIZE,
                            KEEP_ALIVE_TIME,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                            new ThreadFactory() {
                                private final AtomicInteger threadNumber = new AtomicInteger(1);

                                @Override
                                public Thread newThread(Runnable r) {
                                    Thread t = new Thread(r, "monitor-collect-" + threadNumber.getAndIncrement());
                                    t.setDaemon(true);
                                    return t;
                                }
                            },
                            new ThreadPoolExecutor.CallerRunsPolicy()
                    );
                    log.info("监控采集线程池初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                            CORE_POOL_SIZE, MAX_POOL_SIZE, QUEUE_CAPACITY);
                }
            }
        }
    }

    /**
     * 并发采集指定设备的监控数据
     */
    @Override
    public List<CompletableFuture<MonitorTask>> collectDeviceMetrics(List<Device> devices) {
        initExecutor();

        log.info("开始并发采集设备指标: deviceCount={}", devices.size());
        runningTaskCount.addAndGet(devices.size());

        List<CompletableFuture<MonitorTask>> futures = devices.stream()
                .map(device -> CompletableFuture.supplyAsync(() -> collectDevice(device), collectionExecutor))
                .collect(Collectors.toList());

        return futures;
    }

    /**
     * 采集单个设备指标
     */
    @Override
    public List<DeviceMetric> collectSingleDevice(Device device) {
        MonitorTask task = collectDevice(device);
        if ("SUCCESS".equals(task.getTaskStatus())) {
            return deviceMetricService.getLatestMetricsByDeviceId(device.getId());
        }
        return Collections.emptyList();
    }

    /**
     * 采集单个设备（核心方法）
     */
    private MonitorTask collectDevice(Device device) {
        long startTime = System.currentTimeMillis();
        String taskId = UUID.randomUUID().toString();

        // 创建监控任务
        MonitorTask task = new MonitorTask();
        task.setTaskId(taskId);
        task.setDeviceId(device.getId());
        task.setDeviceName(device.getDeviceName());
        task.setDeviceType(device.getDeviceType());
        task.setRoomId(device.getRoomId());
        task.setTaskType("COLLECTION");
        task.setTaskStatus("PENDING");
        monitorTaskService.createTask(task);

        try {
            // 标记任务开始
            monitorTaskService.markTaskStart(taskId);

            // 获取监控配置
            MonitorConfig config = monitorConfigService.getConfigByDeviceType(device.getDeviceType());

            // 根据设备类型采集数据
            List<DeviceMetric> metrics = new ArrayList<>();

            switch (device.getDeviceType()) {
                case "SERVER":
                case "SWITCH":
                case "ROUTER":
                case "FIREWALL":
                    metrics = snmpMonitorService.collectMetrics(device);
                    break;
                case "UPS":
                case "PDU":
                case "AIR_CONDITIONER":
                    metrics = snmpMonitorService.collectMetrics(device);
                    break;
                case "SENSOR":
                    // 传感器数据采集（假设有专门的服务）
                    metrics = collectSensorMetrics(device);
                    break;
                default:
                    log.warn("不支持的设备类型: deviceType={}", device.getDeviceType());
            }

            // 保存指标数据
            if (!metrics.isEmpty()) {
                // 批量保存，提高性能
                metrics.forEach(m -> {
                    m.setCreatedAt(LocalDateTime.now());
                    m.setCollectionTime(LocalDateTime.now());
                });
                deviceMetricService.saveBatchMetrics(metrics);
            }

            // 标记任务完成
            monitorTaskService.markTaskComplete(taskId, metrics.size(), true);

            // 更新统计
            totalCollected.incrementAndGet();
            long duration = System.currentTimeMillis() - startTime;
            totalDuration.addAndGet(duration);
            deviceCollectTimes.put(device.getId(), duration);

            log.debug("设备采集完成: deviceId={}, metricCount={}, duration={}ms",
                    device.getId(), metrics.size(), duration);

            task.setTaskStatus("SUCCESS");
            task.setDataCount(metrics.size());
            task.setDuration(duration);

        } catch (Exception e) {
            log.error("设备采集失败: deviceId={}, error={}", device.getId(), e.getMessage(), e);

            // 标记任务失败
            monitorTaskService.markTaskComplete(taskId, 0, false);

            totalFailed.incrementAndGet();
            task.setTaskStatus("FAILED");
            task.setErrorMessage(e.getMessage());
        } finally {
            runningTaskCount.decrementAndGet();
        }

        return task;
    }

    /**
     * 采集传感器指标
     */
    private List<DeviceMetric> collectSensorMetrics(Device device) {
        List<DeviceMetric> metrics = new ArrayList<>();
        // 这里可以集成Modbus或其他协议采集传感器数据
        // 暂时返回空列表，实际需要根据传感器类型实现
        return metrics;
    }

    /**
     * 并发采集指定机房的监控数据
     */
    @Override
    public Map<String, Object> collectRoomMetrics(String roomId) {
        log.info("开始采集机房指标: roomId={}", roomId);

        // 查询机房的所有设备
        List<Device> devices = deviceMapper.selectList(
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper.lambdaQuery()
                        .eq(Device::getRoomId, roomId)
        );

        if (devices.isEmpty()) {
            return createEmptyResult();
        }

        // 并发采集
        List<CompletableFuture<MonitorTask>> futures = collectDeviceMetrics(devices);

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return collectStatistics(futures);
    }

    /**
     * 并发采集所有设备的监控数据
     */
    @Override
    public Map<String, Object> collectAllDeviceMetrics() {
        log.info("开始采集所有设备指标");

        // 查询所有设备
        List<Device> devices = deviceMapper.selectList(null);

        if (devices.isEmpty()) {
            return createEmptyResult();
        }

        log.info("总设备数: {}", devices.size());

        // 并发采集
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<MonitorTask>> futures = collectDeviceMetrics(devices);

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long totalTime = System.currentTimeMillis() - startTime;

        Map<String, Object> result = collectStatistics(futures);
        result.put("totalDevices", devices.size());
        result.put("totalTime", totalTime);
        result.put("throughput", String.format("%.2f devices/sec",
                (double) devices.size() / (totalTime / 1000.0)));

        return result;
    }

    /**
     * 收集统计信息
     */
    private Map<String, Object> collectStatistics(List<CompletableFuture<MonitorTask>> futures) {
        int total = futures.size();
        int success = 0;
        int failed = 0;
        int timeout = 0;
        long totalDuration = 0;

        for (CompletableFuture<MonitorTask> future : futures) {
            try {
                MonitorTask task = future.get();
                if ("SUCCESS".equals(task.getTaskStatus())) {
                    success++;
                } else if ("FAILED".equals(task.getTaskStatus())) {
                    failed++;
                } else if ("TIMEOUT".equals(task.getTaskStatus())) {
                    timeout++;
                }
                if (task.getDuration() != null) {
                    totalDuration += task.getDuration();
                }
            } catch (Exception e) {
                failed++;
                log.error("获取任务结果失败", e);
            }
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", total);
        statistics.put("success", success);
        statistics.put("failed", failed);
        statistics.put("timeout", timeout);
        statistics.put("successRate", total > 0 ? String.format("%.2f%%", (double) success / total * 100) : "0%");
        statistics.put("avgDuration", total > 0 ? totalDuration / total : 0);

        return statistics;
    }

    /**
     * 创建空结果
     */
    private Map<String, Object> createEmptyResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        result.put("success", 0);
        result.put("failed", 0);
        result.put("successRate", "0%");
        return result;
    }

    /**
     * 启动定时监控任务
     */
    @Override
    public void startScheduledTasks() {
        if (scheduledTasksStarted) {
            log.warn("定时监控任务已启动");
            return;
        }
        scheduledTasksStarted = true;
        log.info("定时监控任务已启动");
    }

    /**
     * 停止定时监控任务
     */
    @Override
    public void stopScheduledTasks() {
        scheduledTasksStarted = false;
        log.info("定时监控任务已停止");
    }

    /**
     * 定时采集任务（根据配置的频率）
     */
    @Scheduled(fixedDelay = 1000) // 每秒检查一次是否需要采集
    public void scheduledCollect() {
        if (!scheduledTasksStarted) {
            return;
        }

        try {
            // 获取所有启用的监控配置
            List<MonitorConfig> configs = monitorConfigService.getAllEnabledConfigs();

            for (MonitorConfig config : configs) {
                // 检查是否需要采集（这里应该根据上次采集时间和配置的频率来判断）
                // 简化实现：每次都采集，实际应该记录上次采集时间
                if (shouldCollect(config.getDeviceType(), config.getCollectionInterval())) {
                    collectByDeviceType(config.getDeviceType());
                }
            }
        } catch (Exception e) {
            log.error("定时采集任务执行失败", e);
        }
    }

    /**
     * 判断是否需要采集
     */
    private boolean shouldCollect(String deviceType, Integer interval) {
        // 简化实现：总是返回true，实际应该基于上次采集时间判断
        return true;
    }

    /**
     * 按设备类型采集
     */
    private void collectByDeviceType(String deviceType) {
        List<Device> devices = deviceMapper.selectList(
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper.lambdaQuery()
                        .eq(Device::getDeviceType, deviceType)
        );

        if (!devices.isEmpty()) {
            collectDeviceMetrics(devices);
        }
    }

    /**
     * 获取采集性能统计
     */
    @Override
    public Map<String, Object> getPerformanceStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long total = totalCollected.get();
        long failed = totalFailed.get();
        long durationSum = totalDuration.get();
        long avgDuration = total > 0 ? durationSum / total : 0;

        stats.put("totalCollected", total);
        stats.put("totalFailed", failed);
        stats.put("successRate", (total + failed) > 0 ?
                String.format("%.2f%%", (double) total / (total + failed) * 100) : "0%");
        stats.put("avgDuration", avgDuration);

        // 线程池统计
        stats.put("activeThreads", collectionExecutor != null ? collectionExecutor.getActiveCount() : 0);
        stats.put("poolSize", collectionExecutor != null ? collectionExecutor.getPoolSize() : 0);
        stats.put("queueSize", collectionExecutor != null ? collectionExecutor.getQueue().size() : 0);

        // 设备采集耗时Top10
        List<Map.Entry<String, Long>> topDevices = deviceCollectTimes.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        stats.put("topSlowDevices", topDevices);

        return stats;
    }

    /**
     * 获取当前运行的采集任务数
     */
    @Override
    public int getRunningTaskCount() {
        return runningTaskCount.get();
    }
}
