package com.example.studyagent.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterReq {

    @NotBlank(message = "nickname cannot be blank")
    @Size(max = 64, message = "nickname length must be less than or equal to 64")
    private String nickname;

    @NotBlank(message = "account cannot be blank")
    @Size(max = 128, message = "account length must be less than or equal to 128")
    private String account;

    @NotBlank(message = "password cannot be blank")
    @Size(min = 6, max = 64, message = "password length must be between 6 and 64")
    private String password;
}
