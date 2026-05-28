package com.example.studyagent.study.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudyPlanAdjustResp {

    private String planId;
    private Integer adjustedCount;
    private List<StudyTaskResp> adjustedTasks;
}
