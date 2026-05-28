package com.example.studyagent.ai.generator;

import com.example.studyagent.ai.client.AiClient;
import com.example.studyagent.ai.prompt.StudySummaryPromptBuilder;
import com.example.studyagent.study.entity.StudyFeedbackDO;
import com.example.studyagent.study.entity.StudyPlanDO;
import com.example.studyagent.study.entity.StudyTaskDO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AiStudySummaryGenerator {

    private final StudySummaryPromptBuilder studySummaryPromptBuilder;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    public AiSummaryResult generate(
            StudyPlanDO plan,
            List<StudyTaskDO> tasks,
            List<StudyFeedbackDO> feedbackList,
            int completedTaskCount,
            double completionRate) {
        String prompt = studySummaryPromptBuilder.build(plan, tasks, feedbackList, completedTaskCount, completionRate);
        String aiResponse = aiClient.chat(prompt);
        return parseSummary(aiResponse);
    }

    private AiSummaryResult parseSummary(String aiResponse) {
        try {
            AiSummaryResult result = objectMapper.readValue(stripMarkdownFence(aiResponse), AiSummaryResult.class);
            return new AiSummaryResult(
                    result.weakPoints() == null ? Collections.emptyList() : result.weakPoints(),
                    result.summary(),
                    result.suggestions() == null ? Collections.emptyList() : result.suggestions()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to parse AI study summary JSON: " + exception.getMessage(), exception);
        }
    }

    private String stripMarkdownFence(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("```json") && trimmed.endsWith("```")) {
            return trimmed.substring(7, trimmed.length() - 3).trim();
        }
        if (trimmed.startsWith("```") && trimmed.endsWith("```")) {
            return trimmed.substring(3, trimmed.length() - 3).trim();
        }
        return trimmed;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AiSummaryResult(
            List<String> weakPoints,
            String summary,
            List<String> suggestions
    ) {
    }
}
