package com.example.triage.application.service;

import com.example.triage.domain.capability.CapabilityRecommendation;
import com.example.triage.domain.disease.DiseaseCandidate;
import com.example.triage.domain.population.PopulationProfile;
import com.example.triage.infrastructure.persistence.model.CapabilityRecord;
import com.example.triage.infrastructure.persistence.model.DiseaseCapabilityRelationRecord;
import com.example.triage.infrastructure.persistence.repository.CapabilityDataRepository;
import com.example.triage.infrastructure.persistence.repository.DiseaseDataRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MedicalCapabilityService {

    private final DiseaseDataRepository diseaseDataRepository;
    private final CapabilityDataRepository capabilityDataRepository;
    private final DiseaseNormalizeService diseaseNormalizeService;

    public MedicalCapabilityService(DiseaseDataRepository diseaseDataRepository,
                                    CapabilityDataRepository capabilityDataRepository,
                                    DiseaseNormalizeService diseaseNormalizeService) {
        this.diseaseDataRepository = diseaseDataRepository;
        this.capabilityDataRepository = capabilityDataRepository;
        this.diseaseNormalizeService = diseaseNormalizeService;
    }

    public List<CapabilityRecommendation> recommendCapabilities(List<DiseaseCandidate> diseases, PopulationProfile profile, List<String> pathwayTags) {
        List<String> diseaseCodes = diseases.stream().map(DiseaseCandidate::diseaseCode).toList();
        List<DiseaseCapabilityRelationRecord> relations = diseaseDataRepository.findRelationsByDiseaseCodes(diseaseCodes);
        Map<String, DiseaseCandidate> diseaseMap = diseases.stream().collect(Collectors.toMap(DiseaseCandidate::diseaseCode, Function.identity()));
        Map<String, List<DiseaseCapabilityRelationRecord>> grouped = relations.stream().collect(Collectors.groupingBy(DiseaseCapabilityRelationRecord::capabilityCode));
        List<CapabilityRecord> capabilities = capabilityDataRepository.findByCapabilityCodes(new ArrayList<>(grouped.keySet()));
        Map<String, CapabilityRecord> capabilityMap = capabilities.stream().collect(Collectors.toMap(CapabilityRecord::capabilityCode, Function.identity()));
        List<CapabilityRecommendation> output = new ArrayList<>();
        for (Map.Entry<String, List<DiseaseCapabilityRelationRecord>> entry : grouped.entrySet()) {
            CapabilityRecord capability = capabilityMap.get(entry.getKey());
            if (capability == null) {
                continue;
            }
            if (!diseaseNormalizeService.matchesGenderAndAge(capability.genderRule(), capability.ageMin(), capability.ageMax(), profile.gender(), profile.age())) {
                continue;
            }
            List<String> requiredCrowds = diseaseNormalizeService.parseList(capability.crowdTagsJson());
            if (!requiredCrowds.isEmpty() && profile.crowdTags().stream().noneMatch(requiredCrowds::contains)) {
                continue;
            }
            List<String> capabilityPathways = diseaseNormalizeService.parseList(capability.pathwayTagsJson());
            double score = 0D;
            List<String> matchedDiseases = new ArrayList<>();
            for (DiseaseCapabilityRelationRecord rel : entry.getValue()) {
                DiseaseCandidate disease = diseaseMap.get(rel.diseaseCode());
                if (disease == null) {
                    continue;
                }
                score += disease.score() * (rel.priorityScore() == null ? 1D : rel.priorityScore());
                matchedDiseases.add(disease.diseaseName());
            }
            if (!capabilityPathways.isEmpty()) {
                long matchedPathwayCount = pathwayTags.stream().filter(capabilityPathways::contains).count();
                if (matchedPathwayCount > 0) {
                    score += matchedPathwayCount * 2.5;
                } else if ("SPECIAL_PATHWAY".equals(capability.capabilityType())) {
                    score = score * 0.75;
                }
            }
            if ("SUBSPECIALTY".equals(capability.capabilityType())) {
                score += 1.2;
            }
            if ("SPECIAL_POPULATION".equals(capability.capabilityType()) && !requiredCrowds.isEmpty()) {
                score += 0.8;
            }
            output.add(new CapabilityRecommendation(
                    capability.capabilityCode(),
                    capability.capabilityName(),
                    capability.capabilityType(),
                    capability.standardDeptCode(),
                    matchedDiseases.stream().distinct().toList(),
                    score
            ));
        }
        Map<String, CapabilityRecommendation> merged = new LinkedHashMap<>();
        for (CapabilityRecommendation item : output) {
            merged.put(item.capabilityCode(), item);
        }
        return merged.values().stream()
                .sorted(Comparator.comparingDouble(CapabilityRecommendation::score).reversed())
                .limit(6)
                .toList();
    }
}
