package com.example.studyagent.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("study_plan")
public class StudyPlanDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String planId;
    private String title;
    private String subject;
    private LocalDate examDate;
    private String currentLevel;
    private Integer dailyMinutes;
    private Integer targetScore;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
