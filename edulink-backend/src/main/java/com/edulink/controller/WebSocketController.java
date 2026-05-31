package com.edulink.controller;

import com.edulink.dto.ChatMessageDTO;
import com.edulink.entity.ChatSession;
import com.edulink.mapper.ChatSessionMapper;
import com.edulink.service.ChatService;
import com.edulink.utils.JwtUtil;
import com.edulink.vo.ChatMessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * WebSocket 消息控制器
 * 处理 STOMP 协议下的实时聊天消息，支持单聊和群聊消息的接收与推送
 */
@Controller
public class WebSocketController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 处理客户端发送的聊天消息
     * 客户端通过 STOMP 发送到 /app/chat.send 目标（因为配置了应用前缀 /app）
     *
     * 流程：
     * 1. 从 Authorization 头解析发送者 ID
     * 2. 调用服务层保存消息
     * 3. 若保存成功，根据会话类型（单聊/群聊）将消息推送给对应的接收方
     *
     * @param message 消息内容（会话ID、接收者ID、消息正文等）
     * @param token   JWT令牌，用于验证发送者身份
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO message,
                            @Header("Authorization") String token) {
        Long senderId = JwtUtil.getUserId(token);
        // 保存消息并获取返回的 VO（包含消息ID、时间戳等）
        var result = chatService.sendMessage(message, senderId);
        if (result.getCode() != 200) return;   // 保存失败则终止
        ChatMessageVO vo = result.getData();

        // 查询会话基本信息（会话类型、成员等）
        ChatSession session = chatSessionMapper.selectById(message.getSessionId());
        if (session == null) return;

        if ("GROUP".equals(session.getSessionType())) {
            // 群聊：向群内所有成员（除发送者）推送消息
            List<Long> memberIds = chatSessionMapper.selectMemberIdsBySessionId(message.getSessionId());
            for (Long memberId : memberIds) {
                if (memberId.equals(senderId)) continue;
                // 推送到每个用户的个人队列 /user/{memberId}/queue/messages
                messagingTemplate.convertAndSendToUser(
                        String.valueOf(memberId),
                        "/queue/messages",
                        vo
                );
            }
        } else {
            // 单聊：仅向接收者推送
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(message.getReceiverId()),
                    "/queue/messages",
                    vo
            );
        }
    }
}