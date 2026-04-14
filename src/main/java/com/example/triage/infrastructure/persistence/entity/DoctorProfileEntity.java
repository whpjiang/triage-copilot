package com.example.triage.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("doctor_profile")
public class DoctorProfileEntity {
    @TableId
    public Long id;
    public Long hospitalId;
    public Long departmentId;
    public String doctorName;
    public String title;
    public String specialtyText;
    public String genderRule;
    public Integer ageMin;
    public Integer ageMax;
    public String crowdTagsJson;
    public Integer activeStatus;
}
