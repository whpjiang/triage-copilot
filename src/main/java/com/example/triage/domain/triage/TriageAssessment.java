package com.example.triage.domain.triage;

import com.example.triage.domain.capability.CapabilityRecommendation;
import com.example.triage.domain.disease.DiseaseCandidate;
import com.example.triage.domain.hospital.DepartmentRecommendation;
import com.example.triage.domain.population.PopulationProfile;

import java.util.List;

public record TriageAssessment(
        PopulationProfile populationProfile,
        List<DiseaseCandidate> candidateDiseases,
        List<CapabilityRecommendation> capabilityRecommendations,
        List<DepartmentRecommendation> departmentRecommendations,
        String explanation
) {
}
