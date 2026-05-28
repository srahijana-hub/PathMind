package com.example.studyagent.study.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class StudyPlanListItemResp {

    private String planId;
    private String title;
    private String subject;
    private LocalDate examDate;
    private Integer dailyMinutes;
    private Integer targetScore;
    private String status;
}
