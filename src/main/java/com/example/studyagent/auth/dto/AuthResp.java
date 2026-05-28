package com.example.studyagent.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResp {

    private String token;
    private String userId;
    private String nickname;
    private String account;
}
