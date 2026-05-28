package com.example.studyagent.ai.client;

public record AiChatOptions(
        String model,
        Integer maxTokens,
        Double temperature,
        boolean useChatModel
) {

    public static AiChatOptions dailyChat() {
        return new AiChatOptions(null, 1024, 0.4, true);
    }
}
