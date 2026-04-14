package com.example.triage.web;

import com.example.triage.application.dto.TriageAssessRequest;
import com.example.triage.application.dto.TriageAssessResponse;
import com.example.triage.application.orchestrator.TriageDecisionOrchestrator;
import com.example.triage.common.ApiResponse;
import com.example.triage.web.dto.TriageAnalyzeRequest;
import com.example.triage.web.dto.TriageAnalyzeResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/triage")
public class TriageController {

    private final TriageDecisionOrchestrator triageDecisionOrchestrator;

    public TriageController(TriageDecisionOrchestrator triageDecisionOrchestrator) {
        this.triageDecisionOrchestrator = triageDecisionOrchestrator;
    }

    @PostMapping("/analyze")
    public ApiResponse<TriageAnalyzeResponse> analyze(@Valid @RequestBody TriageAnalyzeRequest request) {
        TriageAssessRequest assessRequest = new TriageAssessRequest();
        assessRequest.setSymptoms(request.getSymptoms());
        assessRequest.setAge(request.getAge());
        assessRequest.setGender(request.getGender());
        assessRequest.setCity(request.getCity());
        assessRequest.setSpecialCondition(request.getSpecialCondition());

        TriageAssessResponse assessResponse = triageDecisionOrchestrator.assess(assessRequest);
        TriageAnalyzeResponse legacy = new TriageAnalyzeResponse();
        legacy.setRecommendedDepartment(assessResponse.getCapabilityRecommendations().isEmpty()
                ? "-"
                : assessResponse.getCapabilityRecommendations().get(0).getCapabilityName());
        legacy.setRecommendedFunctionalClinic(assessResponse.getDepartmentRecommendations().isEmpty()
                ? "-"
                : assessResponse.getDepartmentRecommendations().get(0).getDepartmentName());
        legacy.setUrgencyLevel(assessResponse.getCandidateDiseases().isEmpty()
                ? "MEDIUM"
                : assessResponse.getCandidateDiseases().get(0).getUrgencyLevel().toUpperCase());
        legacy.setUrgencyReason("Legacy analyze endpoint is now backed by the structured triage pipeline.");
        legacy.setAdvice(assessResponse.getExplanation());
        legacy.setDisclaimer("This suggestion cannot replace clinical diagnosis.");
        return ApiResponse.success(legacy);
    }
}
