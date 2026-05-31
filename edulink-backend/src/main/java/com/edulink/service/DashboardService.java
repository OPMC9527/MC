package com.edulink.service;

import com.edulink.utils.Result;
import com.edulink.vo.DashboardStatisticsVO;

public interface DashboardService {
    Result<DashboardStatisticsVO> getStatistics();
}