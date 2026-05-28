package com.example.studyagent.ai.prompt;

import com.example.studyagent.study.entity.StudyFeedbackDO;
import com.example.studyagent.study.entity.StudyPlanDO;
import com.example.studyagent.study.entity.StudyTaskDO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudyPlanAdjustPromptBuilder {

    public String build(StudyPlanDO plan, List<StudyTaskDO> pendingTasks, List<StudyFeedbackDO> feedbackList) {
        return """
                You are an adaptive study planning assistant.

                Adjust only the pending tasks based on the learner's feedback.

                Study plan:
                - Subject: %s
                - Exam date: %s
                - Daily study time: %d minutes
                - Target score: %d

                Pending tasks to adjust:
                %s

                Recent learner feedback:
                %s

                Rules:
                1. Only adjust the tasks listed above.
                2. Keep the same taskId for each adjusted task.
                3. Make tasks more specific and focused on weak points mentioned in feedback.
                4. estimatedMinutes must not exceed the daily study time.
                5. Return JSON only. Do not include markdown or extra explanation.

                JSON format:
                {
                  "tasks": [
                    {
                      "taskId": 1,
                      "title": "string",
                      "content": "string",
                      "estimatedMinutes": 60
                    }
                  ]
                }
                """.formatted(
                plan.getSubject(),
                plan.getExamDate(),
                plan.getDailyMinutes(),
                plan.getTargetScore(),
                formatTasks(pendingTasks),
                formatFeedback(feedbackList)
        );
    }

    private String formatTasks(List<StudyTaskDO> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return "No pending tasks.";
        }
        StringBuilder builder = new StringBuilder();
        for (StudyTaskDO task : tasks.stream().limit(5).toList()) {
            builder.append("- taskId=").append(task.getId())
                    .append(", dayIndex=").append(task.getDayIndex())
                    .append(", title=").append(task.getTitle())
                    .append(", content=").append(compact(task.getContent(), 160))
                    .append(", estimatedMinutes=").append(task.getEstimatedMinutes())
                    .append("\n");
        }
        return builder.toString();
    }

    private String formatFeedback(List<StudyFeedbackDO> feedbackList) {
        if (feedbackList == null || feedbackList.isEmpty()) {
            return "No feedback yet.";
        }
        StringBuilder builder = new StringBuilder();
        List<StudyFeedbackDO> recentFeedback = feedbackList.size() > 5
                ? feedbackList.subList(feedbackList.size() - 5, feedbackList.size())
                : feedbackList;
        for (StudyFeedbackDO feedback : recentFeedback) {
            builder.append("- taskId=").append(feedback.getTaskId())
                    .append(", completed=").append(feedback.getCompleted())
                    .append(", difficulty=").append(feedback.getDifficulty())
                    .append(", problem=").append(compact(feedback.getProblem(), 240))
                    .append("\n");
        }
        return builder.toString();
    }

    private String compact(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(normalized.length() - maxLength);
    }
}
