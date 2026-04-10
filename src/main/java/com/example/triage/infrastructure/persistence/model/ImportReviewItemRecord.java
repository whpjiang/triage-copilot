package com.example.triage.infrastructure.persistence.model;

public record ImportReviewItemRecord(
        Long id,
        Long jobId,
        String datasetType,
        String itemKey,
        String issueType,
        String rawContent,
        String suggestion,
        Boolean resolved
) {
}
