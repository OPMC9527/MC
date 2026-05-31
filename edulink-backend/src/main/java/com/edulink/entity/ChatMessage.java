package com.edulink.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long id;
    private Long sessionId;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private String senderAvatar;
    private String messageType;
    private String content;
    private Integer isRead;
    private LocalDateTime createTime;
}