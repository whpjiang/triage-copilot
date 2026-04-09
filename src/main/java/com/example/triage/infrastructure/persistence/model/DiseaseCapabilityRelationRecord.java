package com.example.triage.infrastructure.persistence.model;

public record DiseaseCapabilityRelationRecord(
        String diseaseCode,
        String capabilityCode,
        String relType,
        Double priorityScore,
        String crowdConstraint,
        String note
) {
}
