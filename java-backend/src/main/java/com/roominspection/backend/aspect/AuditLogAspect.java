package com.roominspection.backend.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roominspection.backend.annotation.AuditLog;
import com.roominspection.backend.entity.AuditLog as AuditLogEntity;
import com.roominspection.backend.mapper.AuditLogMapper;
import com.roominspection.backend.security.CustomUserDetails;
import com.roominspection.backend.util.EncryptionUtil;
import eu.bitwalker.useragentutils.UserAgent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 拦截标记了@AuditLog注解的方法，自动记录操作日志
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogAspect.class);

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Autowired
    private EncryptionUtil encryptionUtil;

    /**
     * 配置切入点：标记了@AuditLog注解的方法
     */
    @Pointcut("@annotation(com.roominspection.backend.annotation.AuditLog)")
    public void auditLogPointcut() {
    }

    /**
     * 环绕通知
     */
    @Around("auditLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditLog auditLogAnnotation = method.getAnnotation(AuditLog.class);

        // 获取当前用户信息
        String userId = null;
        String username = null;
        String realName = null;
        try {
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();
            if (userDetails != null) {
                userId = userDetails.getUserId();
                username = userDetails.getUsername();
                realName = userDetails.getRealName();
            }
        } catch (Exception e) {
            // 未登录或认证失败
        }

        // 获取请求参数
        String params = null;
        if (request != null && auditLogAnnotation.logParams()) {
            try {
                // 限制参数长度，避免过大
                String paramsJson = JSON.toJSONString(joinPoint.getArgs(),
                        SerializerFeature.IgnoreNonFieldGetter);
                if (paramsJson.length() > 2000) {
                    params = paramsJson.substring(0, 2000) + "...(已截断)";
                } else {
                    params = paramsJson;
                }
                // 加密参数
                params = encryptionUtil.encryptJson(params);
            } catch (Exception e) {
                logger.error("记录操作日志参数失败", e);
            }
        }

        // 执行方法
        Object result = null;
        String resultJson = null;
        String status = "SUCCESS";
        String errorMessage = null;

        try {
            result = joinPoint.proceed();
            // 记录响应结果
            if (result != null && auditLogAnnotation.logResult()) {
                try {
                    resultJson = JSON.toJSONString(result,
                            SerializerFeature.IgnoreNonFieldGetter);
                    // 限制结果长度
                    if (resultJson.length() > 2000) {
                        resultJson = resultJson.substring(0, 2000) + "...(已截断)";
                    }
                    // 加密结果
                    resultJson = encryptionUtil.encryptJson(resultJson);
                } catch (Exception e) {
                    logger.error("记录操作日志结果失败", e);
                }
            }
        } catch (Throwable throwable) {
            status = "FAILED";
            errorMessage = throwable.getMessage();
            throw throwable;
        } finally {
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;

            // 构建操作日志对象
            AuditLogEntity auditLogEntity = new AuditLogEntity();
            auditLogEntity.setUserId(userId);
            auditLogEntity.setUsername(username);
            auditLogEntity.setRealName(realName);
            if (request != null) {
                auditLogEntity.setIp(getIpAddress(request));
                auditLogEntity.setMethod(request.getMethod());
                auditLogEntity.setUrl(request.getRequestURI());
                UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
                auditLogEntity.setUserAgent(userAgent.toString());
                auditLogEntity.setBrowser(userAgent.getBrowser().getName());
                auditLogEntity.setOs(userAgent.getOperatingSystem().getName());
            }
            auditLogEntity.setOperationType(auditLogAnnotation.operationType());
            auditLogEntity.setModule(auditLogAnnotation.module());
            auditLogEntity.setDescription(auditLogAnnotation.description());
            auditLogEntity.setParams(params);
            auditLogEntity.setResult(resultJson);
            auditLogEntity.setStatus(status);
            auditLogEntity.setErrorMessage(errorMessage);
            auditLogEntity.setExecutionTime(executionTime);
            auditLogEntity.setCreateTime(LocalDateTime.now());

            // 异步保存日志
            if (auditLogAnnotation.async()) {
                saveAuditLogAsync(auditLogEntity);
            } else {
                saveAuditLog(auditLogEntity);
            }
        }

        return result;
    }

    /**
     * 异步保存操作日志
     */
    @Async("taskExecutor")
    public void saveAuditLogAsync(AuditLogEntity auditLogEntity) {
        saveAuditLog(auditLogEntity);
    }

    /**
     * 保存操作日志
     */
    private void saveAuditLog(AuditLogEntity auditLogEntity) {
        try {
            auditLogMapper.insert(auditLogEntity);
        } catch (Exception e) {
            logger.error("保存操作日志失败", e);
        }
    }

    /**
     * 获取IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
