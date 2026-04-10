package com.example.triage.application.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BaseDataImportResponse {

    private Long jobId;
    private String datasetType;
    private Integer successCount;
    private Integer failureCount;
    private Integer reviewCount;
    private Integer autoMappedCount;
    private Map<String, Integer> reviewTypeDistribution = new LinkedHashMap<>();
    private Map<String, Integer> commonIssueDistribution = new LinkedHashMap<>();
    private List<String> messages = new ArrayList<>();

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getDatasetType() {
        return datasetType;
    }

    public void setDatasetType(String datasetType) {
        this.datasetType = datasetType;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Integer getAutoMappedCount() {
        return autoMappedCount;
    }

    public void setAutoMappedCount(Integer autoMappedCount) {
        this.autoMappedCount = autoMappedCount;
    }

    public Map<String, Integer> getReviewTypeDistribution() {
        return reviewTypeDistribution;
    }

    public void setReviewTypeDistribution(Map<String, Integer> reviewTypeDistribution) {
        this.reviewTypeDistribution = reviewTypeDistribution;
    }

    public Map<String, Integer> getCommonIssueDistribution() {
        return commonIssueDistribution;
    }

    public void setCommonIssueDistribution(Map<String, Integer> commonIssueDistribution) {
        this.commonIssueDistribution = commonIssueDistribution;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
