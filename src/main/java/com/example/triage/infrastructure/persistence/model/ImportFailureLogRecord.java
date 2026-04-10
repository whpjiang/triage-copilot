package com.example.triage.infrastructure.persistence.model;

public record ImportFailureLogRecord(
        Long failureId,
        Long jobId,
        Integer rowNumber,
        String rawContent,
        String errorMessage
) {
}
