package com.example.studyagent.auth.service;

import com.example.studyagent.auth.dto.AuthResp;
import com.example.studyagent.auth.dto.LoginReq;
import com.example.studyagent.auth.dto.RegisterReq;

public interface AuthService {

    AuthResp register(RegisterReq request);

    AuthResp login(LoginReq request);
}
