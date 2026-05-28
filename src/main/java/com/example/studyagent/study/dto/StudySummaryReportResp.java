package com.example.studyagent.study.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StudySummaryReportResp {

    private Long reportId;
    private String planId;
    private String userQuestion;
    private String aiAnswer;
    private String summary;
    private List<String> suggestions;
    private LocalDateTime createdAt;
}
