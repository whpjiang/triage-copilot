package com.example.triage.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("medical_capability_catalog")
public class MedicalCapabilityCatalogEntity {
    @TableId
    public Long id;
    public String capabilityCode;
    public String capabilityName;
    public String capabilityType;
    public String parentCode;
    public String standardDeptCode;
    public String aliasesJson;
    public String genderRule;
    public Integer ageMin;
    public Integer ageMax;
    public String crowdTagsJson;
    public String pathwayTagsJson;
    public Integer activeStatus;
}
