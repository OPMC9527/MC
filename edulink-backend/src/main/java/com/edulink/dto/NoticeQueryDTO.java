package com.edulink.dto;

import lombok.Data;

@Data
public class NoticeQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String title;
    private Integer isUrgent;
}