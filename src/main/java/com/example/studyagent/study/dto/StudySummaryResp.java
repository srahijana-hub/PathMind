package com.example.studyagent.study.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudySummaryResp {

    private String planId;
    private Integer completedTaskCount;
    private Integer totalTaskCount;
    private Double completionRate;
    private List<String> weakPoints;
    private String summary;
    private List<String> suggestions;
}
