package com.example.triage.application.service;

import com.example.triage.application.dto.TriageAssessRequest;
import com.example.triage.domain.capability.CapabilityRecommendation;
import com.example.triage.domain.disease.DiseaseCandidate;
import com.example.triage.domain.hospital.DepartmentRecommendation;
import com.example.triage.domain.population.PopulationProfile;
import com.example.triage.domain.triage.TriageAssessment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TriageDecisionService {

    private final PopulationProfileService populationProfileService;
    private final DiseaseCandidateService diseaseCandidateService;
    private final MedicalCapabilityService medicalCapabilityService;
    private final LocalDepartmentMappingService localDepartmentMappingService;
    private final TriageExplanationService triageExplanationService;

    public TriageDecisionService(PopulationProfileService populationProfileService,
                                 DiseaseCandidateService diseaseCandidateService,
                                 MedicalCapabilityService medicalCapabilityService,
                                 LocalDepartmentMappingService localDepartmentMappingService,
                                 TriageExplanationService triageExplanationService) {
        this.populationProfileService = populationProfileService;
        this.diseaseCandidateService = diseaseCandidateService;
        this.medicalCapabilityService = medicalCapabilityService;
        this.localDepartmentMappingService = localDepartmentMappingService;
        this.triageExplanationService = triageExplanationService;
    }

    public TriageAssessment assess(TriageAssessRequest request) {
        PopulationProfile profile = populationProfileService.buildProfile(request);
        List<DiseaseCandidate> diseases = diseaseCandidateService.identifyCandidates(request.getSymptoms(), profile);
        List<CapabilityRecommendation> capabilities = medicalCapabilityService.recommendCapabilities(diseases, profile);
        List<DepartmentRecommendation> departments = localDepartmentMappingService.mapDepartments(capabilities, profile, request.getCity());
        String explanation = triageExplanationService.explain(profile, diseases, capabilities, departments);
        return new TriageAssessment(profile, diseases, capabilities, departments, explanation);
    }
}
