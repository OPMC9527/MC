package com.edulink.service.impl;

import com.edulink.dto.StudentQueryDTO;
import com.edulink.dto.StudentUpdateDTO;
import com.edulink.entity.Student;
import com.edulink.entity.User;
import com.edulink.mapper.*;
import com.edulink.service.StudentService;
import com.edulink.utils.Result;
import com.edulink.utils.UserContext;
import com.edulink.vo.StudentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 学生服务实现类
 * 提供学生信息的查询、新增、更新、删除等功能，并根据用户角色进行数据权限隔离
 */
@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ClassMapper classMapper;
    @Autowired
    private AttendanceMapper attendanceMapper;
    @Autowired
    private ScoreMapper scoreMapper;

    /**
     * 根据学生ID获取学生详情（包含关联的用户信息、班级信息、家长信息）
     *
     * @param id 学生ID
     * @return 包含学生视图对象的结果，不存在则返回错误信息
     */
    @Override
    public Result<StudentVO> getStudentDetail(Long id) {
        StudentVO vo = studentMapper.selectStudentDetailById(id);
        return vo != null ? Result.success(vo) : Result.error("学生不存在");
    }

    /**
     * 条件查询学生列表（支持分页，由调用方控制分页参数）
     * 根据当前登录用户角色自动设置查询范围：
     * - TEACHER：仅查询该教师担任班主任的班级中的学生
     * - STUDENT：仅查询自己的学生记录
     * - PARENT：查询其关联的所有子女的学生记录
     * - ADMIN：查询所有学生（无限制）
     *
     * @param queryDTO 查询条件对象（班级名称、学生姓名、学号、班级ID列表等）
     * @return 学生视图对象列表
     */
    @Override
    public List<StudentVO> listStudents(StudentQueryDTO queryDTO) {
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();

        if ("TEACHER".equals(role)) {
            // 教师：只显示其担任班主任的班级中的学生
            List<Long> classIds = classMapper.findClassIdsByHeadTeacherId(userId);
            if (classIds.isEmpty()) {
                return new ArrayList<>();
            }
            queryDTO.setClassIds(classIds);
        } else if ("STUDENT".equals(role)) {
            // 学生：只显示自己的记录
            Long studentId = studentMapper.findIdByUserId(userId);
            if (studentId != null) {
                queryDTO.setStudentId(studentId);
            } else {
                return new ArrayList<>();
            }
        } else if ("PARENT".equals(role)) {
            // 家长：显示关联的所有子女
            List<Long> studentIds = studentMapper.findIdsByParentId(userId);
            if (!studentIds.isEmpty()) {
                queryDTO.setStudentIds(studentIds);
            } else {
                return new ArrayList<>();
            }
        }
        // 管理员无限制，可查看所有学生
        return studentMapper.selectStudentList(queryDTO);
    }

    /**
     * 新增学生
     * 业务逻辑：创建用户账号（角色为STUDENT，默认密码123456），同时创建学生扩展记录
     *
     * @param dto 学生信息（真实姓名、手机号、邮箱、学号、班级ID、性别、出生日期等）
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<?> addStudent(StudentUpdateDTO dto) {
        // 1. 创建用户账号
        User user = new User();
        user.setUsername(dto.getPhone());                              // 使用手机号作为用户名
        user.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes())); // 默认密码123456，MD5加密
        user.setRealName(dto.getRealName());
        user.setRole("STUDENT");
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(1);                                            // 默认启用
        userMapper.insert(user);
        Long userId = user.getId();

        // 2. 检查手机号是否已被注册（由于上面insert后可能因唯一约束失败而回滚，此处做二次校验更安全）
        if (userMapper.findByUsername(dto.getPhone()) != null) {
            return Result.error("手机号已被注册，请更换");
        }

        // 3. 创建学生扩展记录（id与用户表id保持一致）
        Student student = new Student();
        BeanUtils.copyProperties(dto, student);
        student.setId(userId);
        studentMapper.insert(student);
        return Result.success("添加成功");
    }

    /**
     * 更新学生信息
     * 同时更新 sys_user 表（真实姓名、手机号、邮箱）和 student 表（学号、班级、性别、出生日期、家长ID）
     * 进行唯一性校验：手机号不能与其他用户重复，学号不能与其他学生重复
     *
     * @param dto 学生更新信息（必须包含学生ID）
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<?> updateStudent(StudentUpdateDTO dto) {
        // 1. 获取原学生信息和原用户信息
        Student oldStudent = studentMapper.selectById(dto.getId());
        if (oldStudent == null) {
            return Result.error("学生不存在");
        }
        User oldUser = userMapper.findById(dto.getId());

        // 2. 手机号唯一性校验（如果手机号发生变化，需检查是否已被其他用户占用）
        if (!dto.getPhone().equals(oldUser.getUsername())) {
            User existUser = userMapper.findByUsername(dto.getPhone());
            if (existUser != null) {
                return Result.error("手机号已被其他用户注册");
            }
        }

        // 3. 学号唯一性校验（如果学号发生变化，需检查是否已被其他学生使用）
        if (!dto.getStudentNumber().equals(oldStudent.getStudentNumber())) {
            Student existStu = studentMapper.findByStudentNumber(dto.getStudentNumber());
            if (existStu != null) {
                return Result.error("学号已被其他学生使用");
            }
        }

        // 4. 更新用户信息（真实姓名、手机号、邮箱）
        oldUser.setRealName(dto.getRealName());
        oldUser.setPhone(dto.getPhone());
        oldUser.setEmail(dto.getEmail());
        userMapper.update(oldUser);

        // 5. 更新学生扩展信息（学号、班级、性别、出生日期、家长ID）
        BeanUtils.copyProperties(dto, oldStudent);
        studentMapper.update(oldStudent);

        return Result.success("更新成功");
    }

    /**
     * 删除学生
     * 同时删除 sys_user 表记录和 student 表记录（物理删除）
     * 注意：需要确保没有外键约束关联（如成绩、考勤等记录应先处理，此处未做级联，调用前需保证）
     *
     * @param  studentId 学生ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<?> deleteStudent(Long studentId) {
        // 权限校验：教师只能删除自己班级的学生
        Long teacherId = UserContext.getUserId();
        Long headTeacherId = classMapper.findHeadTeacherIdByStudentId(studentId);
        if (headTeacherId == null || !headTeacherId.equals(teacherId)) {
            return Result.error("只能删除自己班级的学生");
        }
        // 1. 删除考勤记录
        attendanceMapper.deleteByStudentId(studentId);
        // 2. 删除成绩记录
        scoreMapper.deleteByStudentId(studentId);
        // 3. 删除学生记录
        int rows = studentMapper.deleteById(studentId);
        if (rows == 0) {
            return Result.error("学生不存在");
        }
        // 4. 删除关联的用户账号（可选）
        userMapper.deleteById(studentId);
        return Result.success("删除成功");
    }
}