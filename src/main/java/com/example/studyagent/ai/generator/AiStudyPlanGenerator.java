package com.example.studyagent.ai.generator;

import com.example.studyagent.ai.client.AiClient;
import com.example.studyagent.ai.prompt.StudyPlanPromptBuilder;
import com.example.studyagent.study.dto.StudyPlanCreateReq;
import com.example.studyagent.study.dto.StudyPlanResp;
import com.example.studyagent.study.dto.StudyTaskResp;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class AiStudyPlanGenerator {

    private static final int MAX_TASK_COUNT = 14;

    private final StudyPlanPromptBuilder studyPlanPromptBuilder;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    public StudyPlanResp generate(StudyPlanCreateReq request) {
        String prompt = studyPlanPromptBuilder.build(request);
        String aiResponse = aiClient.chat(prompt);
        AiStudyPlanResult aiPlan = parseAiPlan(aiResponse);

        List<AiStudyTask> aiTasks = aiPlan.tasks() == null ? Collections.emptyList() : aiPlan.tasks();
        if (aiTasks.isEmpty()) {
            throw new IllegalStateException("AI study plan tasks are empty.");
        }

        List<AiStudyTask> limitedTasks = aiTasks.stream()
                .limit(MAX_TASK_COUNT)
                .toList();

        List<StudyTaskResp> tasks = IntStream.range(0, limitedTasks.size())
                .mapToObj(index -> {
                    AiStudyTask task = limitedTasks.get(index);
                    int dayIndex = index + 1;
                    return StudyTaskResp.builder()
                            .dayIndex(dayIndex)
                            .taskDate(LocalDate.now().plusDays(dayIndex - 1L))
                            .title(task.title())
                            .content(task.content())
                            .estimatedMinutes(normalizeEstimatedMinutes(task.estimatedMinutes(), request.getDailyMinutes()))
                            .build();
                })
                .toList();

        return StudyPlanResp.builder()
                .planId(UUID.randomUUID().toString())
                .title(aiPlan.title())
                .subject(request.getSubject())
                .examDate(request.getExamDate())
                .dailyMinutes(request.getDailyMinutes())
                .targetScore(request.getTargetScore())
                .tasks(tasks)
                .build();
    }

    private int normalizeEstimatedMinutes(Integer estimatedMinutes, Integer dailyMinutes) {
        int safeDailyMinutes = dailyMinutes == null || dailyMinutes < 1 ? 60 : dailyMinutes;
        if (estimatedMinutes == null || estimatedMinutes < 1) {
            return safeDailyMinutes;
        }
        return Math.min(estimatedMinutes, safeDailyMinutes);
    }

    private AiStudyPlanResult parseAiPlan(String aiResponse) {
        try {
            String json = stripMarkdownFence(aiResponse);
            return objectMapper.readValue(json, AiStudyPlanResult.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to parse AI study plan JSON: " + exception.getMessage(), exception);
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

    private record AiStudyPlanResult(
            String title,
            List<AiStudyTask> tasks
    ) {
    }

    private record AiStudyTask(
            Integer dayIndex,
            String title,
            String content,
            Integer estimatedMinutes
    ) {
    }
}
