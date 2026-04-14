package com.example.triage.application.service;

import com.example.triage.application.dto.TriageAssessRequest;
import com.example.triage.domain.capability.CapabilityRecommendation;
import com.example.triage.domain.disease.DiseaseCandidate;
import com.example.triage.domain.hospital.DepartmentRecommendation;
import com.example.triage.domain.population.PopulationProfile;
import com.example.triage.domain.triage.IntentType;
import com.example.triage.domain.triage.RecommendationContext;
import com.example.triage.domain.triage.RouteType;
import com.example.triage.domain.triage.SeverityLevel;
import com.example.triage.domain.triage.TriageAssessment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TriageDecisionService {

    private final PopulationProfileService populationProfileService;
    private final PathwayTagService pathwayTagService;
    private final DiseaseCandidateService diseaseCandidateService;
    private final MedicalCapabilityService medicalCapabilityService;
    private final LocalDepartmentMappingService localDepartmentMappingService;
    private final DoctorRecommendationService doctorRecommendationService;
    private final TriageExplanationService triageExplanationService;
    private final RecommendationPolicyService recommendationPolicyService;

    public TriageDecisionService(PopulationProfileService populationProfileService,
                                 PathwayTagService pathwayTagService,
                                 DiseaseCandidateService diseaseCandidateService,
                                 MedicalCapabilityService medicalCapabilityService,
                                 LocalDepartmentMappingService localDepartmentMappingService,
                                 DoctorRecommendationService doctorRecommendationService,
                                 TriageExplanationService triageExplanationService,
                                 RecommendationPolicyService recommendationPolicyService) {
        this.populationProfileService = populationProfileService;
        this.pathwayTagService = pathwayTagService;
        this.diseaseCandidateService = diseaseCandidateService;
        this.medicalCapabilityService = medicalCapabilityService;
        this.localDepartmentMappingService = localDepartmentMappingService;
        this.doctorRecommendationService = doctorRecommendationService;
        this.triageExplanationService = triageExplanationService;
        this.recommendationPolicyService = recommendationPolicyService;
    }

    public TriageAssessment assess(TriageAssessRequest request) {
        PopulationProfile profile = populationProfileService.buildProfile(request);
        List<String> pathwayTags = pathwayTagService.inferPathwayTags(request.getSymptoms(), profile);
        List<DiseaseCandidate> diseases = diseaseCandidateService.identifyCandidates(request.getSymptoms(), profile);
        boolean commonDisease = recommendationPolicyService.isCommonDisease(diseases);
        SeverityLevel severityLevel = recommendationPolicyService.resolveSeverityLevel(diseases);
        RecommendationContext recommendationContext = recommendationPolicyService.buildContext(
                request,
                severityLevel,
                commonDisease,
                IntentType.SYMPTOM_TRIAGE_QUERY
        );
        RouteType routeType = recommendationPolicyService.resolveRouteType(recommendationContext);
        List<CapabilityRecommendation> capabilities = medicalCapabilityService.recommendCapabilities(diseases, profile, pathwayTags);
        List<DepartmentRecommendation> departments = localDepartmentMappingService.mapDepartments(
                capabilities,
                profile,
                request.getCity(),
                recommendationContext
        );
        var doctors = doctorRecommendationService.recommendDoctors(departments, capabilities, profile, recommendationContext);
        String explanation = triageExplanationService.explain(profile, pathwayTags, diseases, capabilities, departments, doctors);
        return new TriageAssessment(profile, pathwayTags, diseases, capabilities, departments, doctors, explanation, commonDisease, routeType);
    }
}
