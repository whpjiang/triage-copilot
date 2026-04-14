package com.example.triage.infrastructure.ai;

import org.springframework.stereotype.Component;

@Component
public class AiExplanationClient {

    private final DashScopeCompatibleClient dashScopeCompatibleClient;

    public AiExplanationClient(DashScopeCompatibleClient dashScopeCompatibleClient) {
        this.dashScopeCompatibleClient = dashScopeCompatibleClient;
    }

    public String polishExplanation(String structuredFacts) {
        String response = dashScopeCompatibleClient.chat(
                "Rewrite the structured triage explanation into concise Chinese, but do not add or change any clinical facts.",
                structuredFacts
        );
        if (response == null || response.isBlank()) {
            return structuredFacts;
        }
        return response;
    }
}
