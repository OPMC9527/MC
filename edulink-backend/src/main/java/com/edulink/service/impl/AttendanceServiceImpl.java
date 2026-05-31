package com.edulink.service.impl;

import com.edulink.dto.AttendanceDTO;
import com.edulink.dto.AttendanceQueryDTO;
import com.edulink.entity.Attendance;
import com.edulink.mapper.AttendanceMapper;
import com.edulink.mapper.StudentMapper;
import com.edulink.service.AttendanceService;
import com.edulink.utils.Result;
import com.edulink.utils.UserContext;
import com.edulink.vo.AttendanceVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 考勤服务实现类
 * 提供考勤记录的查询、签到、更新、删除等功能，并根据用户角色进行数据权限隔离
 */
@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private StudentMapper studentMapper;

    /**
     * 根据考勤记录ID获取详情
     *
     * @param id 考勤记录ID
     * @return 包含考勤详情的响应对象，不存在则返回错误信息
     */
    @Override
    public Result<AttendanceVO> getAttendanceDetail(Long id) {
        AttendanceVO vo = attendanceMapper.selectById(id);
        return vo != null ? Result.success(vo) : Result.error("考勤记录不存在");
    }

    /**
     * 条件查询考勤记录列表（支持分页，由调用方控制分页参数）
     * 根据当前登录用户角色自动设置查询范围：
     * - STUDENT：仅查询自己的考勤记录
     * - PARENT：查询其关联的所有子女的考勤记录
     * - ADMIN / TEACHER：查询所有记录（无额外限制）
     *
     * @param queryDTO 查询条件对象（包含学生ID、班级、日期范围、状态等）
     * @return 包含考勤列表的响应对象
     */
    @Override
    public Result<List<AttendanceVO>> listAttendances(AttendanceQueryDTO queryDTO) {
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();

        // 根据角色设置查询条件
        if ("STUDENT".equals(role)) {
            // 学生：仅查询自己的考勤记录
            Long studentId = studentMapper.findIdByUserId(userId);
            if (studentId == null) {
                return Result.success(new ArrayList<>());
            }
            queryDTO.setStudentId(studentId);
        } else if ("PARENT".equals(role)) {
            // 家长：查询所有关联子女的考勤记录
            List<Long> studentIds = studentMapper.findIdsByParentId(userId);
            if (studentIds.isEmpty()) {
                return Result.success(new ArrayList<>());
            }
            queryDTO.setStudentIds(studentIds);
        }
        // 管理员和教师不做额外限制，可查看所有考勤记录

        List<AttendanceVO> list = attendanceMapper.selectList(queryDTO);
        return Result.success(list);
    }

    /**
     * 签到（新增考勤记录）
     * 自动填充教师ID（当前登录用户），并检查是否重复打卡
     *
     * @param dto 考勤数据（学生ID、考勤日期、状态等）
     * @return 操作结果
     */
    @Override
    public Result<?> checkIn(AttendanceDTO dto) {
        // 自动填充 teacherId（记录考勤的教师）
        if (dto.getTeacherId() == null) {
            Long currentUserId = UserContext.getUserId();
            if (currentUserId == null) {
                return Result.error("未登录，无法获取教师信息");
            }
            dto.setTeacherId(currentUserId);
        }
        // 检查重复打卡：同一学生同一日期只能有一条考勤记录
        Attendance exist = attendanceMapper.selectByStudentAndDate(dto.getStudentId(), dto.getAttendanceDate());
        if (exist != null) {
            return Result.error("今日已打卡，请勿重复");
        }
        Attendance attendance = new Attendance();
        BeanUtils.copyProperties(dto, attendance);
        attendanceMapper.insert(attendance);
        return Result.success("打卡成功");
    }

    /**
     * 更新考勤记录
     *
     * @param dto 考勤数据（必须包含ID）
     * @return 操作结果
     */
    @Override
    public Result<?> updateAttendance(AttendanceDTO dto) {
        if (dto.getId() == null) {
            return Result.error("考勤ID不能为空");
        }
        Attendance attendance = new Attendance();
        BeanUtils.copyProperties(dto, attendance);
        attendanceMapper.update(attendance);
        return Result.success("更新成功");
    }

    /**
     * 删除考勤记录
     *
     * @param id 考勤记录ID
     * @return 操作结果
     */
    @Override
    public Result<?> deleteAttendance(Long id) {
        attendanceMapper.deleteById(id);
        return Result.success("删除成功");
    }
}