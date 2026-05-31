package com.edulink.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Notice {
    private Long id;
    private String title;
    private String content;
    private Long publisherId;
    private String targetRoles;   // JSON 字符串，如 "[\"STUDENT\",\"TEACHER\"]"
    private Integer isUrgent;
    private Integer viewCount;
    private LocalDateTime createTime;
}