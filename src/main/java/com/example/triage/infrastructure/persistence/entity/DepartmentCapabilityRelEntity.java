package com.example.triage.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("department_capability_rel")
public class DepartmentCapabilityRelEntity {
    @TableId
    public Long id;
    public Long departmentId;
    public String capabilityCode;
    public String supportLevel;
    public BigDecimal weight;
    public String source;
}
