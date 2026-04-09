package com.example.triage.infrastructure.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class AiExplanationClient {

    private final ObjectProvider<ChatModel> chatModelProvider;

    public AiExplanationClient(ObjectProvider<ChatModel> chatModelProvider) {
        this.chatModelProvider = chatModelProvider;
    }

    public String polishExplanation(String structuredFacts) {
        ChatModel model = chatModelProvider.getIfAvailable();
        if (model == null) {
            return structuredFacts;
        }
        try {
            String response = ChatClient.builder(model).build()
                    .prompt()
                    .system("Rewrite the structured triage explanation into concise Chinese, but do not add or change any clinical facts.")
                    .user(structuredFacts)
                    .call()
                    .content();
            return response == null || response.isBlank() ? structuredFacts : response;
        } catch (Exception ex) {
            return structuredFacts;
        }
    }
}
