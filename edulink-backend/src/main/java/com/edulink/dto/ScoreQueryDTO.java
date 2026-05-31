package com.edulink.dto;

import lombok.Data;
import javax.validation.constraints.Min;
import java.util.List;

@Data
public class ScoreQueryDTO {
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页条数最小为1")
    private Integer pageSize = 10;

    private Long studentId;
    private List<Long> studentIds;  // 家长查询多个孩子时使用
    private String studentName;
    private String className;
    private String courseName;
    private String examType;
    private Long classId;
}