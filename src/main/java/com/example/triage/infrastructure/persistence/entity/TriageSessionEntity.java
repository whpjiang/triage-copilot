package com.example.triage.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("triage_session")
public class TriageSessionEntity {
    @TableId
    public Long id;
    public String sessionId;
    public String userId;
    public String dialogId;
    public String currentStage;
    public Integer askRound;
    public Integer invalidAnswerCount;
    public String city;
    public String area;
    public Integer nearby;
    public BigDecimal latitude;
    public BigDecimal longitude;
    public Integer patientAge;
    public String patientGender;
    public String severityLevel;
    public String routeType;
    public String status;
}
