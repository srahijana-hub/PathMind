package com.example.studyagent.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("study_task")
public class StudyTaskDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String planId;
    private Integer dayIndex;
    private LocalDate taskDate;
    private String title;
    private String content;
    private Integer estimatedMinutes;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
