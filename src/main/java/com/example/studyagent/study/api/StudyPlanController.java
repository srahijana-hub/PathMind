package com.example.studyagent.study.api;

import com.example.studyagent.common.Result;
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
import com.example.studyagent.study.service.StudyPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/study/plans")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @PostMapping
    public Result<StudyPlanResp> createPlan(@Valid @RequestBody StudyPlanCreateReq request) {
        return Result.success(studyPlanService.createPlan(request));
    }

    @GetMapping("/{planId}")
    public Result<StudyPlanResp> getPlanDetail(@PathVariable String planId) {
        return Result.success(studyPlanService.getPlanDetail(planId));
    }

    @GetMapping
    public Result<List<StudyPlanListItemResp>> listPlans() {
        return Result.success(studyPlanService.listPlans());
    }

    @PostMapping("/{planId}/feedback")
    public Result<StudyFeedbackResp> submitFeedback(
            @PathVariable String planId,
            @Valid @RequestBody StudyFeedbackReq request) {
        return Result.success(studyPlanService.submitFeedback(planId, request));
    }

    @PostMapping("/{planId}/adjust")
    public Result<StudyPlanAdjustResp> adjustPlan(@PathVariable String planId) {
        return Result.success(studyPlanService.adjustPlan(planId));
    }

    @GetMapping("/{planId}/summary")
    public Result<StudySummaryResp> getSummary(@PathVariable String planId) {
        return Result.success(studyPlanService.getSummary(planId));
    }

    @PostMapping("/{planId}/assistant/chat")
    public Result<StudyAssistantChatResp> chatWithAssistant(
            @PathVariable String planId,
            @Valid @RequestBody StudyAssistantChatReq request) {
        return Result.success(studyPlanService.chatWithAssistant(planId, request));
    }

    @PostMapping(value = "/{planId}/assistant/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAssistantChat(
            @PathVariable String planId,
            @Valid @RequestBody StudyAssistantChatReq request) {
        return studyPlanService.streamAssistantChat(planId, request);
    }

    @PostMapping("/{planId}/assistant/finish")
    public Result<StudyAssistantFinishResp> finishAssistantConversation(
            @PathVariable String planId,
            @Valid @RequestBody StudyAssistantFinishReq request) {
        return Result.success(studyPlanService.finishAssistantConversation(planId, request));
    }

    @GetMapping("/{planId}/summary-reports")
    public Result<List<StudySummaryReportResp>> listSummaryReports(@PathVariable String planId) {
        return Result.success(studyPlanService.listSummaryReports(planId));
    }
}
