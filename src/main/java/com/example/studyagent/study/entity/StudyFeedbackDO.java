package com.example.studyagent.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("study_feedback")
public class StudyFeedbackDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String planId;
    private Long taskId;
    private Boolean completed;
    private String difficulty;
    private String problem;
    private LocalDateTime createdAt;
}
