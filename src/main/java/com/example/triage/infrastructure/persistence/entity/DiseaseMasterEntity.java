package com.example.triage.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("disease_master")
public class DiseaseMasterEntity {
    @TableId
    public Long id;
    public String diseaseCode;
    public String diseaseName;
    public String aliasesJson;
    public String symptomKeywords;
    public String genderRule;
    public Integer ageMin;
    public Integer ageMax;
    public String ageGroup;
    public String urgencyLevel;
    public String reviewStatus;
    public Integer deleted;
}
