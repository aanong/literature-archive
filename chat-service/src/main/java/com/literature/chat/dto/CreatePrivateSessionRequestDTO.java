package com.literature.chat.dto;

import jakarta.validation.constraints.NotBlank;

public class CreatePrivateSessionRequestDTO {

    @NotBlank(message = "目标用户名不能为空")
    private String peerUsername;

    public String getPeerUsername() {
        return peerUsername;
    }

    public void setPeerUsername(String peerUsername) {
        this.peerUsername = peerUsername;
    }
}
