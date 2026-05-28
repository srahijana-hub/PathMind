package com.example.studyagent.study.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.studyagent.ai.client.AiChatOptions;
import com.example.studyagent.ai.client.AiClient;
import com.example.studyagent.ai.generator.AiStudyPlanAdjuster;
import com.example.studyagent.ai.generator.AiStudyPlanGenerator;
import com.example.studyagent.ai.generator.AiStudySummaryGenerator;
import com.example.studyagent.study.dto.StudyAssistantChatReq;
import com.example.studyagent.study.dto.StudyAssistantChatResp;
import com.example.studyagent.study.dto.StudyAssistantFinishReq;
import com.example.studyagent.study.dto.StudyAssistantFinishResp;
import com.example.studyagent.study.dto.StudyFeedbackReq;
import com.example.studyagent.study.dto.StudyFeedbackResp;
import com.example.studyagent.study.dto.StudyPlanAdjustResp;
import com.example.studyagent.study.dto.StudyPlanCreateReq;
import com.example.studyagent.study.dto.StudyPlanListItemResp;
import com.example.studyagent.study.dto.StudyPlanResp;
import com.example.studyagent.study.dto.StudySummaryResp;
import com.example.studyagent.study.dto.StudySummaryReportResp;
import com.example.studyagent.study.dto.StudyTaskResp;
import com.example.studyagent.study.entity.StudyFeedbackDO;
import com.example.studyagent.study.entity.StudyPlanDO;
import com.example.studyagent.study.entity.StudySummaryReportDO;
import com.example.studyagent.study.entity.StudyTaskDO;
import com.example.studyagent.study.mapper.StudyFeedbackMapper;
import com.example.studyagent.study.mapper.StudyPlanMapper;
import com.example.studyagent.study.mapper.StudySummaryReportMapper;
import com.example.studyagent.study.mapper.StudyTaskMapper;
import com.example.studyagent.study.service.StudyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyPlanServiceImpl implements StudyPlanService {

    private final AiStudyPlanGenerator aiStudyPlanGenerator;
    private final AiStudyPlanAdjuster aiStudyPlanAdjuster;
    private final AiStudySummaryGenerator aiStudySummaryGenerator;
    private final AiClient aiClient;
    private final StudyPlanMapper studyPlanMapper;
    private final StudyTaskMapper studyTaskMapper;
    private final StudyFeedbackMapper studyFeedbackMapper;
    private final StudySummaryReportMapper studySummaryReportMapper;

    @Value("${study-agent.ai.enabled:false}")
    private boolean aiEnabled;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyPlanResp createPlan(StudyPlanCreateReq request) {
        StudyPlanResp plan = aiEnabled ? aiStudyPlanGenerator.generate(request) : buildDemoPlan(request);
        savePlan(request, plan);
        saveTasks(plan);
        return plan;
    }

    @Override
    public StudyPlanResp getPlanDetail(String planId) {
        StudyPlanDO plan = requirePlan(planId);
        List<StudyTaskResp> tasks = listTaskEntities(planId).stream()
                .map(this::toTaskResp)
                .toList();
        return toPlanResp(plan, tasks);
    }

    @Override
    public List<StudyPlanListItemResp> listPlans() {
        List<StudyPlanDO> plans = studyPlanMapper.selectList(new LambdaQueryWrapper<StudyPlanDO>()
                .orderByDesc(StudyPlanDO::getCreatedAt));
        return plans.stream()
                .map(this::toPlanListItemResp)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyFeedbackResp submitFeedback(String planId, StudyFeedbackReq request) {
        requirePlan(planId);
        StudyTaskDO task = requireTask(planId, request.getTaskId());

        StudyFeedbackDO feedback = new StudyFeedbackDO();
        feedback.setPlanId(planId);
        feedback.setTaskId(task.getId());
        feedback.setCompleted(request.getCompleted());
        feedback.setDifficulty(request.getDifficulty());
        feedback.setProblem(request.getProblem());
        studyFeedbackMapper.insert(feedback);

        String taskStatus = Boolean.TRUE.equals(request.getCompleted()) ? "DONE" : "TODO";
        task.setStatus(taskStatus);
        studyTaskMapper.updateById(task);

        return StudyFeedbackResp.builder()
                .feedbackId(feedback.getId())
                .planId(planId)
                .taskId(task.getId())
                .completed(request.getCompleted())
                .difficulty(request.getDifficulty())
                .problem(request.getProblem())
                .taskStatus(taskStatus)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyPlanAdjustResp adjustPlan(String planId) {
        StudyPlanDO plan = requirePlan(planId);
        List<StudyTaskDO> pendingTasks = studyTaskMapper.selectList(new LambdaQueryWrapper<StudyTaskDO>()
                .eq(StudyTaskDO::getPlanId, planId)
                .ne(StudyTaskDO::getStatus, "DONE")
                .orderByAsc(StudyTaskDO::getDayIndex)
                .last("LIMIT 5"));
        List<StudyFeedbackDO> feedbackList = listFeedback(planId);

        if (pendingTasks.isEmpty()) {
            return StudyPlanAdjustResp.builder()
                    .planId(planId)
                    .adjustedCount(0)
                    .adjustedTasks(List.of())
                    .build();
        }

        List<StudyTaskDO> adjustedTasks = aiEnabled
                ? applyAiAdjustment(plan, pendingTasks, feedbackList)
                : applyLocalAdjustment(plan, pendingTasks, feedbackList);

        for (StudyTaskDO task : adjustedTasks) {
            studyTaskMapper.updateById(task);
        }

        return StudyPlanAdjustResp.builder()
                .planId(planId)
                .adjustedCount(adjustedTasks.size())
                .adjustedTasks(adjustedTasks.stream().map(this::toTaskResp).toList())
                .build();
    }

    @Override
    public StudySummaryResp getSummary(String planId) {
        requirePlan(planId);
        List<StudyTaskDO> tasks = listTaskEntities(planId);
        List<StudyFeedbackDO> feedbackList = listFeedback(planId);

        int totalTaskCount = tasks.size();
        int completedTaskCount = (int) tasks.stream()
                .filter(task -> "DONE".equals(task.getStatus()))
                .count();
        double completionRate = calculateCompletionRate(completedTaskCount, totalTaskCount);
        List<String> weakPoints = extractWeakPoints(feedbackList);
        String summary = buildSummaryText(completedTaskCount, totalTaskCount, completionRate, weakPoints);
        List<String> suggestions = buildSuggestions(completionRate, weakPoints);

        if (aiEnabled) {
            try {
                AiStudySummaryGenerator.AiSummaryResult aiSummary = aiStudySummaryGenerator.generate(
                        requirePlan(planId), tasks, feedbackList, completedTaskCount, completionRate);
                if (StringUtils.hasText(aiSummary.summary())) {
                    summary = aiSummary.summary();
                }
                if (aiSummary.weakPoints() != null && !aiSummary.weakPoints().isEmpty()) {
                    weakPoints = aiSummary.weakPoints();
                }
                if (aiSummary.suggestions() != null && !aiSummary.suggestions().isEmpty()) {
                    suggestions = aiSummary.suggestions();
                }
            } catch (Exception ignored) {
                // Keep local summary as a fallback when AI summary generation fails.
            }
        }

        return StudySummaryResp.builder()
                .planId(planId)
                .completedTaskCount(completedTaskCount)
                .totalTaskCount(totalTaskCount)
                .completionRate(completionRate)
                .weakPoints(weakPoints)
                .summary(summary)
                .suggestions(suggestions)
                .build();
    }

    @Override
    public StudyAssistantChatResp chatWithAssistant(String planId, StudyAssistantChatReq request) {
        StudyPlanDO plan = requirePlan(planId);
        List<StudyTaskDO> tasks = listTaskEntities(planId);

        String answer = buildAssistantAnswer(plan, tasks, request.getMessage(), request.getConversationText());
        return StudyAssistantChatResp.builder()
                .answer(answer)
                .latestSummary(null)
                .reports(List.of())
                .build();
    }

    @Override
    public SseEmitter streamAssistantChat(String planId, StudyAssistantChatReq request) {
        SseEmitter emitter = new SseEmitter(90_000L);
        CompletableFuture.runAsync(() -> {
            try {
                StudyPlanDO plan = requirePlan(planId);
                List<StudyTaskDO> tasks = listTaskEntities(planId);
                if (!aiEnabled) {
                    sendSseChunk(emitter, "message", "我已经收到你的问题。建议先完成最近一个未完成任务，再记录具体卡点，之后点击结束对话并更新计划。");
                    sendSseChunk(emitter, "done", "[DONE]");
                    emitter.complete();
                    return;
                }
                String prompt = buildAssistantPrompt(plan, tasks, request.getMessage(), request.getConversationText());
                aiClient.streamChat(prompt, AiChatOptions.dailyChat(), chunk -> sendSseChunk(emitter, "message", chunk));
                sendSseChunk(emitter, "done", "[DONE]");
                emitter.complete();
            } catch (Exception e) {
                try {
                    sendSseChunk(emitter, "error", StringUtils.hasText(e.getMessage()) ? e.getMessage() : "AI 助手暂时不可用");
                } catch (Exception ignored) {
                    // Ignore secondary send failures.
                }
                emitter.complete();
            }
        });
        return emitter;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyAssistantFinishResp finishAssistantConversation(String planId, StudyAssistantFinishReq request) {
        requirePlan(planId);
        StudyFeedbackDO feedback = new StudyFeedbackDO();
        feedback.setPlanId(planId);
        feedback.setCompleted(false);
        feedback.setDifficulty("CONVERSATION");
        feedback.setProblem(request.getConversationText());
        studyFeedbackMapper.insert(feedback);

        StudySummaryResp latestSummary = getSummary(planId);

        StudySummaryReportDO report = new StudySummaryReportDO();
        report.setPlanId(planId);
        report.setUserQuestion(request.getConversationText());
        report.setAiAnswer("本次对话已结束，系统已根据对话内容更新学习总结。");
        report.setSummary(latestSummary.getSummary());
        report.setSuggestions(String.join("\n", latestSummary.getSuggestions()));
        studySummaryReportMapper.insert(report);

        return StudyAssistantFinishResp.builder()
                .latestSummary(latestSummary)
                .reports(listSummaryReports(planId))
                .build();
    }

    @Override
    public List<StudySummaryReportResp> listSummaryReports(String planId) {
        requirePlan(planId);
        return studySummaryReportMapper.selectList(new LambdaQueryWrapper<StudySummaryReportDO>()
                        .eq(StudySummaryReportDO::getPlanId, planId)
                        .orderByDesc(StudySummaryReportDO::getCreatedAt))
                .stream()
                .map(this::toSummaryReportResp)
                .toList();
    }

    private String buildAssistantAnswer(StudyPlanDO plan, List<StudyTaskDO> tasks, String message, String conversationText) {
        if (!aiEnabled) {
            return "我已经记录了你的问题：“" + message + "”。建议先对照当前计划中未完成的任务，标记最卡住的知识点，再点击智能调整让系统重新安排优先级。";
        }
        return aiClient.chat(buildAssistantPrompt(plan, tasks, message, conversationText), AiChatOptions.dailyChat());
    }

    private String buildAssistantPrompt(StudyPlanDO plan, List<StudyTaskDO> tasks, String message, String conversationText) {
        String taskText = tasks.stream()
                .filter(task -> !"DONE".equals(task.getStatus()))
                .limit(3)
                .map(task -> task.getDayIndex() + ". " + task.getTitle() + "（" + task.getStatus() + "）：" + task.getContent())
                .collect(Collectors.joining("\n"));
        String prompt = """
                你是一个有温度的学习伙伴，用中文和学生聊天。语气自然亲切，像朋友一样，不要太官方。
                认真分析学生说的话，结合对话历史给出有针对性的回答。

                背景：科目 %s，考试日期 %s，每天学习 %d 分钟。
                未完成任务：%s

                当前对话：
                %s

                学生说：%s

                要求：直接回答学生的问题，给出实用建议，回答完整自然。
                """.formatted(plan.getSubject(), plan.getExamDate(), plan.getDailyMinutes(), taskText,
                compactText(conversationText, 1500), message);
        return prompt;
    }

    private void sendSseChunk(SseEmitter emitter, String event, String data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send AI stream chunk.", e);
        }
    }

    private String compactText(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "暂无";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
    }

    private StudySummaryReportResp toSummaryReportResp(StudySummaryReportDO report) {
        List<String> suggestions = StringUtils.hasText(report.getSuggestions())
                ? List.of(report.getSuggestions().split("\\n"))
                : List.of();
        return StudySummaryReportResp.builder()
                .reportId(report.getId())
                .planId(report.getPlanId())
                .userQuestion(report.getUserQuestion())
                .aiAnswer(report.getAiAnswer())
                .summary(report.getSummary())
                .suggestions(suggestions)
                .createdAt(report.getCreatedAt())
                .build();
    }

    private double calculateCompletionRate(int completedTaskCount, int totalTaskCount) {
        if (totalTaskCount <= 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(completedTaskCount * 100.0 / totalTaskCount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private List<String> extractWeakPoints(List<StudyFeedbackDO> feedbackList) {
        Set<String> weakPoints = new LinkedHashSet<>();
        for (StudyFeedbackDO feedback : feedbackList) {
            if (StringUtils.hasText(feedback.getProblem())) {
                weakPoints.add(feedback.getProblem().trim());
            }
            if (StringUtils.hasText(feedback.getDifficulty()) && !"EASY".equalsIgnoreCase(feedback.getDifficulty())) {
                weakPoints.add("难度反馈：" + feedback.getDifficulty().trim());
            }
            if (weakPoints.size() >= 5) {
                break;
            }
        }
        return new ArrayList<>(weakPoints);
    }

    private String buildSummaryText(
            int completedTaskCount,
            int totalTaskCount,
            double completionRate,
            List<String> weakPoints) {
        if (totalTaskCount == 0) {
            return "当前还没有生成学习任务。";
        }
        String weakPointText = weakPoints.isEmpty()
                ? "暂时还没有记录到明显薄弱点。"
                : "当前主要薄弱点包括：" + String.join("；", weakPoints) + "。";
        return "你已完成 " + completedTaskCount + "/" + totalTaskCount
                + " 个学习任务，完成率为 " + completionRate + "%。" + weakPointText;
    }

    private List<String> buildSuggestions(double completionRate, List<String> weakPoints) {
        List<String> suggestions = new ArrayList<>();
        if (completionRate < 50) {
            suggestions.add("建议先缩小每日任务范围，优先完成最关键的基础内容。");
        } else if (completionRate < 80) {
            suggestions.add("当前节奏基本可行，建议预留时间复盘未完成任务。");
        } else {
            suggestions.add("当前进度较稳定，可以加入限时练习来提升应试表现。");
        }

        if (!weakPoints.isEmpty()) {
            suggestions.add("安排一次专项复习，重点处理：" + weakPoints.get(0));
            suggestions.add("建立错题和问题清单，在下一次调整计划前集中复盘。");
        } else {
            suggestions.add("建议每完成一个任务后提交反馈，这样系统才能持续识别薄弱点。");
        }
        return suggestions;
    }

    private List<StudyTaskDO> applyAiAdjustment(
            StudyPlanDO plan,
            List<StudyTaskDO> pendingTasks,
            List<StudyFeedbackDO> feedbackList) {
        Map<Long, StudyTaskDO> taskMap = pendingTasks.stream()
                .collect(Collectors.toMap(StudyTaskDO::getId, Function.identity()));
        List<AiStudyPlanAdjuster.AdjustedTask> aiTasks = aiStudyPlanAdjuster.adjust(plan, pendingTasks, feedbackList);
        List<StudyTaskDO> adjustedTasks = new ArrayList<>();

        for (AiStudyPlanAdjuster.AdjustedTask aiTask : aiTasks) {
            if (aiTask.taskId() == null || !taskMap.containsKey(aiTask.taskId())) {
                continue;
            }
            StudyTaskDO task = taskMap.get(aiTask.taskId());
            if (StringUtils.hasText(aiTask.title())) {
                task.setTitle(aiTask.title());
            }
            if (StringUtils.hasText(aiTask.content())) {
                task.setContent(aiTask.content());
            }
            if (aiTask.estimatedMinutes() != null && aiTask.estimatedMinutes() > 0) {
                task.setEstimatedMinutes(Math.min(aiTask.estimatedMinutes(), plan.getDailyMinutes()));
            }
            adjustedTasks.add(task);
        }
        return adjustedTasks;
    }

    private List<StudyTaskDO> applyLocalAdjustment(
            StudyPlanDO plan,
            List<StudyTaskDO> pendingTasks,
            List<StudyFeedbackDO> feedbackList) {
        String weakPoint = latestProblem(feedbackList);
        List<StudyTaskDO> adjustedTasks = new ArrayList<>();
        int adjustedLimit = Math.min(pendingTasks.size(), 3);

        for (int i = 0; i < adjustedLimit; i++) {
            StudyTaskDO task = pendingTasks.get(i);
            task.setTitle("Adjusted Day " + task.getDayIndex() + ": strengthen " + plan.getSubject());
            task.setContent("Review the weak point: " + weakPoint
                    + ". Then redo examples, summarize mistakes, and finish a short practice set.");
            task.setEstimatedMinutes(Math.min(task.getEstimatedMinutes() + 15, plan.getDailyMinutes()));
            adjustedTasks.add(task);
        }
        return adjustedTasks;
    }

    private String latestProblem(List<StudyFeedbackDO> feedbackList) {
        if (feedbackList == null || feedbackList.isEmpty()) {
            return "recent unfinished or difficult topics";
        }
        for (StudyFeedbackDO feedback : feedbackList) {
            if (StringUtils.hasText(feedback.getProblem())) {
                return feedback.getProblem();
            }
        }
        return "recent unfinished or difficult topics";
    }

    private List<StudyTaskDO> listTaskEntities(String planId) {
        return studyTaskMapper.selectList(new LambdaQueryWrapper<StudyTaskDO>()
                .eq(StudyTaskDO::getPlanId, planId)
                .orderByAsc(StudyTaskDO::getDayIndex));
    }

    private List<StudyFeedbackDO> listFeedback(String planId) {
        return studyFeedbackMapper.selectList(new LambdaQueryWrapper<StudyFeedbackDO>()
                .eq(StudyFeedbackDO::getPlanId, planId)
                .orderByDesc(StudyFeedbackDO::getCreatedAt));
    }

    private StudyPlanDO requirePlan(String planId) {
        if (!StringUtils.hasText(planId)) {
            throw new IllegalArgumentException("planId cannot be blank");
        }
        StudyPlanDO plan = studyPlanMapper.selectOne(new LambdaQueryWrapper<StudyPlanDO>()
                .eq(StudyPlanDO::getPlanId, planId));
        if (plan == null) {
            throw new IllegalArgumentException("study plan not found");
        }
        return plan;
    }

    private StudyTaskDO requireTask(String planId, Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("taskId cannot be null");
        }
        StudyTaskDO task = studyTaskMapper.selectOne(new LambdaQueryWrapper<StudyTaskDO>()
                .eq(StudyTaskDO::getPlanId, planId)
                .eq(StudyTaskDO::getId, taskId));
        if (task == null) {
            throw new IllegalArgumentException("study task not found");
        }
        return task;
    }

    private StudyPlanResp buildDemoPlan(StudyPlanCreateReq request) {
        List<StudyTaskResp> tasks = buildDemoTasks(request);
        return StudyPlanResp.builder()
                .planId(UUID.randomUUID().toString())
                .title(request.getSubject() + " study plan")
                .subject(request.getSubject())
                .examDate(request.getExamDate())
                .dailyMinutes(request.getDailyMinutes())
                .targetScore(request.getTargetScore())
                .tasks(tasks)
                .build();
    }

    private void savePlan(StudyPlanCreateReq request, StudyPlanResp plan) {
        StudyPlanDO entity = new StudyPlanDO();
        entity.setPlanId(plan.getPlanId());
        entity.setTitle(plan.getTitle());
        entity.setSubject(plan.getSubject());
        entity.setExamDate(plan.getExamDate());
        entity.setCurrentLevel(request.getCurrentLevel());
        entity.setDailyMinutes(plan.getDailyMinutes());
        entity.setTargetScore(plan.getTargetScore());
        entity.setStatus("ACTIVE");
        studyPlanMapper.insert(entity);
    }

    private void saveTasks(StudyPlanResp plan) {
        if (plan.getTasks() == null || plan.getTasks().isEmpty()) {
            return;
        }
        for (StudyTaskResp task : plan.getTasks()) {
            StudyTaskDO entity = new StudyTaskDO();
            entity.setPlanId(plan.getPlanId());
            entity.setDayIndex(task.getDayIndex());
            entity.setTaskDate(task.getTaskDate());
            entity.setTitle(task.getTitle());
            entity.setContent(task.getContent());
            entity.setEstimatedMinutes(task.getEstimatedMinutes());
            entity.setStatus("TODO");
            studyTaskMapper.insert(entity);
            task.setTaskId(entity.getId());
            task.setStatus(entity.getStatus());
        }
    }

    private StudyPlanResp toPlanResp(StudyPlanDO plan, List<StudyTaskResp> tasks) {
        return StudyPlanResp.builder()
                .planId(plan.getPlanId())
                .title(plan.getTitle())
                .subject(plan.getSubject())
                .examDate(plan.getExamDate())
                .dailyMinutes(plan.getDailyMinutes())
                .targetScore(plan.getTargetScore())
                .tasks(tasks)
                .build();
    }

    private StudyPlanListItemResp toPlanListItemResp(StudyPlanDO plan) {
        return StudyPlanListItemResp.builder()
                .planId(plan.getPlanId())
                .title(plan.getTitle())
                .subject(plan.getSubject())
                .examDate(plan.getExamDate())
                .dailyMinutes(plan.getDailyMinutes())
                .targetScore(plan.getTargetScore())
                .status(plan.getStatus())
                .build();
    }

    private StudyTaskResp toTaskResp(StudyTaskDO task) {
        return StudyTaskResp.builder()
                .taskId(task.getId())
                .dayIndex(task.getDayIndex())
                .taskDate(task.getTaskDate())
                .title(task.getTitle())
                .content(task.getContent())
                .estimatedMinutes(task.getEstimatedMinutes())
                .status(task.getStatus())
                .build();
    }

    private List<StudyTaskResp> buildDemoTasks(StudyPlanCreateReq request) {
        long daysUntilExam = ChronoUnit.DAYS.between(LocalDate.now(), request.getExamDate());
        int taskCount = (int) Math.min(Math.max(daysUntilExam, 1), 7);
        List<StudyTaskResp> tasks = new ArrayList<>();

        for (int i = 1; i <= taskCount; i++) {
            tasks.add(StudyTaskResp.builder()
                    .dayIndex(i)
                    .taskDate(LocalDate.now().plusDays(i - 1L))
                    .title("Day " + i + ": " + request.getSubject() + " focused study")
                    .content("Review key concepts, finish practice exercises, and write down questions you cannot solve.")
                    .estimatedMinutes(request.getDailyMinutes())
                    .status("TODO")
                    .build());
        }
        return tasks;
    }
}



