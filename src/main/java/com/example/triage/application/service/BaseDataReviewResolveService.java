package com.example.triage.application.service;

import com.example.triage.application.dto.BaseDataReviewResolveRequest;
import com.example.triage.application.dto.BaseDataReviewResolveResponse;
import com.example.triage.infrastructure.persistence.repository.BaseDataAdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BaseDataReviewResolveService {

    private final BaseDataAdminRepository baseDataAdminRepository;

    public BaseDataReviewResolveService(BaseDataAdminRepository baseDataAdminRepository) {
        this.baseDataAdminRepository = baseDataAdminRepository;
    }

    public BaseDataReviewResolveResponse resolve(BaseDataReviewResolveRequest request) {
        if (request == null || request.getReviewId() == null) {
            throw new IllegalArgumentException("reviewId is required");
        }
        int updated = baseDataAdminRepository.resolveReviewItem(
                request.getReviewId(),
                StringUtils.hasText(request.getResolutionNote()) ? request.getResolutionNote().trim() : "resolved manually"
        );
        if (updated == 0) {
            throw new IllegalArgumentException("review item not found: " + request.getReviewId());
        }
        BaseDataReviewResolveResponse response = new BaseDataReviewResolveResponse();
        response.setReviewId(request.getReviewId());
        response.setResolved(true);
        response.setPendingCount(baseDataAdminRepository.countPendingReviews(null, null));
        return response;
    }
}
