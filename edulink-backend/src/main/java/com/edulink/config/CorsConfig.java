package com.edulink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域资源共享（CORS）配置类
 * 允许前端应用（可能运行在不同域名/端口）访问后端API
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 配置跨域请求的映射规则
     * 允许所有路径、所有来源、常见HTTP方法，并支持携带凭证（Cookie等）
     *
     * @param registry CORS注册器，用于添加跨域映射
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                     // 对所有接口路径生效
                .allowedOriginPatterns("*")            // 允许所有来源（注意使用 allowedOriginPatterns 而非 allowedOrigins，以支持通配符和凭证）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的HTTP方法
                .allowedHeaders("*")                   // 允许所有请求头
                .allowCredentials(true)                // 允许携带Cookie/Authorization头等凭证
                .maxAge(3600);                         // 预检请求的缓存时间（单位：秒）
    }
}