package com.example.studyagent.study.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudyAssistantFinishReq {

    @NotBlank(message = "conversationText cannot be blank")
    private String conversationText;
}
