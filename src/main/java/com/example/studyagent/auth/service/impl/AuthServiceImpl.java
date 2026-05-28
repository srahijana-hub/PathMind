package com.example.studyagent.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.studyagent.auth.dto.AuthResp;
import com.example.studyagent.auth.dto.LoginReq;
import com.example.studyagent.auth.dto.RegisterReq;
import com.example.studyagent.auth.entity.StudyUserDO;
import com.example.studyagent.auth.mapper.StudyUserMapper;
import com.example.studyagent.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final StudyUserMapper studyUserMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public AuthResp register(RegisterReq request) {
        StudyUserDO existingUser = findByAccount(request.getAccount());
        if (existingUser != null) {
            throw new IllegalArgumentException("account already exists");
        }

        StudyUserDO user = new StudyUserDO();
        user.setUserId(UUID.randomUUID().toString());
        user.setNickname(request.getNickname().trim());
        user.setAccount(request.getAccount().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE");
        studyUserMapper.insert(user);

        return toAuthResp(user);
    }

    @Override
    public AuthResp login(LoginReq request) {
        StudyUserDO user = findByAccount(request.getAccount());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("account or password is incorrect");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException("account is disabled");
        }
        return toAuthResp(user);
    }

    private StudyUserDO findByAccount(String account) {
        return studyUserMapper.selectOne(new LambdaQueryWrapper<StudyUserDO>()
                .eq(StudyUserDO::getAccount, account == null ? "" : account.trim())
                .last("LIMIT 1"));
    }

    private AuthResp toAuthResp(StudyUserDO user) {
        return AuthResp.builder()
                .token(UUID.randomUUID().toString())
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .account(user.getAccount())
                .build();
    }
}
