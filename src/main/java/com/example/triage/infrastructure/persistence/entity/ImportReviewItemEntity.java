package com.example.triage.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("import_review_item")
public class ImportReviewItemEntity {
    @TableId
    public Long id;
    public Long jobId;
    public String datasetType;
    public String itemKey;
    public String issueType;
    public String rawContent;
    public String suggestion;
    public Integer resolved;
    public String resolutionNote;
}
