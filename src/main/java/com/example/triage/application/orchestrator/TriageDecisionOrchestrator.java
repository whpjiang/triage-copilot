package com.example.triage.application.orchestrator;

import com.example.triage.application.dto.TriageAssessRequest;
import com.example.triage.application.dto.TriageAssessResponse;
import com.example.triage.application.service.TriageDecisionService;
import com.example.triage.domain.triage.TriageAssessment;
import org.springframework.stereotype.Component;

@Component
public class TriageDecisionOrchestrator {

    private final TriageDecisionService triageDecisionService;

    public TriageDecisionOrchestrator(TriageDecisionService triageDecisionService) {
        this.triageDecisionService = triageDecisionService;
    }

    public TriageAssessResponse assess(TriageAssessRequest request) {
        TriageAssessment assessment = triageDecisionService.assess(request);
        TriageAssessResponse response = new TriageAssessResponse();
        TriageAssessResponse.PopulationProfileDto profileDto = new TriageAssessResponse.PopulationProfileDto();
        profileDto.setGender(assessment.populationProfile().gender());
        profileDto.setAge(assessment.populationProfile().age());
        profileDto.setAgeGroup(assessment.populationProfile().ageGroup().name().toLowerCase());
        profileDto.setCrowdTags(assessment.populationProfile().crowdTags());
        response.setPopulationProfile(profileDto);
        response.setPathwayTags(assessment.pathwayTags());
        response.setCandidateDiseases(assessment.candidateDiseases().stream().map(item -> {
            TriageAssessResponse.DiseaseCandidateDto dto = new TriageAssessResponse.DiseaseCandidateDto();
            dto.setDiseaseCode(item.diseaseCode());
            dto.setDiseaseName(item.diseaseName());
            dto.setMatchedKeywords(item.matchedKeywords());
            dto.setUrgencyLevel(item.urgencyLevel());
            dto.setScore(item.score());
            return dto;
        }).toList());
        response.setCapabilityRecommendations(assessment.capabilityRecommendations().stream().map(item -> {
            TriageAssessResponse.CapabilityRecommendationDto dto = new TriageAssessResponse.CapabilityRecommendationDto();
            dto.setCapabilityCode(item.capabilityCode());
            dto.setCapabilityName(item.capabilityName());
            dto.setCapabilityType(item.capabilityType());
            dto.setStandardDeptCode(item.standardDeptCode());
            dto.setMatchedDiseases(item.matchedDiseases());
            dto.setScore(item.score());
            return dto;
        }).toList());
        response.setDepartmentRecommendations(assessment.departmentRecommendations().stream().map(item -> {
            TriageAssessResponse.DepartmentRecommendationDto dto = new TriageAssessResponse.DepartmentRecommendationDto();
            dto.setDepartmentId(item.departmentId());
            dto.setHospitalName(item.hospitalName());
            dto.setDepartmentName(item.departmentName());
            dto.setParentDepartmentName(item.parentDepartmentName());
            dto.setCapabilityCode(item.capabilityCode());
            dto.setSupportLevel(item.supportLevel());
            dto.setScore(item.score());
            return dto;
        }).toList());
        response.setDoctorRecommendations(assessment.doctorRecommendations().stream().map(item -> {
            TriageAssessResponse.DoctorRecommendationDto dto = new TriageAssessResponse.DoctorRecommendationDto();
            dto.setDoctorId(item.doctorId());
            dto.setDoctorName(item.doctorName());
            dto.setTitle(item.title());
            dto.setHospitalName(item.hospitalName());
            dto.setDepartmentName(item.departmentName());
            dto.setSpecialtyText(item.specialtyText());
            dto.setScore(item.score());
            return dto;
        }).toList());
        response.setExplanation(assessment.explanation());
        return response;
    }
}
