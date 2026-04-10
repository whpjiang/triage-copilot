package com.example.triage.infrastructure.ai;

import com.example.triage.application.service.DiseaseNormalizeService;
import com.example.triage.domain.disease.DiseaseCandidate;
import com.example.triage.domain.population.PopulationProfile;
import com.example.triage.infrastructure.persistence.model.DiseaseRecord;
import com.example.triage.infrastructure.persistence.repository.AiRecallAuditRepository;
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
import java.util.Locale;
import java.util.Set;

@Component
public class AiDiseaseRecallClient {

    private final ObjectProvider<ChatModel> chatModelProvider;
    private final ObjectMapper objectMapper;
    private final DiseaseNormalizeService diseaseNormalizeService;
    private final AiRecallAuditRepository aiRecallAuditRepository;

    public AiDiseaseRecallClient(ObjectProvider<ChatModel> chatModelProvider,
                                 ObjectMapper objectMapper,
                                 DiseaseNormalizeService diseaseNormalizeService,
                                 AiRecallAuditRepository aiRecallAuditRepository) {
        this.chatModelProvider = chatModelProvider;
        this.objectMapper = objectMapper;
        this.diseaseNormalizeService = diseaseNormalizeService;
        this.aiRecallAuditRepository = aiRecallAuditRepository;
    }

    public List<String> suggestDiseaseCodes(String symptoms,
                                            PopulationProfile profile,
                                            List<DiseaseRecord> eligibleDiseases,
                                            List<DiseaseCandidate> ruleCandidates) {
        String normalizedSymptoms = diseaseNormalizeService.normalizeText(symptoms);
        if (!StringUtils.hasText(normalizedSymptoms) || eligibleDiseases.isEmpty()) {
            audit(symptoms, profile, eligibleDiseases, ruleCandidates, List.of(), "SKIPPED_EMPTY", "empty symptoms or no eligible diseases");
            return List.of();
        }
        if (isHighRisk(normalizedSymptoms)) {
            audit(symptoms, profile, eligibleDiseases, ruleCandidates, List.of(), "SKIPPED_HIGH_RISK", "high-risk symptoms require rule-based handling");
            return List.of();
        }
        ChatModel model = chatModelProvider.getIfAvailable();
        if (model == null) {
            audit(symptoms, profile, eligibleDiseases, ruleCandidates, List.of(), "SKIPPED_NO_MODEL", "chat model unavailable");
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
            List<String> suggestions = parseDiseaseCodes(response);
            audit(symptoms, profile, eligibleDiseases, ruleCandidates, suggestions, "SUGGESTED", truncate(response));
            return suggestions;
        } catch (Exception ex) {
            audit(symptoms, profile, eligibleDiseases, ruleCandidates, List.of(), "FAILED", truncate(ex.getMessage()));
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

    private boolean isHighRisk(String symptoms) {
        return containsAny(symptoms,
                "胸痛",
                "呼吸困难",
                "昏迷",
                "意识不清",
                "抽搐",
                "大出血",
                "偏瘫",
                "言语不清",
                "chest pain",
                "shortness of breath",
                "unconscious",
                "convulsion",
                "major bleeding",
                "hemiplegia");
    }

    private boolean containsAny(String text, String... keywords) {
        String normalized = text.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (normalized.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private void audit(String symptoms,
                       PopulationProfile profile,
                       List<DiseaseRecord> eligibleDiseases,
                       List<DiseaseCandidate> ruleCandidates,
                       List<String> suggestions,
                       String status,
                       String message) {
        aiRecallAuditRepository.save(
                symptoms,
                profile.gender(),
                profile.age(),
                profile.ageGroup().name().toLowerCase(Locale.ROOT),
                eligibleDiseases.size(),
                ruleCandidates.stream().map(DiseaseCandidate::diseaseCode).distinct().toList(),
                suggestions,
                status,
                message
        );
    }

    private String truncate(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() <= 500 ? trimmed : trimmed.substring(0, 500);
    }
}
