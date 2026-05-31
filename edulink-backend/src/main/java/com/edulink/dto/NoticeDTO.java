package com.edulink.dto;

import com.edulink.validator.InsertGroup;
import com.edulink.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.*;

@Data
public class NoticeDTO {

    @NotNull(message = "通知ID不能为空", groups = UpdateGroup.class)
    private Long id;

    @NotBlank(message = "标题不能为空", groups = InsertGroup.class)
    @Size(max = 200, message = "标题不能超过200字")
    private String title;

    @NotBlank(message = "内容不能为空", groups = InsertGroup.class)
    private String content;

    @NotBlank(message = "目标角色不能为空", groups = InsertGroup.class)
    private String targetRoles;   // JSON 字符串，如 "[\"STUDENT\",\"TEACHER\"]"

    @Min(value = 0, message = "紧急程度只能是0或1")
    @Max(value = 1, message = "紧急程度只能是0或1")
    private Integer isUrgent;     // 0普通 1紧急
}