package com.edulink.vo;

import lombok.Data;
import java.util.List;

@Data
public class ChildStatisticsVO {
    private List<DashboardStatisticsVO.RecentScore> recentScores; // 最近成绩列表
    private DashboardStatisticsVO.AttendanceStat attendanceStat;// 考勤统计
}