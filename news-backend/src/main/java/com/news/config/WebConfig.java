package com.news.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 配置跨域、拦截器、静态资源等
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Value("${upload.path}")
    private String uploadPath;

    /**
     * 配置跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置静态资源映射
     * 将 /uploads/** 映射到文件上传目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }

    /**
     * 配置拦截器
     * 注意：拦截器顺序很重要，速率限制应在认证之前
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 速率限制拦截器（最先执行）
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/error",
                        "/uploads/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );

        // 2. JWT 认证拦截器
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/auth/**",      // 登录注册
                        "/api/public/**",    // 公开接口
                        "/uploads/**",       // 静态资源
                        "/error",            // 错误页面
                        "/swagger-ui/**",    // Swagger UI
                        "/v3/api-docs/**"    // OpenAPI 文档
                );
    }
}
