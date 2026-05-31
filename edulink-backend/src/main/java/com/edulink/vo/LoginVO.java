package com.edulink.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String role;
        private String avatar;
        private String phone;
        private String email;
    }
}