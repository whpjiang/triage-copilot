package com.example.triage.application.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BaseDataJobDetailResponse {

    private JobDto job;
    private Integer pendingReviewCount;
    private Map<String, Integer> reviewTypeDistribution = new LinkedHashMap<>();
    private Map<String, Integer> commonIssueDistribution = new LinkedHashMap<>();
    private List<FailureLogDto> failures = new ArrayList<>();
    private List<ReviewLogDto> recentReviews = new ArrayList<>();

    public JobDto getJob() {
        return job;
    }

    public void setJob(JobDto job) {
        this.job = job;
    }

    public Integer getPendingReviewCount() {
        return pendingReviewCount;
    }

    public void setPendingReviewCount(Integer pendingReviewCount) {
        this.pendingReviewCount = pendingReviewCount;
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

    public List<FailureLogDto> getFailures() {
        return failures;
    }

    public void setFailures(List<FailureLogDto> failures) {
        this.failures = failures;
    }

    public List<ReviewLogDto> getRecentReviews() {
        return recentReviews;
    }

    public void setRecentReviews(List<ReviewLogDto> recentReviews) {
        this.recentReviews = recentReviews;
    }

    public static class JobDto {
        private Long jobId;
        private String datasetType;
        private String fileName;
        private String status;
        private Integer successCount;
        private Integer failureCount;
        private Integer reviewCount;
        private Integer autoMappedCount;
        private String message;

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

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
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

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class FailureLogDto {
        private Long failureId;
        private Integer rowNumber;
        private String rawContent;
        private String errorMessage;

        public Long getFailureId() {
            return failureId;
        }

        public void setFailureId(Long failureId) {
            this.failureId = failureId;
        }

        public Integer getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(Integer rowNumber) {
            this.rowNumber = rowNumber;
        }

        public String getRawContent() {
            return rawContent;
        }

        public void setRawContent(String rawContent) {
            this.rawContent = rawContent;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    public static class ReviewLogDto {
        private Long reviewId;
        private String datasetType;
        private String itemKey;
        private String issueType;
        private String suggestion;
        private Boolean resolved;

        public Long getReviewId() {
            return reviewId;
        }

        public void setReviewId(Long reviewId) {
            this.reviewId = reviewId;
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
