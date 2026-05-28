package com.example.studyagent.study.service;

import com.example.studyagent.study.dto.StudyFeedbackReq;
import com.example.studyagent.study.dto.StudyFeedbackResp;
import com.example.studyagent.study.dto.StudyAssistantChatReq;
import com.example.studyagent.study.dto.StudyAssistantChatResp;
import com.example.studyagent.study.dto.StudyAssistantFinishReq;
import com.example.studyagent.study.dto.StudyAssistantFinishResp;
import com.example.studyagent.study.dto.StudyPlanAdjustResp;
import com.example.studyagent.study.dto.StudyPlanCreateReq;
import com.example.studyagent.study.dto.StudyPlanListItemResp;
import com.example.studyagent.study.dto.StudyPlanResp;
import com.example.studyagent.study.dto.StudySummaryResp;
import com.example.studyagent.study.dto.StudySummaryReportResp;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface StudyPlanService {

    StudyPlanResp createPlan(StudyPlanCreateReq request);

    StudyPlanResp getPlanDetail(String planId);

    List<StudyPlanListItemResp> listPlans();

    StudyFeedbackResp submitFeedback(String planId, StudyFeedbackReq request);

    StudyPlanAdjustResp adjustPlan(String planId);

    StudySummaryResp getSummary(String planId);

    StudyAssistantChatResp chatWithAssistant(String planId, StudyAssistantChatReq request);

    SseEmitter streamAssistantChat(String planId, StudyAssistantChatReq request);

    StudyAssistantFinishResp finishAssistantConversation(String planId, StudyAssistantFinishReq request);

    List<StudySummaryReportResp> listSummaryReports(String planId);
}
