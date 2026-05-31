package com.edulink.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private Long id;
    private String realName;
    private String phone;
    private String email;
    private Long classId;   // 学生所属班级
}