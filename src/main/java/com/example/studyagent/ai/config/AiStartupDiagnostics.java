package com.example.studyagent.ai.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class AiStartupDiagnostics {

    @Value("${study-agent.ai.enabled:false}")
    private boolean enabled;

    @Value("${study-agent.ai.base-url:}")
    private String baseUrl;

    @Value("${study-agent.ai.api-key:}")
    private String apiKey;

    @Value("${study-agent.ai.model:}")
    private String model;

    @PostConstruct
    public void logAiConfig() {
        log.info("AI config loaded: enabled={}, apiKeyConfigured={}, model={}, baseUrl={}",
                enabled,
                StringUtils.hasText(apiKey),
                model,
                baseUrl);
    }
}
