package com.edulink.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Score {
    private Long id;
    private Long studentId;
    private String courseName;
    private BigDecimal score;
    private String examType;
    private LocalDate examDate;
    private Long teacherId;
    private LocalDateTime createTime;
}