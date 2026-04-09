package com.example.triage.infrastructure.persistence.model;

public record DepartmentMappingRecord(
        Long departmentId,
        Long hospitalId,
        String hospitalName,
        String departmentName,
        String parentDepartmentName,
        String departmentIntro,
        String serviceScope,
        String genderRule,
        Integer ageMin,
        Integer ageMax,
        String crowdTagsJson,
        String capabilityCode,
        String supportLevel,
        Double weight,
        String source
) {
}
