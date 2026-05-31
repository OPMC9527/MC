package com.edulink.dto;

import lombok.Data;
import javax.validation.constraints.Min;
import java.util.List;

@Data
public class StudentQueryDTO {

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页条数最小为1")
    private Integer pageSize = 10;

    private String className;      // 班级名称模糊查询
    private String studentName;    // 学生姓名模糊查询
    private String studentNumber;  // 学号精确查询
    private Boolean exactMatch = false; // 默认模糊查询
    private List<Long> classIds;
    // 数据隔离字段（用于学生/家长角色过滤）
    private Long studentId;        // 单个学生ID（学生查看自己）
    private List<Long> studentIds; // 多个学生ID（家长查看所有孩子）
}