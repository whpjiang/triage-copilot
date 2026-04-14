package com.example.triage.domain.hospital;

public record DepartmentRecommendation(
        Long departmentId,
        String hospitalName,
        String departmentName,
        String parentDepartmentName,
        String capabilityCode,
        String supportLevel,
        String districtName,
        Double latitude,
        Double longitude,
        Double authorityScore,
        double score
) {
}
