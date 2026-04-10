package com.example.triage.infrastructure.ai;

import com.example.triage.application.service.DiseaseNormalizeService;
import com.example.triage.domain.disease.DiseaseCandidate;
import com.example.triage.domain.population.PopulationProfile;
import com.example.triage.infrastructure.persistence.model.DiseaseRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class AiDiseaseRecallClient {

    private final ObjectProvider<ChatModel> chatModelProvider;
    private final ObjectMapper objectMapper;
    private final DiseaseNormalizeService diseaseNormalizeService;

    public AiDiseaseRecallClient(ObjectProvider<ChatModel> chatModelProvider,
                                 ObjectMapper objectMapper,
                                 DiseaseNormalizeService diseaseNormalizeService) {
        this.chatModelProvider = chatModelProvider;
        this.objectMapper = objectMapper;
        this.diseaseNormalizeService = diseaseNormalizeService;
    }

    public List<String> suggestDiseaseCodes(String symptoms,
                                            PopulationProfile profile,
                                            List<DiseaseRecord> eligibleDiseases,
                                            List<DiseaseCandidate> ruleCandidates) {
        ChatModel model = chatModelProvider.getIfAvailable();
        if (model == null || !StringUtils.hasText(symptoms) || eligibleDiseases.isEmpty()) {
            return List.of();
        }
        try {
            String prompt = """
                    Symptoms: %s
                    Gender: %s
                    Age: %s
                    Rule candidates: %s

                    Choose up to 3 supplemental disease_code values from the options below.
                    Do not invent new codes. Do not explain. Return JSON string array only.
                    Disease options:
                    %s
                    """.formatted(
                    symptoms,
                    profile.gender(),
                    profile.age(),
                    ruleCandidates.stream().map(DiseaseCandidate::diseaseCode).distinct().toList(),
                    buildDiseaseOptions(symptoms, eligibleDiseases)
            );
            String response = ChatClient.builder(model).build()
                    .prompt()
                    .system("Return only a JSON array of disease_code values chosen from the provided options.")
                    .user(prompt)
                    .call()
                    .content();
            return parseDiseaseCodes(response);
        } catch (Exception ex) {
            return List.of();
        }
    }

    List<String> parseDiseaseCodes(String response) {
        if (!StringUtils.hasText(response)) {
            return List.of();
        }
        String trimmed = response.trim();
        try {
            if (trimmed.startsWith("[")) {
                List<String> values = objectMapper.readValue(trimmed, new TypeReference<List<String>>() {
                });
                return values.stream()
                        .map(diseaseNormalizeService::normalizeText)
                        .filter(StringUtils::hasText)
                        .distinct()
                        .limit(3)
                        .toList();
            }
        } catch (Exception ignored) {
        }
        Set<String> fallback = new LinkedHashSet<>();
        for (String line : trimmed.split("[\\r\\n,]")) {
            String normalized = diseaseNormalizeService.normalizeText(
                    line.replace("\"", "").replace("-", "").trim()
            );
            if (StringUtils.hasText(normalized)) {
                fallback.add(normalized);
            }
            if (fallback.size() >= 3) {
                break;
            }
        }
        return new ArrayList<>(fallback);
    }

    private String buildDiseaseOptions(String symptoms, List<DiseaseRecord> diseases) {
        String normalizedSymptoms = diseaseNormalizeService.normalizeText(symptoms);
        return diseases.stream()
                .sorted(Comparator.comparingInt((DiseaseRecord disease) -> overlapScore(normalizedSymptoms, disease)).reversed()
                        .thenComparing(DiseaseRecord::diseaseCode))
                .limit(30)
                .map(disease -> "- %s | %s | aliases=%s | keywords=%s".formatted(
                        disease.diseaseCode(),
                        disease.diseaseName(),
                        String.join("/", diseaseNormalizeService.parseList(disease.aliasesJson())),
                        String.join("/", diseaseNormalizeService.parseList(disease.symptomKeywords()))
                ))
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("");
    }

    private int overlapScore(String normalizedSymptoms, DiseaseRecord disease) {
        int score = 0;
        if (normalizedSymptoms.contains(diseaseNormalizeService.normalizeText(disease.diseaseName()))) {
            score += 3;
        }
        for (String alias : diseaseNormalizeService.parseList(disease.aliasesJson())) {
            if (normalizedSymptoms.contains(alias)) {
                score += 2;
            }
        }
        for (String keyword : diseaseNormalizeService.parseList(disease.symptomKeywords())) {
            if (normalizedSymptoms.contains(keyword)) {
                score += 1;
            }
        }
        return score;
    }
}
