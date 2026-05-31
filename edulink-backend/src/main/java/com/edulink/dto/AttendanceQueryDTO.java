package com.edulink.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AttendanceQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Long classId;          // 班级ID
    private String studentName;    // 学生姓名（模糊查询）
    private Long studentId;        // 学生ID（精确，用于数据隔离）
    private List<Long> studentIds;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

}