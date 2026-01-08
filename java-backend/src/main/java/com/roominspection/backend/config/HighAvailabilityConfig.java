package com.roominspection.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 高可用配置
 * 支持负载均衡、多实例部署、故障切换
 */
@Configuration
@EnableAsync
@EnableScheduling
@EnableRetry
public class HighAvailabilityConfig {

    @Value("${spring.application.name:room-inspection-backend}")
    private String applicationName;

    @Value("${server.port:8080}")
    private int serverPort;

    /**
     * 配置Redis模板
     * 用于多实例间的数据共享和状态同步
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用String序列化器作为Key的序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // 使用JSON序列化器作为Value的序列化器
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    /**
     * 配置异步任务线程池
     * 用于异步处理告警通知、工单创建等任务
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("async-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 配置监控数据采集线程池
     * 用于并发采集200+设备的监控数据
     */
    @Bean(name = "monitorExecutor")
    public Executor monitorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("monitor-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 配置工单处理线程池
     * 用于异步处理工单，确保响应时间≤3秒
     */
    @Bean(name = "workOrderExecutor")
    public Executor workOrderExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(300);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("workorder-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 配置数据备份线程池
     */
    @Bean(name = "backupExecutor")
    public Executor backupExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("backup-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 配置健康检查线程池
     */
    @Bean(name = "healthCheckExecutor")
    public Executor healthCheckExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("healthcheck-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 分布式锁配置
     * 用于多实例间的任务协调和资源竞争控制
     */
    @Bean
    public DistributedLock distributedLock(RedisTemplate<String, Object> redisTemplate) {
        return new DistributedLock(redisTemplate);
    }

    /**
     * 负载均衡策略配置
     */
    @Bean
    public LoadBalanceStrategy loadBalanceStrategy() {
        return new LoadBalanceStrategy();
    }

    /**
     * 分布式锁实现
     */
    public static class DistributedLock {
        private final RedisTemplate<String, Object> redisTemplate;
        private static final long LOCK_EXPIRE_TIME = 30000; // 锁过期时间30秒

        public DistributedLock(RedisTemplate<String, Object> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        /**
         * 尝试获取锁
         */
        public boolean tryLock(String lockKey) {
            String key = "lock:" + lockKey;
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(key, System.currentTimeMillis(), LOCK_EXPIRE_TIME, java.util.concurrent.TimeUnit.MILLISECONDS);
            return Boolean.TRUE.equals(locked);
        }

        /**
         * 释放锁
         */
        public void unlock(String lockKey) {
            String key = "lock:" + lockKey;
            redisTemplate.delete(key);
        }

        /**
         * 带过期时间的获取锁
         */
        public boolean tryLock(String lockKey, long expireTime, java.util.concurrent.TimeUnit timeUnit) {
            String key = "lock:" + lockKey;
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(key, System.currentTimeMillis(), expireTime, timeUnit);
            return Boolean.TRUE.equals(locked);
        }
    }

    /**
     * 负载均衡策略
     */
    public static class LoadBalanceStrategy {
        /**
         * 轮询策略
         */
        private int roundRobinIndex = 0;

        public synchronized String roundRobin(java.util.List<String> instances) {
            if (instances == null || instances.isEmpty()) {
                return null;
            }

            String instance = instances.get(roundRobinIndex);
            roundRobinIndex = (roundRobinIndex + 1) % instances.size();
            return instance;
        }

        /**
         * 加权轮询策略
         */
        public synchronized String weightedRoundRobin(java.util.List<InstanceWeight> instances) {
            if (instances == null || instances.isEmpty()) {
                return null;
            }

            // 计算总权重
            int totalWeight = instances.stream()
                    .mapToInt(InstanceWeight::getWeight)
                    .sum();

            // 选择实例
            int randomWeight = (int) (Math.random() * totalWeight);
            for (InstanceWeight instance : instances) {
                randomWeight -= instance.getWeight();
                if (randomWeight <= 0) {
                    return instance.getInstance();
                }
            }

            return instances.get(0).getInstance();
        }

        /**
         * 随机策略
         */
        public String random(java.util.List<String> instances) {
            if (instances == null || instances.isEmpty()) {
                return null;
            }

            int randomIndex = (int) (Math.random() * instances.size());
            return instances.get(randomIndex);
        }
    }

    /**
     * 实例权重
     */
    public static class InstanceWeight {
        private String instance;
        private int weight;

        public InstanceWeight(String instance, int weight) {
            this.instance = instance;
            this.weight = weight;
        }

        public String getInstance() {
            return instance;
        }

        public int getWeight() {
            return weight;
        }
    }
}
