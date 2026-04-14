package com.example.triage.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("import_failure_log")
public class ImportFailureLogEntity {
    @TableId
    public Long id;
    public Long jobId;
    @TableField("row_number")
    public Integer rowNumber;
    public String rawContent;
    public String errorMessage;
}
