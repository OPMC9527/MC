package com.edulink.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudentVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String studentNumber;
    private Long classId;
    private String className;
    private String grade;
    private String gender;
    private LocalDate birthDate;
    private String parentName;
    private String parentPhone;
    private LocalDateTime createTime;
}