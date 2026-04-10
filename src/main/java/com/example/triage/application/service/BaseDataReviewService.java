package com.example.triage.application.service;

import com.example.triage.application.dto.BaseDataReviewResponse;
import com.example.triage.infrastructure.persistence.repository.BaseDataAdminRepository;
import org.springframework.stereotype.Service;

@Service
public class BaseDataReviewService {

    private final BaseDataAdminRepository baseDataAdminRepository;

    public BaseDataReviewService(BaseDataAdminRepository baseDataAdminRepository) {
        this.baseDataAdminRepository = baseDataAdminRepository;
    }

    public BaseDataReviewResponse listPendingReviews(String datasetType, Long jobId, int limit) {
        BaseDataReviewResponse response = new BaseDataReviewResponse();
        response.setPendingCount(baseDataAdminRepository.countPendingReviews(datasetType, jobId));
        response.setItems(baseDataAdminRepository.findPendingReviews(datasetType, jobId, limit).stream().map(item -> {
            BaseDataReviewResponse.ReviewItemDto dto = new BaseDataReviewResponse.ReviewItemDto();
            dto.setId(item.id());
            dto.setJobId(item.jobId());
            dto.setDatasetType(item.datasetType());
            dto.setItemKey(item.itemKey());
            dto.setIssueType(item.issueType());
            dto.setRawContent(item.rawContent());
            dto.setSuggestion(item.suggestion());
            dto.setResolved(item.resolved());
            return dto;
        }).toList());
        return response;
    }
}
