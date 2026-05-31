package com.edulink.dto;

import com.edulink.validator.InsertGroup;
import com.edulink.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class AttendanceDTO {

    @NotNull(message = "考勤ID不能为空", groups = UpdateGroup.class)
    private Long id;

    @NotNull(message = "学生ID不能为空", groups = InsertGroup.class)
    private Long studentId;

    @NotNull(message = "考勤日期不能为空", groups = InsertGroup.class)
    private LocalDate attendanceDate;

    @NotBlank(message = "考勤状态不能为空", groups = InsertGroup.class)
    @Pattern(regexp = "PRESENT|ABSENT|LATE|LEAVE", message = "状态必须是 PRESENT/ABSENT/LATE/LEAVE")
    private String status;

    @Size(max = 200, message = "备注不能超过200字")
    private String remark;

    private Long teacherId;    // 后端自动填充
}