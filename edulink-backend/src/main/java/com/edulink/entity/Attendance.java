package com.edulink.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Attendance {
    private Long id;
    private Long studentId;
    private LocalDate attendanceDate;
    private String status;   // PRESENT, ABSENT, LATE, LEAVE
    private String remark;
    private Long teacherId;
    private LocalDateTime createTime;
}