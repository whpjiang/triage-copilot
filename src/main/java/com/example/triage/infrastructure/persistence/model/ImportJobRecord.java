package com.example.triage.infrastructure.persistence.model;

public record ImportJobRecord(
        Long jobId,
        String datasetType,
        String fileName,
        String status,
        Integer successCount,
        Integer failureCount,
        Integer reviewCount,
        Integer autoMappedCount,
        String message
) {
}
