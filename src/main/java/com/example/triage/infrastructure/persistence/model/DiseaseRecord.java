package com.example.triage.infrastructure.persistence.model;

public record DiseaseRecord(
        String diseaseCode,
        String diseaseName,
        String aliasesJson,
        String symptomKeywords,
        String genderRule,
        Integer ageMin,
        Integer ageMax,
        String ageGroup,
        String urgencyLevel,
        String reviewStatus
) {
}
