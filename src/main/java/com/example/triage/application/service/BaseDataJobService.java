package com.example.triage.application.service;

import com.example.triage.application.dto.BaseDataJobDetailResponse;
import com.example.triage.application.dto.BaseDataJobListResponse;
import com.example.triage.infrastructure.persistence.repository.BaseDataAdminRepository;
import org.springframework.stereotype.Service;

@Service
public class BaseDataJobService {

    private final BaseDataAdminRepository baseDataAdminRepository;

    public BaseDataJobService(BaseDataAdminRepository baseDataAdminRepository) {
        this.baseDataAdminRepository = baseDataAdminRepository;
    }

    public BaseDataJobListResponse listJobs(int limit) {
        BaseDataJobListResponse response = new BaseDataJobListResponse();
        response.setJobs(baseDataAdminRepository.findRecentJobs(limit).stream().map(item -> {
            BaseDataJobListResponse.JobSummaryDto dto = new BaseDataJobListResponse.JobSummaryDto();
            dto.setJobId(item.jobId());
            dto.setDatasetType(item.datasetType());
            dto.setFileName(item.fileName());
            dto.setStatus(item.status());
            dto.setSuccessCount(item.successCount());
            dto.setFailureCount(item.failureCount());
            dto.setReviewCount(item.reviewCount());
            dto.setAutoMappedCount(item.autoMappedCount());
            dto.setMessage(item.message());
            return dto;
        }).toList());
        return response;
    }

    public BaseDataJobDetailResponse getJobDetail(Long jobId, int failureLimit) {
        var job = baseDataAdminRepository.findJobById(jobId);
        if (job == null) {
            throw new IllegalArgumentException("job not found: " + jobId);
        }
        BaseDataJobDetailResponse response = new BaseDataJobDetailResponse();
        BaseDataJobDetailResponse.JobDto jobDto = new BaseDataJobDetailResponse.JobDto();
        jobDto.setJobId(job.jobId());
        jobDto.setDatasetType(job.datasetType());
        jobDto.setFileName(job.fileName());
        jobDto.setStatus(job.status());
        jobDto.setSuccessCount(job.successCount());
        jobDto.setFailureCount(job.failureCount());
        jobDto.setReviewCount(job.reviewCount());
        jobDto.setAutoMappedCount(job.autoMappedCount());
        jobDto.setMessage(job.message());
        response.setJob(jobDto);
        response.setPendingReviewCount(baseDataAdminRepository.countPendingReviews(null, jobId));
        response.setReviewTypeDistribution(baseDataAdminRepository.countReviewTypes(jobId));
        response.setCommonIssueDistribution(baseDataAdminRepository.countFailureTypes(jobId));
        response.setFailures(baseDataAdminRepository.findFailuresByJobId(jobId, failureLimit).stream().map(item -> {
            BaseDataJobDetailResponse.FailureLogDto dto = new BaseDataJobDetailResponse.FailureLogDto();
            dto.setFailureId(item.failureId());
            dto.setRowNumber(item.rowNumber());
            dto.setRawContent(item.rawContent());
            dto.setErrorMessage(item.errorMessage());
            return dto;
        }).toList());
        response.setRecentReviews(baseDataAdminRepository.findRecentReviewsByJobId(jobId, failureLimit).stream().map(item -> {
            BaseDataJobDetailResponse.ReviewLogDto dto = new BaseDataJobDetailResponse.ReviewLogDto();
            dto.setReviewId(item.id());
            dto.setDatasetType(item.datasetType());
            dto.setItemKey(item.itemKey());
            dto.setIssueType(item.issueType());
            dto.setSuggestion(item.suggestion());
            dto.setResolved(item.resolved());
            return dto;
        }).toList());
        return response;
    }
}
