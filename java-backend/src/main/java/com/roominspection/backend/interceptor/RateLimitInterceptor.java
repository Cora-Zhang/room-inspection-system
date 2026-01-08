package com.roominspection.backend.interceptor;

import com.roominspection.backend.common.ApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

/**
 * API限流拦截器
 * 支持IP限流和用户限流
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        String userId = request.getHeader(ApiConstants.HEADER_TENANT_ID);

        // IP限流检查
        if (!checkRateLimit(ApiConstants.REDIS_PREFIX_RATE_LIMIT + "ip:" + clientIp,
                           ApiConstants.RATE_LIMIT_IP_PER_MINUTE, Duration.ofMinutes(1))) {
            sendRateLimitExceededResponse(response, "IP限流，请稍后再试");
            return false;
        }

        // 用户限流检查
        if (userId != null && !userId.isEmpty()) {
            if (!checkRateLimit(ApiConstants.REDIS_PREFIX_RATE_LIMIT + "user:" + userId,
                               ApiConstants.RATE_LIMIT_USER_PER_MINUTE, Duration.ofMinutes(1))) {
                sendRateLimitExceededResponse(response, "用户限流，请稍后再试");
                return false;
            }
        }

        return true;
    }

    /**
     * 检查限流
     * @param key Redis键
     * @param limit 限流次数
     * @param duration 时间窗口
     * @return true-未超过限流，false-超过限流
     */
    private boolean checkRateLimit(String key, int limit, Duration duration) {
        try {
            Long currentCount = redisTemplate.opsForValue().increment(key, 1);

            if (currentCount != null && currentCount == 1) {
                // 第一次访问，设置过期时间
                redisTemplate.expire(key, duration);
            }

            if (currentCount != null && currentCount > limit) {
                return false;
            }

            return true;
        } catch (Exception e) {
            // Redis异常，放行请求
            return true;
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 返回限流超出响应
     */
    private void sendRateLimitExceededResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":429,\"message\":\"" + message + "\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}");
    }
}
