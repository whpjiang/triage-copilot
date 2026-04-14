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
    public Integer activeStatus;
    public Integer deleted;
}
