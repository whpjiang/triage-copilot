package com.example.triage.infrastructure.persistence.model;

public record CapabilityRecord(
        String capabilityCode,
        String capabilityName,
        String capabilityType,
        String parentCode,
        String standardDeptCode,
        String aliasesJson,
        String genderRule,
        Integer ageMin,
        Integer ageMax,
        String crowdTagsJson,
        String pathwayTagsJson,
        Integer activeStatus
) {
}
