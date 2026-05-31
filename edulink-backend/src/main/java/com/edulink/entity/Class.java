package com.edulink.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Class {
    private Long id;
    private String className;
    private String grade;
    private Long headTeacherId;
    private String description;
    private LocalDateTime createTime;
}