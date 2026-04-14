package com.example.triage.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("import_job_record")
public class ImportJobRecordEntity {
    @TableId
    public Long id;
    public String datasetType;
    public String fileName;
    public String status;
    public Integer successCount;
    public Integer failureCount;
    public Integer reviewCount;
    public Integer autoMappedCount;
    public String message;
}
