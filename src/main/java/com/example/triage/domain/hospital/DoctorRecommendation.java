package com.example.triage.domain.hospital;

public record DoctorRecommendation(
        Long doctorId,
        String doctorName,
        String title,
        String hospitalName,
        String departmentName,
        String specialtyText,
        double score
) {
}
