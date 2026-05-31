package com.edulink.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserSearchVO {
    private Long id;
    private String username;
    private String realName;
    private String role;
    private String phone;
    private String email;
    private Integer status;
    private String avatar;
    private LocalDateTime createTime;
}