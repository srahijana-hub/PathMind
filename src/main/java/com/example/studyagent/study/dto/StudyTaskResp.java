package com.example.studyagent.study.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class StudyTaskResp {

    private Long taskId;
    private Integer dayIndex;
    private LocalDate taskDate;
    private String title;
    private String content;
    private Integer estimatedMinutes;
    private String status;
}
