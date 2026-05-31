package com.edulink.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Student {
    private Long id;
    private String studentNumber;
    private Long classId;
    private String gender;
    private LocalDate birthDate;
    private Long parentId;
    private LocalDateTime createTime;
}