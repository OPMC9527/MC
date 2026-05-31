package com.edulink.service.impl;

import com.edulink.dto.*;
import com.edulink.entity.Student;
import com.edulink.entity.User;
import com.edulink.mapper.*;
import com.edulink.service.UserService;
import com.edulink.utils.JwtUtil;
import com.edulink.utils.Result;
import com.edulink.vo.LoginVO;
import com.edulink.vo.UserSearchVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.List;

/**
 * 用户服务实现类
 * 提供用户登录、注册、信息维护、密码管理、状态管理等功能
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private ScoreMapper scoreMapper;
    @Autowired
    private AttendanceMapper attendanceMapper;
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private NoticeMapper noticeMapper;
    @Autowired
    private ChatSessionMapper chatSessionMapper;   // 可能需要创建

    /**
     * 用户登录
     * 根据用户名查询用户，验证密码和账号状态，生成JWT令牌
     *
     * @param loginDTO 登录请求参数（用户名、密码）
     * @return 包含用户信息和令牌的结果对象，失败时返回错误信息
     */
    @Override
    public Result<LoginVO> login(LoginDTO loginDTO) {
        // 1. 查询用户
        User user = userMapper.findByUsername(loginDTO.getUsername());
        if (user == null) {
            return Result.error("用户名不存在");
        }

        // 2. 验证密码（MD5加密后比对）
        String encryptPassword = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes());
        if (!encryptPassword.equals(user.getPassword())) {
            return Result.error("密码错误");
        }

        // 3. 检查状态（1=正常，0=禁用）
        if (user.getStatus() != 1) {
            return Result.error("账号已被禁用，请联系管理员");
        }

        // 4. 生成Token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 5. 构建返回数据
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);

        LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setRole(user.getRole());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        loginVO.setUserInfo(userInfo);

        return Result.success(loginVO);
    }

    /**
     * 获取用户个人信息
     * 根据用户ID查询详细信息，用于前端展示个人资料
     *
     * @param userId 用户ID
     * @return 包含用户详细信息的对象，失败时返回错误信息
     */
    @Override
    public Result<LoginVO.UserInfo> getUserProfile(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setRole(user.getRole());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());

        return Result.success(userInfo);
    }

    /**
     * 更新个人资料（手机号、邮箱）
     * 支持部分字段更新，仅更新非空字段
     *
     * @param userId 当前登录用户ID
     * @param dto    更新参数（手机号、邮箱）
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<?> updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 更新手机号和邮箱（仅当传入值不为空时）
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        userMapper.update(user);
        return Result.success("更新成功");
    }

    /**
     * 修改密码
     * 需要验证旧密码正确性，新密码MD5加密后保存
     *
     * @param userId 当前登录用户ID
     * @param dto    旧密码和新密码
     * @return 操作结果，成功提示重新登录
     */
    @Override
    @Transactional
    public Result<?> updatePassword(Long userId, UpdatePasswordDTO dto) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 验证旧密码
        String oldEncoded = DigestUtils.md5DigestAsHex(dto.getOldPassword().getBytes());
        if (!oldEncoded.equals(user.getPassword())) {
            return Result.error("旧密码错误");
        }
        // 加密新密码并保存
        String newEncoded = DigestUtils.md5DigestAsHex(dto.getNewPassword().getBytes());
        user.setPassword(newEncoded);
        userMapper.update(user);   // 必须更新 password 字段
        return Result.success("密码修改成功，请重新登录");
    }

    /**
     * 重置用户密码为默认值（123456）
     * 管理员操作，将指定用户的密码重置为123456（MD5加密后存储）
     *
     * @param userId 需要重置密码的用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<?> resetPassword(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 重置为默认密码 123456 并 MD5 加密
        String defaultPassword = DigestUtils.md5DigestAsHex("123456".getBytes());
        user.setPassword(defaultPassword);
        userMapper.update(user);
        return Result.success("密码已重置为123456");
    }

    /**
     * 切换用户状态（启用/禁用）
     * 状态值取反：1 -> 0, 0 -> 1
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<?> toggleStatus(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 切换状态：0->1, 1->0
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userMapper.update(user);
        return Result.success("状态已更新");
    }

    /**
     * 用户注册（支持学生和教师角色）
     * 校验用户名、手机号唯一性，创建用户记录。若角色为学生，同时创建学生扩展信息
     *
     * @param dto 注册参数（用户名、密码、真实姓名、角色、手机号、邮箱、班级ID等）
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<?> register(RegisterDTO dto) {
        // 1. 检查用户名是否已存在
        if (userMapper.findByUsername(dto.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        // 2. 检查手机号是否已被注册（注意：这里用的是findByUsername，实际应按手机号查询，可能存在逻辑缺陷）
        if (userMapper.findByUsername(dto.getPhone()) != null) {
            return Result.error("手机号已被注册");
        }
        // 处理空邮箱为null
        if (dto.getEmail() != null && dto.getEmail().trim().isEmpty()) {
            dto.setEmail(null);
        }
        // 3. 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()));
        user.setRealName(dto.getRealName());
        user.setRole(dto.getRole());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(1);  // 默认启用
        userMapper.insert(user);
        Long userId = user.getId();

        // 4. 如果是学生，插入 student 表
        if ("STUDENT".equals(dto.getRole())) {
            Student student = new Student();
            student.setId(userId);
            student.setStudentNumber(dto.getUsername()); // 可自定义学号生成规则
            student.setClassId(dto.getClassId());
            student.setGender("MALE"); // 默认性别，可后续编辑
            studentMapper.insert(student);
        }
        return Result.success("注册成功");
    }

    /**
     * 条件查询用户列表（用于管理页面）
     * 支持按用户名、真实姓名、角色进行模糊或精确筛选
     *
     * @param queryDTO 查询条件封装
     * @return 用户简要信息列表
     */
    @Override
    public List<UserSearchVO> listUsers(UserQueryDTO queryDTO) {
        return userMapper.listUsers(queryDTO);
    }

    /**
     * 更新用户信息（管理员操作）
     * 可更新真实姓名、手机号、邮箱。若用户为学生且提供了班级ID，同时更新学生的班级信息
     *
     * @param dto 更新参数（用户ID、真实姓名、手机号、邮箱、班级ID等）
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<?> updateUser(UserUpdateDTO dto) {
        User user = userMapper.findById(dto.getId());
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 更新用户基本字段
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        userMapper.update(user);

        // 如果是学生，更新班级信息
        if ("STUDENT".equals(user.getRole()) && dto.getClassId() != null) {
            Student student = studentMapper.selectById(dto.getId());
            if (student != null) {
                student.setClassId(dto.getClassId());
                studentMapper.update(student);
            }
        }
        return Result.success("更新成功");
    }

    @Override
    @Transactional
    public Result<?> deleteUser(Long userId) {
        // 1. 先删除考勤记录（因为 attendance 表引用了 student.id）
        attendanceMapper.deleteByStudentId(userId);
        // 2. 删除成绩记录（如果 score 表引用了 student.id）
        scoreMapper.deleteByStudentId(userId);
        // 3. 删除学生记录（如果该用户是学生）
        studentMapper.deleteById(userId);
        // 4. 删除通知表中该用户发布的通知
        noticeMapper.deleteByPublisherId(userId);
        // 5. 删除聊天消息（发送的消息）
        chatMessageMapper.deleteBySenderId(userId);
        // 6. 删除聊天成员表中的记录
        chatSessionMapper.deleteByUserId(userId);
        // 7. 最后删除用户
        int rows = userMapper.deleteById(userId);
        if (rows == 0) {
            return Result.error("用户不存在");
        }
        return Result.success("删除成功");
    }
}