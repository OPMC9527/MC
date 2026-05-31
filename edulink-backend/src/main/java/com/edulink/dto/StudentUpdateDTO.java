package com.edulink.dto;

import com.edulink.validator.InsertGroup;
import com.edulink.validator.UpdateGroup;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class StudentUpdateDTO {

    @NotNull(message = "ID不能为空", groups = UpdateGroup.class)
    private Long id;

    @NotBlank(message = "姓名不能为空", groups = {InsertGroup.class, UpdateGroup.class})
    private String realName;

    @NotBlank(message = "手机号不能为空", groups = InsertGroup.class)
    private String phone;

    private String email;

    @NotBlank(message = "学号不能为空", groups = InsertGroup.class)
    private String studentNumber;

    @NotNull(message = "班级ID不能为空", groups = InsertGroup.class)
    private Long classId;

    private String gender;
    private LocalDate birthDate;
    private Long parentId;
}