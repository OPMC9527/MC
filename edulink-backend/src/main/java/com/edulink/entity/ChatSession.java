package com.edulink.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatSession {
    private Long id;
    private String sessionType;   // SINGLE, GROUP
    private String sessionName;
    private Long groupId;
    private Long creatorId;        // 新增：群创建者ID
    private LocalDateTime createTime;
}