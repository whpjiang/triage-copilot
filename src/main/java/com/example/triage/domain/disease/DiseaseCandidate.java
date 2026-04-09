package com.example.triage.domain.disease;

import java.util.List;

public record DiseaseCandidate(
        String diseaseCode,
        String diseaseName,
        List<String> matchedKeywords,
        String urgencyLevel,
        double score
) {
}
