package com.edulink.service;

import com.edulink.dto.AttendanceDTO;
import com.edulink.dto.AttendanceQueryDTO;
import com.edulink.utils.Result;
import com.edulink.vo.AttendanceVO;

import java.util.List;

public interface AttendanceService {
    Result<AttendanceVO> getAttendanceDetail(Long id);
    Result<List<AttendanceVO>> listAttendances(AttendanceQueryDTO queryDTO);
    Result<?> checkIn(AttendanceDTO dto);
    Result<?> updateAttendance(AttendanceDTO dto);
    Result<?> deleteAttendance(Long id);
}