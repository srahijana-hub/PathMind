package com.example.studyagent.study.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class StudyPlanResp {

    private String planId;
    private String title;
    private String subject;
    private LocalDate examDate;
    private Integer dailyMinutes;
    private Integer targetScore;
    private List<StudyTaskResp> tasks;
}
