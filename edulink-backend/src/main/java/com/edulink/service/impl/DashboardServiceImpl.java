package com.edulink.service.impl;

import com.edulink.mapper.*;
import com.edulink.service.DashboardService;
import com.edulink.utils.Result;
import com.edulink.utils.UserContext;
import com.edulink.vo.DashboardStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 仪表盘服务实现类
 * 根据用户角色（管理员、教师、学生、家长）返回不同的统计数据和欢迎信息
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private ScoreMapper scoreMapper;
    @Autowired
    private AttendanceMapper attendanceMapper;
    @Autowired
    private NoticeMapper noticeMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 获取仪表盘统计数据
     * 根据当前登录用户的角色返回对应的数据：
     * - ADMIN / TEACHER：全局统计（学生总数、教师总数、平均成绩、今日通知数）
     * - STUDENT：个人统计（近期成绩、最近一个月考勤统计）
     * - PARENT：关联孩子的统计（子女列表、第一个孩子的近期成绩和考勤）
     *
     * @return 包含统计数据的结果对象
     */
    @Override
    public Result<DashboardStatisticsVO> getStatistics() {
        // 从线程上下文获取当前用户角色和ID
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();
        DashboardStatisticsVO vo = new DashboardStatisticsVO();
        vo.setRole(role);

        if ("ADMIN".equals(role) || "TEACHER".equals(role)) {
            // 管理员/教师：全局统计
            vo.setStudentCount(studentMapper.countTotal());                // 学生总数
            vo.setTeacherCount(userMapper.countByRole("TEACHER"));        // 教师总数
            vo.setAvgScore(scoreMapper.getAverageScore());                // 所有学生的平均成绩
            vo.setTodayNoticeCount(noticeMapper.countToday());            // 今日发布的通知数量
            vo.setWelcomeMessage("欢迎回来，系统管理员/教师");
        }
        else if ("STUDENT".equals(role)) {
            // 学生：个人统计
            Long studentId = studentMapper.findIdByUserId(userId);        // 根据用户ID查询学生扩展表ID
            if (studentId != null) {
                vo.setRecentScores(scoreMapper.findRecentScoresByStudentId(studentId, 5));   // 最近5条成绩记录
                vo.setAttendanceStat(attendanceMapper.getStatByStudentId(studentId,          // 最近一个月的考勤统计
                        LocalDate.now().minusMonths(1), LocalDate.now()));
            }
            vo.setWelcomeMessage("同学，努力学习，天天向上！");
        }
        else if ("PARENT".equals(role)) {
            // 家长：关联孩子的统计
            List<Long> childIds = studentMapper.findIdsByParentId(userId);                 // 根据家长ID查询关联的孩子ID列表
            List<DashboardStatisticsVO.ChildInfo> children = new ArrayList<>();
            for (Long childId : childIds) {
                DashboardStatisticsVO.ChildInfo info = studentMapper.getChildInfo(childId); // 获取每个孩子的基本信息
                children.add(info);
            }
            vo.setChildren(children);
            if (!childIds.isEmpty()) {
                // 简化处理：仅取第一个孩子的近期成绩和考勤（实际业务可按需展示所有孩子）
                vo.setRecentScores(scoreMapper.findRecentScoresByStudentId(childIds.get(0), 5));
                vo.setAttendanceStat(attendanceMapper.getStatByStudentId(childIds.get(0),
                        LocalDate.now().minusMonths(1), LocalDate.now()));
            }
            vo.setWelcomeMessage("家长您好，您可查看孩子的学习情况");
        }

        return Result.success(vo);
    }
}