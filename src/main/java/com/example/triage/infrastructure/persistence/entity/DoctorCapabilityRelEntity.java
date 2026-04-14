package com.example.triage.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("doctor_capability_rel")
public class DoctorCapabilityRelEntity {
    @TableId
    public Long id;
    public Long doctorId;
    public String capabilityCode;
    public BigDecimal weight;
}
