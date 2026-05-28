package com.example.studyagent.ai.client.dto;

import java.util.List;

public record OpenAiChatResponse(
        List<OpenAiChoice> choices
) {
}
