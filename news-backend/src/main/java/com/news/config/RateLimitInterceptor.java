package com.news.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.common.RateLimit;
import com.news.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 速率限制拦截器
 * 基于滑动窗口算法实现的内存级速率限制
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    /**
     * 请求计数器缓存
     * Key: 限制键（如 "IP:192.168.1.1:/api/auth/login"）
     * Value: 请求记录
     */
    private final Map<String, RequestRecord> requestCounts = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理方法级别的处理器
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        // 没有 @RateLimit 注解，放行
        if (rateLimit == null) {
            return true;
        }

        // 构建限制键
        String limitKey = buildLimitKey(request, handlerMethod, rateLimit);

        // 检查是否超过限制
        if (isRateLimited(limitKey, rateLimit.limit(), rateLimit.period())) {
            log.warn("速率限制触发: {} - {}", limitKey, request.getRequestURI());

            // 返回错误响应
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            Result<Void> result = Result.error(429, "请求过于频繁，请稍后再试");
            response.getWriter().write(objectMapper.writeValueAsString(result));
            return false;
        }

        return true;
    }

    /**
     * 构建限制键
     */
    private String buildLimitKey(HttpServletRequest request, HandlerMethod method, RateLimit rateLimit) {
        String path = request.getRequestURI();
        String methodName = method.getMethod().getName();

        switch (rateLimit.type()) {
            case IP:
                String ip = getClientIp(request);
                return "IP:" + ip + ":" + path;
            case USER:
                Long userId = (Long) request.getAttribute("userId");
                String userKey = userId != null ? String.valueOf(userId) : getClientIp(request);
                return "USER:" + userKey + ":" + path;
            case GLOBAL:
                return "GLOBAL:" + path;
            default:
                return "IP:" + getClientIp(request) + ":" + path;
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 检查是否超过速率限制（滑动窗口算法）
     */
    private boolean isRateLimited(String key, int limit, int periodSeconds) {
        long now = System.currentTimeMillis();
        long windowStart = now - (periodSeconds * 1000L);

        RequestRecord record = requestCounts.compute(key, (k, existing) -> {
            if (existing == null || existing.windowStart < windowStart) {
                // 创建新窗口
                return new RequestRecord(now, new AtomicInteger(1));
            }
            // 在当前窗口内增加计数
            existing.count.incrementAndGet();
            return existing;
        });

        // 定期清理过期记录（简单实现，生产环境建议用定时任务）
        if (requestCounts.size() > 10000) {
            cleanExpiredRecords(windowStart);
        }

        return record.count.get() > limit;
    }

    /**
     * 清理过期记录
     */
    private void cleanExpiredRecords(long windowStart) {
        requestCounts.entrySet().removeIf(entry -> entry.getValue().windowStart < windowStart);
    }

    /**
     * 请求记录
     */
    private static class RequestRecord {
        final long windowStart;
        final AtomicInteger count;

        RequestRecord(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
