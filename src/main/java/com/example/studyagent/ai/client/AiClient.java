//接口，不管接入什么大模型，对业务层来说只需要调用....

package com.example.studyagent.ai.client;

import java.util.function.Consumer;

public interface AiClient {

    String chat(String prompt);

    default String chat(String prompt, AiChatOptions options) {
        return chat(prompt);
    }

    default void streamChat(String prompt, AiChatOptions options, Consumer<String> onChunk) {
        onChunk.accept(chat(prompt, options));
    }
}
