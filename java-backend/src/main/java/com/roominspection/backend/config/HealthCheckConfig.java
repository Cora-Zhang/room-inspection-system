package com.roominspection.backend.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 健康检查配置类
 */
@Component
public class HealthCheckConfig {

    /**
     * 数据库健康检查
     */
    @Component
    public static class DatabaseHealthIndicator implements HealthIndicator {

        private final DataSource dataSource;

        public DatabaseHealthIndicator(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public Health health() {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(1)) {
                    return Health.up()
                            .withDetail("database", "MySQL")
                            .withDetail("validationQuery", "SELECT 1")
                            .build();
                } else {
                    return Health.down()
                            .withDetail("error", "Database connection validation failed")
                            .build();
                }
            } catch (Exception e) {
                return Health.down()
                        .withDetail("error", e.getMessage())
                        .build();
            }
        }
    }

    /**
     * Redis健康检查
     */
    @Component
    public static class RedisHealthIndicator implements HealthIndicator {

        private final org.springframework.data.redis.connection.RedisConnectionFactory redisConnectionFactory;

        public RedisHealthIndicator(org.springframework.data.redis.connection.RedisConnectionFactory redisConnectionFactory) {
            this.redisConnectionFactory = redisConnectionFactory;
        }

        @Override
        public Health health() {
            try {
                org.springframework.data.redis.connection.RedisConnection connection =
                        redisConnectionFactory.getConnection();
                String pong = connection.ping();
                connection.close();

                if ("PONG".equals(pong)) {
                    return Health.up()
                            .withDetail("redis", "Redis")
                            .build();
                } else {
                    return Health.down()
                            .withDetail("error", "Redis ping failed")
                            .build();
                }
            } catch (Exception e) {
                return Health.down()
                        .withDetail("error", e.getMessage())
                        .build();
            }
        }
    }

    /**
     * 磁盘空间健康检查
     */
    @Component
    public static class DiskSpaceHealthIndicator implements HealthIndicator {

        @Override
        public Health health() {
            java.io.File disk = new File("/");
            long freeSpace = disk.getFreeSpace();
            long totalSpace = disk.getTotalSpace();
            double usage = (double) (totalSpace - freeSpace) / totalSpace * 100;

            Status status = usage < 90 ? Status.UP : Status.DOWN;

            return Health.status(status)
                    .withDetail("totalSpace", totalSpace / (1024 * 1024 * 1024) + " GB")
                    .withDetail("freeSpace", freeSpace / (1024 * 1024 * 1024) + " GB")
                    .withDetail("usage", String.format("%.2f%%", usage))
                    .build();
        }
    }

    /**
     * JVM内存健康检查
     */
    @Component
    public static class JvmMemoryHealthIndicator implements HealthIndicator {

        @Override
        public Health health() {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double usage = (double) usedMemory / maxMemory * 100;

            Status status = usage < 90 ? Status.UP : Status.DOWN;

            return Health.status(status)
                    .withDetail("maxMemory", maxMemory / (1024 * 1024) + " MB")
                    .withDetail("totalMemory", totalMemory / (1024 * 1024) + " MB")
                    .withDetail("freeMemory", freeMemory / (1024 * 1024) + " MB")
                    .withDetail("usedMemory", usedMemory / (1024 * 1024) + " MB")
                    .withDetail("usage", String.format("%.2f%%", usage))
                    .build();
        }
    }
}
