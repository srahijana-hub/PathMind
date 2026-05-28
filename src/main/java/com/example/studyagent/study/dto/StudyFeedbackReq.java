package com.example.studyagent.study.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudyFeedbackReq {

    @NotNull(message = "taskId cannot be null")
    private Long taskId;

    @NotNull(message = "completed cannot be null")
    private Boolean completed;

    @Size(max = 32, message = "difficulty length must be less than or equal to 32")
    private String difficulty;

    @Size(max = 1000, message = "problem length must be less than or equal to 1000")
    private String problem;
}
