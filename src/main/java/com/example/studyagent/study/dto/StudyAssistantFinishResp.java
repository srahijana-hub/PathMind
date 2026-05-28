package com.example.studyagent.study.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudyAssistantFinishResp {

    private StudySummaryResp latestSummary;
    private List<StudySummaryReportResp> reports;
}
