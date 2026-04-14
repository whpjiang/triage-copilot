package com.example.triage.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("hospital_department")
public class HospitalDepartmentEntity {
    @TableId
    public Long id;
    public Long hospitalId;
    public String departmentName;
    public String parentDepartmentName;
    public String departmentIntro;
    public String serviceScope;
    public Integer activeStatus;
    public Integer deleted;
    public String genderRule;
    public Integer ageMin;
    public Integer ageMax;
    public String crowdTagsJson;
    public String standardDeptCode;
    public String subspecialtyCode;
    public String districtName;
    public Double latitude;
    public Double longitude;
    public Integer isEmergency;
    public Double nationalKeyScore;
    public Double provincialKeyScore;
    public Double cityKeyScore;
    public Double authorityScore;
}
