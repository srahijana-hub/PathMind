package com.example.studyagent.study.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudyAssistantChatResp {

    private String answer;
    private StudySummaryResp latestSummary;
    private List<StudySummaryReportResp> reports;
}
