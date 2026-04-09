package com.example.triage.application.service;

import com.example.triage.domain.capability.CapabilityRecommendation;
import com.example.triage.domain.hospital.DepartmentRecommendation;
import com.example.triage.domain.population.PopulationProfile;
import com.example.triage.infrastructure.persistence.model.DepartmentMappingRecord;
import com.example.triage.infrastructure.persistence.repository.HospitalDataRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LocalDepartmentMappingService {

    private static final Map<String, Double> SUPPORT_LEVEL_WEIGHT = Map.of(
            "PRIMARY", 1.0,
            "SECONDARY", 0.7,
            "OPTIONAL", 0.4
    );

    private final HospitalDataRepository hospitalDataRepository;
    private final DiseaseNormalizeService diseaseNormalizeService;

    public LocalDepartmentMappingService(HospitalDataRepository hospitalDataRepository, DiseaseNormalizeService diseaseNormalizeService) {
        this.hospitalDataRepository = hospitalDataRepository;
        this.diseaseNormalizeService = diseaseNormalizeService;
    }

    public List<DepartmentRecommendation> mapDepartments(List<CapabilityRecommendation> capabilities, PopulationProfile profile, String city) {
        List<String> capabilityCodes = capabilities.stream().map(CapabilityRecommendation::capabilityCode).toList();
        Map<String, CapabilityRecommendation> capabilityMap = capabilities.stream().collect(Collectors.toMap(CapabilityRecommendation::capabilityCode, Function.identity()));
        List<DepartmentMappingRecord> mappings = hospitalDataRepository.findDepartmentMappings(capabilityCodes, city);
        Map<Long, DepartmentRecommendation> result = new LinkedHashMap<>();
        for (DepartmentMappingRecord mapping : mappings) {
            CapabilityRecommendation capability = capabilityMap.get(mapping.capabilityCode());
            if (capability == null) {
                continue;
            }
            if (!diseaseNormalizeService.matchesGenderAndAge(mapping.genderRule(), mapping.ageMin(), mapping.ageMax(), profile.gender(), profile.age())) {
                continue;
            }
            List<String> crowdTags = diseaseNormalizeService.parseList(mapping.crowdTagsJson());
            if (!crowdTags.isEmpty() && profile.crowdTags().stream().noneMatch(crowdTags::contains)) {
                continue;
            }
            double supportWeight = SUPPORT_LEVEL_WEIGHT.getOrDefault(mapping.supportLevel(), 0.5);
            double score = capability.score() * (mapping.weight() == null ? 1D : mapping.weight()) + supportWeight;
            DepartmentRecommendation recommendation = new DepartmentRecommendation(
                    mapping.departmentId(),
                    mapping.hospitalName(),
                    mapping.departmentName(),
                    mapping.parentDepartmentName(),
                    mapping.capabilityCode(),
                    mapping.supportLevel(),
                    score
            );
            result.compute(mapping.departmentId(), (key, existing) -> existing == null || recommendation.score() > existing.score() ? recommendation : existing);
        }
        return result.values().stream()
                .sorted(Comparator.comparingDouble(DepartmentRecommendation::score).reversed())
                .limit(5)
                .toList();
    }
}
