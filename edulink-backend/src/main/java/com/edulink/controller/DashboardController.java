package com.edulink.controller;

import com.edulink.mapper.AttendanceMapper;
import com.edulink.mapper.ScoreMapper;
import com.edulink.service.DashboardService;
import com.edulink.utils.Result;
import com.edulink.vo.ChildStatisticsVO;
import com.edulink.vo.DashboardStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 仪表盘控制器
 * 提供数据统计汇总接口，用于后台首页展示关键指标
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private ScoreMapper scoreMapper;
    @Autowired
    private AttendanceMapper attendanceMapper;

    /**
     * 获取系统统计数据
     * 通常包括：用户总数、学生数、教师数、班级数、课程数等关键指标
     *
     * @return 统计数据结果对象（具体结构由DashboardService实现决定）
     */
    @GetMapping("/statistics")
    public Result<?> getStatistics() {
        return dashboardService.getStatistics();
    }

    @GetMapping("/child/{childId}")
    public Result<ChildStatisticsVO> getChildStatistics(@PathVariable Long childId) {
        // 1. 查询最近5条成绩
        List<DashboardStatisticsVO.RecentScore> recentScores = scoreMapper.findRecentScoresByStudentId(childId, 5);
        // 2. 查询近30天考勤统计
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();
        DashboardStatisticsVO.AttendanceStat attendanceStat = attendanceMapper.getStatByStudentId(childId, startDate, endDate);

        // 3. 封装返回
        ChildStatisticsVO vo = new ChildStatisticsVO();
        vo.setRecentScores(recentScores);
        vo.setAttendanceStat(attendanceStat);
        return Result.success(vo);
    }
}