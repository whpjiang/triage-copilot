package com.example.triage.application.service;

import com.example.triage.application.dto.BaseDataReviewResponse;
import com.example.triage.infrastructure.persistence.repository.BaseDataAdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Service
public class BaseDataReviewService {

    private final BaseDataAdminRepository baseDataAdminRepository;

    public BaseDataReviewService(BaseDataAdminRepository baseDataAdminRepository) {
        this.baseDataAdminRepository = baseDataAdminRepository;
    }

    public BaseDataReviewResponse listPendingReviews(String datasetType, Long jobId, int limit) {
        BaseDataReviewResponse response = new BaseDataReviewResponse();
        response.setPendingCount(baseDataAdminRepository.countPendingReviews(datasetType, jobId));
        response.setIssueTypeDistribution(baseDataAdminRepository.countReviewTypes(jobId, datasetType));
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
            dto.setMatchedCapabilityCodes(parseSuggestionSegment(item.suggestion(), "matched"));
            dto.setReviewEvidence(parseSuggestionText(item.suggestion(), "evidence"));
            return dto;
        }).toList());
        return response;
    }

    private List<String> parseSuggestionSegment(String suggestion, String key) {
        String raw = parseSuggestionText(suggestion, key);
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        String normalized = raw.replace("[", "").replace("]", "");
        return Arrays.stream(normalized.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private String parseSuggestionText(String suggestion, String key) {
        if (!StringUtils.hasText(suggestion)) {
            return null;
        }
        String marker = key + "=";
        int start = suggestion.indexOf(marker);
        if (start < 0) {
            return null;
        }
        int from = start + marker.length();
        int end = suggestion.indexOf(';', from);
        String value = end < 0 ? suggestion.substring(from) : suggestion.substring(from, end);
        return value.trim();
    }
}
