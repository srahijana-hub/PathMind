package com.example.studyagent.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenAiChatRequest(
        String model,
        List<OpenAiMessage> messages,
        Double temperature,
        @JsonProperty("max_tokens")
        Integer maxTokens,
        Boolean stream
) {
}
