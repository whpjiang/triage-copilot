package com.example.triage.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("ai_recall_audit_log")
public class AiRecallAuditLogEntity {
    @TableId
    public Long id;
    public String symptoms;
    public String gender;
    public Integer age;
    public String ageGroup;
    public Integer eligibleDiseaseCount;
    public String ruleCandidateCodesJson;
    public String suggestedCodesJson;
    public String status;
    public String message;
}
