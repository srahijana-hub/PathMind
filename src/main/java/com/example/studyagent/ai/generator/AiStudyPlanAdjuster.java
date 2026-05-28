package com.example.studyagent.ai.generator;

import com.example.studyagent.ai.client.AiClient;
import com.example.studyagent.ai.prompt.StudyPlanAdjustPromptBuilder;
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
public class AiStudyPlanAdjuster {

    private final StudyPlanAdjustPromptBuilder studyPlanAdjustPromptBuilder;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    public List<AdjustedTask> adjust(StudyPlanDO plan, List<StudyTaskDO> pendingTasks, List<StudyFeedbackDO> feedbackList) {
        String prompt = studyPlanAdjustPromptBuilder.build(plan, pendingTasks, feedbackList);
        String aiResponse = aiClient.chat(prompt);
        AiAdjustResult result = parseAdjustResult(aiResponse);
        return result.tasks() == null ? Collections.emptyList() : result.tasks();
    }

    private AiAdjustResult parseAdjustResult(String aiResponse) {
        try {
            return objectMapper.readValue(stripMarkdownFence(aiResponse), AiAdjustResult.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to parse AI adjusted study plan JSON: " + exception.getMessage(), exception);
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
    private record AiAdjustResult(List<AdjustedTask> tasks) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdjustedTask(
            Long taskId,
            String title,
            String content,
            Integer estimatedMinutes
    ) {
    }
}
