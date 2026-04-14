package com.example.triage.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("triage_slot_state")
public class TriageSlotStateEntity {
    @TableId
    public Long id;
    public String sessionId;
    public String symptomsJson;
    public String diseaseName;
    public String targetHospital;
    public String targetDepartment;
    public String targetDoctor;
    public String missingSlotsJson;
}
