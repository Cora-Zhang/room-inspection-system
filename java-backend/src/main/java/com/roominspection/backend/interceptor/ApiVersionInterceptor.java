package com.roominspection.backend.interceptor;

import com.roominspection.backend.common.ApiConstants;
import com.roominspection.backend.common.ApiVersion;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * API版本控制拦截器
 * 根据请求头中的X-API-Version或URL路径确定API版本
 */
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {

    private static final String VERSION_URI_PATTERN = "/api/(v\\d+)/.*";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        Class<?> clazz = handlerMethod.getBeanType();

        // 获取方法或类上的ApiVersion注解
        ApiVersion methodVersion = method.getAnnotation(ApiVersion.class);
        ApiVersion classVersion = clazz.getAnnotation(ApiVersion.class);

        String requestedVersion = getRequestedVersion(request);
        String expectedVersion = methodVersion != null ? methodVersion.value() :
                                (classVersion != null ? classVersion.value() : ApiConstants.DEFAULT_VERSION);

        // 如果注解配置为支持多版本，则放行
        if ((methodVersion != null && methodVersion.multiVersion()) ||
            (classVersion != null && classVersion.multiVersion())) {
            request.setAttribute(ApiConstants.HEADER_API_VERSION, requestedVersion);
            return true;
        }

        // 验证版本是否匹配
        if (requestedVersion.equals(expectedVersion)) {
            request.setAttribute(ApiConstants.HEADER_API_VERSION, requestedVersion);
            return true;
        }

        // 版本不匹配
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":404,\"message\":\"API版本不存在\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}");
        return false;
    }

    /**
     * 获取请求的API版本
     * 优先从URL路径中提取，其次从请求头中获取
     */
    private String getRequestedVersion(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // 从URL路径中提取版本
        if (uri.matches(VERSION_URI_PATTERN)) {
            String[] parts = uri.split("/");
            if (parts.length >= 3) {
                return parts[2];
            }
        }

        // 从请求头中获取版本
        String version = request.getHeader(ApiConstants.HEADER_API_VERSION);
        return version != null ? version : ApiConstants.DEFAULT_VERSION;
    }
}
