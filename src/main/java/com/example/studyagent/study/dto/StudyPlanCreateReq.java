package com.example.studyagent.study.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudyPlanCreateReq {

    @NotBlank(message = "subject cannot be blank")
    private String subject;

    @NotNull(message = "examDate cannot be null")
    @Future(message = "examDate must be in the future")
    private LocalDate examDate;

    @NotBlank(message = "currentLevel cannot be blank")
    private String currentLevel;

    @NotNull(message = "dailyMinutes cannot be null")
    @Min(value = 15, message = "dailyMinutes must be at least 15")
    @Max(value = 600, message = "dailyMinutes must be less than or equal to 600")
    private Integer dailyMinutes;

    @NotNull(message = "targetScore cannot be null")
    @Min(value = 1, message = "targetScore must be at least 1")
    @Max(value = 100, message = "targetScore must be less than or equal to 100")
    private Integer targetScore;
}
