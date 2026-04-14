package com.example.triage.infrastructure.persistence.model;

public record DoctorRecord(
        Long doctorId,
        Long hospitalId,
        Long departmentId,
        String doctorName,
        String title,
        String specialtyText,
        String genderRule,
        Integer ageMin,
        Integer ageMax,
        String crowdTagsJson,
        String capabilityCode,
        Double weight,
        String hospitalName,
        String departmentName,
        String districtName,
        Double latitude,
        Double longitude,
        Double authorityScore,
        Double academicTitleScore,
        Integer isExpert,
        String campusName
) {
}
