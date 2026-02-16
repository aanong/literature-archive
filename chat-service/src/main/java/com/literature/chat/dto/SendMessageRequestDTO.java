package com.literature.chat.dto;

import jakarta.validation.constraints.NotBlank;

public class SendMessageRequestDTO {

    @NotBlank(message = "消息内容不能为空")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
