package com.edulink.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoticeVO {
    private Long id;
    private String title;
    private String content;
    private String publisherName;
    private String targetRoles;
    private Integer isUrgent;
    private Integer viewCount;
    private LocalDateTime createTime;
}