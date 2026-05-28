package com.example.studyagent.ai.prompt;

import com.example.studyagent.study.entity.StudyFeedbackDO;
import com.example.studyagent.study.entity.StudyPlanDO;
import com.example.studyagent.study.entity.StudyTaskDO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudySummaryPromptBuilder {

    public String build(
            StudyPlanDO plan,
            List<StudyTaskDO> tasks,
            List<StudyFeedbackDO> feedbackList,
            int completedTaskCount,
            double completionRate) {
        return """
                你是一个学习复盘助手。

                请根据学习计划、任务完成情况和用户反馈，生成一份中文阶段性学习总结。

                学习计划：
                - 科目：%s
                - 考试日期：%s
                - 每日学习时间：%d 分钟
                - 目标分数：%d
                - 已完成任务数：%d
                - 总任务数：%d
                - 完成率：%.2f%%

                任务列表：
                %s

                学习反馈：
                %s

                要求：
                1. 必须使用中文输出。
                2. summary 要像老师给学生的复盘，具体、简洁、可执行。
                3. weakPoints 提取 1 到 5 个薄弱点。
                4. suggestions 给出 2 到 5 条后续建议。
                5. 只返回 JSON，不要输出 Markdown 或额外解释。

                JSON 格式：
                {
                  "weakPoints": ["string"],
                  "summary": "string",
                  "suggestions": ["string"]
                }
                """.formatted(
                plan.getSubject(),
                plan.getExamDate(),
                plan.getDailyMinutes(),
                plan.getTargetScore(),
                completedTaskCount,
                tasks == null ? 0 : tasks.size(),
                completionRate,
                formatTasks(tasks),
                formatFeedback(feedbackList)
        );
    }

    private String formatTasks(List<StudyTaskDO> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return "暂无任务。";
        }
        StringBuilder builder = new StringBuilder();
        for (StudyTaskDO task : tasks.stream().limit(10).toList()) {
            builder.append("- 第").append(task.getDayIndex()).append("天")
                    .append("，标题：").append(task.getTitle())
                    .append("，状态：").append(task.getStatus())
                    .append("，内容：").append(compact(task.getContent(), 140))
                    .append("\n");
        }
        return builder.toString();
    }

    private String formatFeedback(List<StudyFeedbackDO> feedbackList) {
        if (feedbackList == null || feedbackList.isEmpty()) {
            return "暂无反馈。";
        }
        StringBuilder builder = new StringBuilder();
        List<StudyFeedbackDO> recentFeedback = feedbackList.size() > 8
                ? feedbackList.subList(feedbackList.size() - 8, feedbackList.size())
                : feedbackList;
        for (StudyFeedbackDO feedback : recentFeedback) {
            builder.append("- taskId=").append(feedback.getTaskId())
                    .append("，是否完成：").append(feedback.getCompleted())
                    .append("，难度：").append(feedback.getDifficulty())
                    .append("，问题：").append(compact(feedback.getProblem(), 220))
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
