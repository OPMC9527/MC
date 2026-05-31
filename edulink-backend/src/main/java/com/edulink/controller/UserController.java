package com.edulink.controller;

import com.edulink.dto.*;
import com.edulink.entity.Student;
import com.edulink.mapper.StudentMapper;
import com.edulink.mapper.UserMapper;
import com.edulink.service.UserService;
import com.edulink.utils.JwtUtil;
import com.edulink.utils.PageResult;
import com.edulink.utils.Result;
import com.edulink.utils.UserContext;
import com.edulink.vo.ChildVO;
import com.edulink.vo.LoginVO;
import com.edulink.vo.UserSearchVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户控制器
 * 处理用户登录、个人信息管理、密码修改、用户管理（列表、更新、重置密码、状态切换、注册）等请求
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StudentMapper studentMapper;


    /**
     * 用户登录
     *
     * @param loginDTO 登录请求参数（用户名、密码）
     * @return 登录结果，成功时包含用户信息和JWT令牌
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    /**
     * 健康检查接口（用于测试服务是否可用）
     *
     * @return 固定字符串 "pong"
     */
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }

    /**
     * 搜索用户（供关联选择使用）
     * 根据关键词模糊匹配真实姓名，排除当前登录用户，最多返回20条
     *
     * @param keyword 搜索关键词
     * @return 匹配的用户简要信息列表
     */
    @GetMapping("/search")
    public Result<List<UserSearchVO>> searchUsers(@RequestParam String keyword) {
        Long currentUserId = UserContext.getUserId();
        List<UserSearchVO> list = userMapper.searchUsers(keyword, currentUserId);
        return Result.success(list);
    }

    /**
     * 测试接口（验证后端是否正常运行）
     *
     * @return 成功提示字符串
     */
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("测试成功");
    }

    /**
     * 获取当前登录用户的个人信息
     * 从线程上下文UserContext中获取当前用户ID
     *
     * @return 用户详细信息（不含密码等敏感信息）
     */
    @GetMapping("/profile")
    public Result<LoginVO.UserInfo> getProfile() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        return userService.getUserProfile(userId);
    }

    /**
     * 更新当前登录用户的个人资料（手机号、邮箱）
     *
     * @param dto 更新的资料（手机号、邮箱）
     * @return 操作结果
     */
    @PutMapping("/profile")
    public Result<?> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        Long userId = UserContext.getUserId();
        return userService.updateProfile(userId, dto);
    }

    /**
     * 修改当前登录用户的密码
     *
     * @param dto 旧密码和新密码
     * @return 操作结果，成功提示重新登录
     */
    @PutMapping("/password")
    public Result<?> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        Long userId = UserContext.getUserId();
        return userService.updatePassword(userId, dto);
    }

    /**
     * 分页查询用户列表（管理员使用）
     * 支持按用户名、真实姓名、角色进行筛选，结果自动分页
     *
     * @param queryDTO 查询条件及分页参数（pageNum、pageSize）
     * @return 分页结果，包含用户简要信息列表及总记录数
     */
    @GetMapping("/list")
    public Result<PageResult<UserSearchVO>> list(UserQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<UserSearchVO> list = userService.listUsers(queryDTO);
        PageInfo<UserSearchVO> pageInfo = new PageInfo<>(list);
        PageResult<UserSearchVO> pageResult = new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
        return Result.success(pageResult);
    }

    /**
     * 更新指定用户的信息（管理员操作）
     * 可更新真实姓名、手机号、邮箱，若为学生可同时更新班级
     *
     * @param id  用户ID（路径参数）
     * @param dto 更新的字段（真实姓名、手机号、邮箱、班级ID等）
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO dto) {
        dto.setId(id);
        return userService.updateUser(dto);
    }

    /**
     * 重置指定用户的密码为默认值123456（管理员操作）
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/reset-password")
    public Result<?> resetPassword(@PathVariable Long id) {
        return userService.resetPassword(id);
    }

    /**
     * 切换指定用户的启用/禁用状态（管理员操作）
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public Result<?> toggleStatus(@PathVariable Long id) {
        return userService.toggleStatus(id);
    }

    /**
     * 用户注册（支持学生和教师角色）
     * 校验用户名、手机号唯一性，创建用户记录，学生同时创建学生扩展信息
     *
     * @param registerDTO 注册参数（用户名、密码、真实姓名、角色、手机号、邮箱、班级ID等）
     * @return 操作结果
     */
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    /**
     * 获取所有教师列表（供其他模块选择教师时使用）
     * 返回教师用户的ID、真实姓名、角色、头像，按姓名排序
     *
     * @return 教师简要信息列表
     */
    @GetMapping("/teachers")
    public Result<List<UserSearchVO>> listTeachers() {
        List<UserSearchVO> list = userMapper.listByRole("TEACHER");
        return Result.success(list);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

    // 获取家长的所有孩子
    @GetMapping("/parent/children")
    public Result<List<ChildVO>> getChildren() {
        Long parentId = UserContext.getUserId();
        List<ChildVO> list = studentMapper.findChildrenByParentId(parentId);
        return Result.success(list);
    }

    // 绑定孩子（通过学号）
    @PostMapping("/parent/child")
    public Result<?> bindChild(@RequestParam String studentNumber) {
        Long parentId = UserContext.getUserId();
        Student student = studentMapper.findByStudentNumber(studentNumber);
        if (student == null) return Result.error("学号不存在");
        if (student.getParentId() != null) return Result.error("该学生已绑定其他家长");
        student.setParentId(parentId);
        studentMapper.update(student);
        return Result.success("绑定成功");
    }

    // 解绑孩子
    @DeleteMapping("/parent/child/{studentId}")
    public Result<?> unbindChild(@PathVariable Long studentId) {
        Long parentId = UserContext.getUserId();
        Student student = studentMapper.selectById(studentId);
        if (student == null || !student.getParentId().equals(parentId)) {
            return Result.error("无权限操作");
        }
        student.setParentId(null);
        studentMapper.update(student);
        return Result.success("解绑成功");
    }

}