package com.example.studyagent.study.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudyAssistantChatReq {

    @NotBlank(message = "message cannot be blank")
    private String message;

    private String conversationText;
}
