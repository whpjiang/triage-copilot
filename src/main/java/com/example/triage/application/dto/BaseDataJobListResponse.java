package com.example.triage.application.dto;

import java.util.ArrayList;
import java.util.List;

public class BaseDataJobListResponse {

    private List<JobSummaryDto> jobs = new ArrayList<>();

    public List<JobSummaryDto> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobSummaryDto> jobs) {
        this.jobs = jobs;
    }

    public static class JobSummaryDto {
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
}
