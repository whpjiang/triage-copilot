package com.example.triage.application.dto;

import java.util.ArrayList;
import java.util.List;

public class BaseDataReviewResponse {

    private Integer pendingCount;
    private List<ReviewItemDto> items = new ArrayList<>();

    public Integer getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Integer pendingCount) {
        this.pendingCount = pendingCount;
    }

    public List<ReviewItemDto> getItems() {
        return items;
    }

    public void setItems(List<ReviewItemDto> items) {
        this.items = items;
    }

    public static class ReviewItemDto {
        private Long id;
        private Long jobId;
        private String datasetType;
        private String itemKey;
        private String issueType;
        private String rawContent;
        private String suggestion;
        private Boolean resolved;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

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

        public String getItemKey() {
            return itemKey;
        }

        public void setItemKey(String itemKey) {
            this.itemKey = itemKey;
        }

        public String getIssueType() {
            return issueType;
        }

        public void setIssueType(String issueType) {
            this.issueType = issueType;
        }

        public String getRawContent() {
            return rawContent;
        }

        public void setRawContent(String rawContent) {
            this.rawContent = rawContent;
        }

        public String getSuggestion() {
            return suggestion;
        }

        public void setSuggestion(String suggestion) {
            this.suggestion = suggestion;
        }

        public Boolean getResolved() {
            return resolved;
        }

        public void setResolved(Boolean resolved) {
            this.resolved = resolved;
        }
    }
}
