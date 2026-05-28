package com.example.studyagent.ai.prompt;

import com.example.studyagent.study.dto.StudyPlanCreateReq;
import org.springframework.stereotype.Component;

@Component
public class StudyPlanPromptBuilder {

    public String build(StudyPlanCreateReq request) {
        return """
                You are a study planning assistant.

                Create a practical study plan based on the user's information.

                User information:
                - Subject: %s
                - Exam date: %s
                - Current level: %s
                - Daily study time: %d minutes
                - Target score: %d

                Rules:
                1. Generate at most 14 daily tasks. Focus on the next 14 days.
                2. Each day's tasks must fit within the user's daily study time.
                3. Tasks should be specific and executable.
                4. The plan should include review, practice, and summary.
                5. Return JSON only. Do not include markdown or extra explanation.

                JSON format:
                {
                  "title": "string",
                  "tasks": [
                    {
                      "dayIndex": 1,
                      "title": "string",
                      "content": "string",
                      "estimatedMinutes": 60
                    }
                  ]
                }
                """.formatted(
                request.getSubject(),
                request.getExamDate(),
                request.getCurrentLevel(),
                request.getDailyMinutes(),
                request.getTargetScore()
        );
    }
}
