package com.example.studyagent.ai.client;

import com.example.studyagent.ai.client.dto.OpenAiChatRequest;
import com.example.studyagent.ai.client.dto.OpenAiChatResponse;
import com.example.studyagent.ai.client.dto.OpenAiMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

@Component
public class OpenAiCompatibleAiClient implements AiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${study-agent.ai.base-url}")
    private String baseUrl;

    @Value("${study-agent.ai.api-key:}")
    private String apiKey;

    @Value("${study-agent.ai.model}")
    private String model;

    @Value("${study-agent.ai.chat-model:${study-agent.ai.model}}")
    private String chatModel;

    public OpenAiCompatibleAiClient(RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public String chat(String prompt) {
        return chat(prompt, new AiChatOptions(null, null, 0.7, false));
    }

    @Override
    public String chat(String prompt, AiChatOptions options) {
        AiChatOptions actualOptions = normalizeOptions(options);
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("AI api key is missing. Please set STUDY_AGENT_AI_API_KEY.");
        }

        OpenAiChatRequest request = new OpenAiChatRequest(
                resolveModel(actualOptions),
                List.of(new OpenAiMessage("user", prompt)),
                actualOptions.temperature(),
                actualOptions.maxTokens(),
                false
        );

        OpenAiChatResponse response = restClient.post()
                .uri(baseUrl + "/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(request)
                .retrieve()
                .body(OpenAiChatResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("AI response is empty.");
        }
        OpenAiMessage message = response.choices().get(0).message();
        if (message == null || !StringUtils.hasText(message.content())) {
            throw new IllegalStateException("AI response message is empty.");
        }
        return message.content();
    }

    @Override
    public void streamChat(String prompt, AiChatOptions options, Consumer<String> onChunk) {
        AiChatOptions actualOptions = normalizeOptions(options);
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("AI api key is missing. Please set STUDY_AGENT_AI_API_KEY.");
        }

        try {
            OpenAiChatRequest requestBody = new OpenAiChatRequest(
                    resolveModel(actualOptions),
                    List.of(new OpenAiMessage("user", prompt)),
                    actualOptions.temperature(),
                    actualOptions.maxTokens(),
                    true
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofSeconds(90))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<java.io.InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() >= 400) {
                String error = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new IllegalStateException("AI stream request failed: " + error);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String data = line.substring("data:".length()).trim();
                    if ("[DONE]".equals(data)) {
                        break;
                    }
                    String content = extractStreamContent(data);
                    if (StringUtils.hasText(content)) {
                        onChunk.accept(content);
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private AiChatOptions normalizeOptions(AiChatOptions options) {
        return options == null ? new AiChatOptions(null, null, 0.7, false) : options;
    }

    private String resolveModel(AiChatOptions options) {
        if (StringUtils.hasText(options.model())) {
            return options.model();
        }
        if (options.useChatModel() && StringUtils.hasText(chatModel)) {
            return chatModel;
        }
        return model;
    }

    private String extractStreamContent(String data) throws Exception {
        JsonNode root = objectMapper.readTree(data);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return "";
        }
        JsonNode choice = choices.get(0);
        JsonNode deltaContent = choice.path("delta").path("content");
        if (!deltaContent.isMissingNode()) {
            return deltaContent.asText("");
        }
        JsonNode messageContent = choice.path("message").path("content");
        return messageContent.isMissingNode() ? "" : messageContent.asText("");
    }
}
