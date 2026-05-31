package com.edulink.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageVO {
    private Long id;
    private Long sessionId;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private String content;
    private String messageType;
    private LocalDateTime createTime;
    private Boolean isSelf;
}