package com.edulink.dto;

import com.edulink.validator.InsertGroup;
import com.edulink.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScoreSaveDTO {

    @NotNull(message = "成绩ID不能为空", groups = UpdateGroup.class)
    private Long id;

    @NotNull(message = "学生ID不能为空", groups = InsertGroup.class)
    private Long studentId;

    @NotBlank(message = "课程名称不能为空", groups = InsertGroup.class)
    @Size(max = 100, message = "课程名称长度不能超过100")
    private String courseName;

    @NotNull(message = "分数不能为空", groups = InsertGroup.class)
    @DecimalMin(value = "0.0", inclusive = true, message = "分数不能低于0")
    @DecimalMax(value = "100.0", inclusive = true, message = "分数不能高于100")
    private BigDecimal score;

    @NotBlank(message = "考试类型不能为空", groups = InsertGroup.class)
    @Pattern(regexp = "期中|期末|平时|单元测试", message = "考试类型不合法")
    private String examType;   // 期中/期末/平时

    @NotNull(message = "考试日期不能为空", groups = InsertGroup.class)
    private LocalDate examDate;

    private Long teacherId;    // 可由后端自动填充，前端可不传
}