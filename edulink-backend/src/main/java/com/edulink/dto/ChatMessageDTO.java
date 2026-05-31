package com.edulink.dto;

import com.edulink.validator.InsertGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ChatMessageDTO {

    @NotNull(message = "会话ID不能为空", groups = InsertGroup.class)
    private Long sessionId;

    @NotNull(message = "接收者ID不能为空", groups = InsertGroup.class)
    private Long receiverId;

    @NotBlank(message = "消息内容不能为空", groups = InsertGroup.class)
    private String content;

    private String messageType = "TEXT";   // 默认文本
}