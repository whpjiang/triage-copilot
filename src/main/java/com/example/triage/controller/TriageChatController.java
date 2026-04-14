package com.example.triage.controller;

import com.example.triage.application.dto.TriageChatRequest;
import com.example.triage.application.dto.TriageChatResponse;
import com.example.triage.application.orchestrator.TriageConversationOrchestrator;
import com.example.triage.common.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/triage")
public class TriageChatController {

    private final TriageConversationOrchestrator triageConversationOrchestrator;

    public TriageChatController(TriageConversationOrchestrator triageConversationOrchestrator) {
        this.triageConversationOrchestrator = triageConversationOrchestrator;
    }

    @PostMapping("/chat")
    public ApiResponse<TriageChatResponse> chat(@RequestBody TriageChatRequest request) {
        return ApiResponse.success(triageConversationOrchestrator.chat(request));
    }
}
