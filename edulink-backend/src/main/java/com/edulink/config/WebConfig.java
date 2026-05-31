package com.edulink.config;

import com.edulink.interceptor.PermissionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 用于注册拦截器、消息转换器等 Web 层组件
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    /**
     * 注册拦截器
     * 配置权限拦截器对所有请求生效，但排除登录、测试、健康检查及 WebSocket 相关路径
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")                                 // 拦截所有请求
                .excludePathPatterns("/user/login",                     // 登录接口无需拦截
                        "/user/test",                                   // 测试接口
                        "/health",                                     // 健康检查接口
                        "class/list" ,
                        "/ws/**");                                      // WebSocket 长连接路径
    }
}