package com.example.triage.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("hospital")
public class HospitalEntity {
    @TableId
    public Long id;
    public String hospitalCode;
    public String hospitalName;
    public String city;
    public String districtName;
    public Double latitude;
    public Double longitude;
    public String hospitalLevel;
    public Integer isEmergency;
    public Double authorityScore;
    public Integer activeStatus;
    public Integer deleted;
}
