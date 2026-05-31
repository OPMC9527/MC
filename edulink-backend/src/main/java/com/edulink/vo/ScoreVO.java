package com.edulink.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScoreVO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNumber;
    private String className;
    private String courseName;
    private BigDecimal score;
    private String examType;
    private LocalDate examDate;
    private String teacherName;
}