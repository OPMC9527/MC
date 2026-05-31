package com.edulink.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatSessionVO {
    private Long id;
    private String sessionType;      // SINGLE, GROUP
    private String sessionName;
    private Long groupId;
    private LocalDateTime createTime;

    // 扩展字段（用于前端展示）
    private Long targetUserId;
    private String targetUserName;
    private String targetUserRole;
    private String targetUserAvatar;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;
    private Integer memberCount;
}