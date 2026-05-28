package com.example.studyagent.auth.api;

import com.example.studyagent.auth.dto.AuthResp;
import com.example.studyagent.auth.dto.LoginReq;
import com.example.studyagent.auth.dto.RegisterReq;
import com.example.studyagent.auth.service.AuthService;
import com.example.studyagent.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<AuthResp> register(@Valid @RequestBody RegisterReq request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/login")
    public Result<AuthResp> login(@Valid @RequestBody LoginReq request) {
        return Result.success(authService.login(request));
    }
}
