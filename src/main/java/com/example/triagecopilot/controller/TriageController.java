package com.example.triagecopilot.controller;

import com.example.triagecopilot.common.ApiResponse;
import com.example.triagecopilot.dto.TriageAnalyzeRequest;
import com.example.triagecopilot.dto.TriageAnalyzeResponse;
import com.example.triagecopilot.service.TriageAgentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/triage")
public class TriageController {

    private final TriageAgentService triageAgentService;

    public TriageController(TriageAgentService triageAgentService) {
        this.triageAgentService = triageAgentService;
    }

    @PostMapping("/analyze")
    public ApiResponse<TriageAnalyzeResponse> analyze(@Valid @RequestBody TriageAnalyzeRequest request) {
        try {
            return ApiResponse.success(triageAgentService.analyze(request));
        } catch (Exception ex) {
            TriageAnalyzeResponse fallback = new TriageAnalyzeResponse();
            fallback.setRecommendedDepartment("General Internal Medicine");
            fallback.setRecommendedFunctionalClinic("General Clinic");
            fallback.setUrgencyLevel("MEDIUM");
            fallback.setUrgencyReason("Temporary service exception.");
            fallback.setAdvice("Please retry in a moment.");
            fallback.setDisclaimer("This suggestion cannot replace clinical diagnosis.");
            return ApiResponse.success(fallback);
        }
    }
}
