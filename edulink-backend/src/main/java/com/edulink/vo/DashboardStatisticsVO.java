package com.edulink.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardStatisticsVO {
    // 通用字段
    private String role;
    private String welcomeMessage;

    // 管理员/教师字段
    private Long studentCount;
    private Long teacherCount;
    private BigDecimal avgScore;
    private Integer todayNoticeCount;

    // 家长/学生字段
    private List<ChildInfo> children;        // 家长：孩子列表
    private List<RecentScore> recentScores;  // 最近成绩
    private AttendanceStat attendanceStat;   // 考勤统计

    @Data
    public static class ChildInfo {
        private Long id;
        private String name;
        private String className;
    }

    @Data
    public static class RecentScore {
        private String courseName;
        private BigDecimal score;
        private String examDate;
    }

    @Data
    public static class AttendanceStat {
        private Integer presentDays;
        private Integer lateDays;
        private Integer absentDays;
        private Integer leaveDays;
    }
}