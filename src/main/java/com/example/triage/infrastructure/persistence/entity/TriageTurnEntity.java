package com.example.triage.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("triage_turn")
public class TriageTurnEntity {
    @TableId
    public Long id;
    public String sessionId;
    public Integer turnNo;
    public String userMessage;
    public String normalizedQuery;
    public String intent;
    public String stage;
    public String replyText;
    public String rawDecisionJson;
}
