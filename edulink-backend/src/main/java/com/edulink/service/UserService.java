package com.edulink.service;

import com.edulink.dto.*;
import com.edulink.utils.Result;
import com.edulink.vo.LoginVO;
import com.edulink.vo.StudentVO;
import com.edulink.vo.UserSearchVO;

import java.awt.color.ICC_Profile;
import java.util.List;

public interface UserService {
    Result<LoginVO> login(LoginDTO loginDTO);
    Result<LoginVO.UserInfo> getUserProfile(Long userId);
    Result<?> updateProfile(Long userId, UpdateProfileDTO dto);
    Result<?> updatePassword(Long userId, UpdatePasswordDTO dto);
    Result<?> updateUser(UserUpdateDTO dto);
    Result<?> resetPassword(Long userId);
    Result<?> toggleStatus(Long userId);
    Result<?> register(RegisterDTO registerDTO);
    Result<?> deleteUser(Long id);
    List<UserSearchVO> listUsers(UserQueryDTO queryDTO);
}