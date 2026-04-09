package com.example.triage.domain.hospital;

public record DepartmentRecommendation(
        Long departmentId,
        String hospitalName,
        String departmentName,
        String parentDepartmentName,
        String capabilityCode,
        String supportLevel,
        double score
) {
}
