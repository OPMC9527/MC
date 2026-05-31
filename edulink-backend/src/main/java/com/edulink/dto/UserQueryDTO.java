package com.edulink.dto;

import lombok.Data;

@Data
public class UserQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String username;
    private String realName;
    private String role;
}