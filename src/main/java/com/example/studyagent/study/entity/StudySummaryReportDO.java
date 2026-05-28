package com.example.studyagent.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("study_summary_report")
public class StudySummaryReportDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String planId;
    private String userQuestion;
    private String aiAnswer;
    private String summary;
    private String suggestions;
    private LocalDateTime createdAt;
}
