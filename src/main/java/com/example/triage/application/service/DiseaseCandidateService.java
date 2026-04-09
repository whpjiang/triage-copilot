package com.example.triage.application.service;

import com.example.triage.domain.disease.DiseaseCandidate;
import com.example.triage.domain.population.PopulationProfile;
import com.example.triage.infrastructure.persistence.model.DiseaseRecord;
import com.example.triage.infrastructure.persistence.repository.DiseaseDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DiseaseCandidateService {

    private final DiseaseDataRepository diseaseDataRepository;
    private final DiseaseNormalizeService diseaseNormalizeService;

    public DiseaseCandidateService(DiseaseDataRepository diseaseDataRepository, DiseaseNormalizeService diseaseNormalizeService) {
        this.diseaseDataRepository = diseaseDataRepository;
        this.diseaseNormalizeService = diseaseNormalizeService;
    }

    public List<DiseaseCandidate> identifyCandidates(String symptoms, PopulationProfile profile) {
        String normalizedSymptoms = symptoms == null ? "" : symptoms.toLowerCase(Locale.ROOT);
        List<DiseaseCandidate> candidates = new ArrayList<>();
        for (DiseaseRecord disease : diseaseDataRepository.findApprovedDiseases()) {
            if (!diseaseNormalizeService.matchesGenderAndAge(disease.genderRule(), disease.ageMin(), disease.ageMax(), profile.gender(), profile.age())) {
                continue;
            }
            List<String> matched = new ArrayList<>();
            double score = 0D;
            if (normalizedSymptoms.contains(diseaseNormalizeService.normalizeText(disease.diseaseName()))) {
                score += 4;
                matched.add(disease.diseaseName());
            }
            for (String alias : diseaseNormalizeService.parseList(disease.aliasesJson())) {
                if (StringUtils.hasText(alias) && normalizedSymptoms.contains(alias)) {
                    score += 3;
                    matched.add(alias);
                }
            }
            for (String keyword : diseaseNormalizeService.parseList(disease.symptomKeywords())) {
                if (StringUtils.hasText(keyword) && normalizedSymptoms.contains(keyword)) {
                    score += 1.2;
                    matched.add(keyword);
                }
            }
            if (score > 0) {
                candidates.add(new DiseaseCandidate(
                        disease.diseaseCode(),
                        disease.diseaseName(),
                        matched.stream().distinct().toList(),
                        disease.urgencyLevel(),
                        score
                ));
            }
        }
        Map<String, DiseaseCandidate> merged = new LinkedHashMap<>();
        for (DiseaseCandidate candidate : candidates) {
            merged.compute(candidate.diseaseCode(), (key, existing) -> {
                if (existing == null) {
                    return candidate;
                }
                List<String> keywords = new ArrayList<>(existing.matchedKeywords());
                keywords.addAll(candidate.matchedKeywords());
                return new DiseaseCandidate(existing.diseaseCode(), existing.diseaseName(), keywords.stream().distinct().toList(), existing.urgencyLevel(), existing.score() + candidate.score());
            });
        }
        return merged.values().stream()
                .sorted(Comparator.comparingDouble(DiseaseCandidate::score).reversed())
                .limit(5)
                .toList();
    }
}
