package com.edulink.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClassVO {
    private Long id;
    private String className;
    private String grade;
    private Long headTeacherId;
    private String headTeacherName;  // 新增
    private String description;
    private LocalDateTime createTime;
}