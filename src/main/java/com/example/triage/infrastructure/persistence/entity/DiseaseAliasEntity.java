package com.example.triage.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("disease_alias")
public class DiseaseAliasEntity {
    @TableId
    public Long id;
    public String diseaseCode;
    public String aliasName;
    public String aliasType;
    public String source;
}
