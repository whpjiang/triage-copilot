package com.example.triage.controller;

import com.example.triage.application.dto.TriageAssessRequest;
import com.example.triage.application.dto.TriageAssessResponse;
import com.example.triage.application.orchestrator.TriageDecisionOrchestrator;
import com.example.triage.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/triage")
public class TriageAssessController {

    private final TriageDecisionOrchestrator triageDecisionOrchestrator;

    public TriageAssessController(TriageDecisionOrchestrator triageDecisionOrchestrator) {
        this.triageDecisionOrchestrator = triageDecisionOrchestrator;
    }

    @PostMapping("/assess")
    public ApiResponse<TriageAssessResponse> assess(@Valid @RequestBody TriageAssessRequest request) {
        return ApiResponse.success(triageDecisionOrchestrator.assess(request));
    }
}
