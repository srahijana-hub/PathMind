package com.example.studyagent.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginReq {

    @NotBlank(message = "account cannot be blank")
    private String account;

    @NotBlank(message = "password cannot be blank")
    private String password;
}
