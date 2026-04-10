package com.example.triage.application.dto;

import java.util.ArrayList;
import java.util.List;

public class BaseDataJobDetailResponse {

    private JobDto job;
    private Integer pendingReviewCount;
    private List<FailureLogDto> failures = new ArrayList<>();

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

    public List<FailureLogDto> getFailures() {
        return failures;
    }

    public void setFailures(List<FailureLogDto> failures) {
        this.failures = failures;
    }

    public static class JobDto {
        private Long jobId;
        private String datasetType;
        private String fileName;
        private String status;
        private Integer successCount;
        private Integer failureCount;
        private Integer reviewCount;
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
}
