package com.example.triage.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("disease_capability_rel")
public class DiseaseCapabilityRelEntity {
    @TableId
    public Long id;
    public String diseaseCode;
    public String capabilityCode;
    public String relType;
    public BigDecimal priorityScore;
    public String crowdConstraint;
    public String note;
}
