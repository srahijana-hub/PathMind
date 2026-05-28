package com.example.studyagent.study.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudyFeedbackResp {

    private Long feedbackId;
    private String planId;
    private Long taskId;
    private Boolean completed;
    private String difficulty;
    private String problem;
    private String taskStatus;
}
