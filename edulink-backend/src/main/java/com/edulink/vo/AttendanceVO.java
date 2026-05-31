package com.edulink.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AttendanceVO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNumber;
    private String className;
    private LocalDate attendanceDate;
    private String status;
    private String remark;
    private String teacherName;
}