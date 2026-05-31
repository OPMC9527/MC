package com.edulink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置类（基于 STOMP 协议）
 * 启用 WebSocket 消息代理，支持实时推送通知、在线聊天等场景
 */
@Configuration
@EnableWebSocketMessageBroker   // 启用 WebSocket 消息代理功能
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     * - 启用简单代理，支持 /topic（广播）和 /queue（点对点）前缀
     * - 设置应用前缀 /app，客户端发送消息时需加上此前缀
     * - 设置用户前缀 /user，用于向特定用户发送私信
     *
     * @param registry 消息代理注册器
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");       // 启用内存消息代理，支持广播和队列
        registry.setApplicationDestinationPrefixes("/app");     // 客户端发送消息的目标前缀（由 @MessageMapping 处理）
        registry.setUserDestinationPrefix("/user");             // 用户点对点消息的前缀
    }

    /**
     * 注册 STOMP 端点（WebSocket 连接入口）
     * 客户端通过此路径建立 WebSocket 连接
     *
     * @param registry STOMP 端点注册器
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")                     // WebSocket 连接端点路径
                .setAllowedOriginPatterns("*")          // 允许所有来源跨域访问
                .withSockJS();                          // 启用 SockJS 回退机制（用于不支持原生 WebSocket 的浏览器）
    }
}