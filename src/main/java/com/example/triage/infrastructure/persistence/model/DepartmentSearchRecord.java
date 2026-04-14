package com.example.triage.infrastructure.persistence.model;

public record DepartmentSearchRecord(
        Long departmentId,
        Long hospitalId,
        String hospitalName,
        String departmentName,
        String parentDepartmentName,
        String districtName,
        Double authorityScore,
        String standardDeptCode,
        String subspecialtyCode,
        Integer isEmergency
) {
}
