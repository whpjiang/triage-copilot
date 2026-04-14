package com.example.triage.application.service;

import com.example.triage.domain.capability.CapabilityRecommendation;
import com.example.triage.domain.hospital.DepartmentRecommendation;
import com.example.triage.domain.hospital.DoctorRecommendation;
import com.example.triage.domain.population.PopulationProfile;
import com.example.triage.domain.triage.RecommendationContext;
import com.example.triage.infrastructure.persistence.model.DoctorRecord;
import com.example.triage.infrastructure.persistence.repository.DoctorDataRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DoctorRecommendationService {

    private final DoctorDataRepository doctorDataRepository;
    private final DiseaseNormalizeService diseaseNormalizeService;
    private final RecommendationPolicyService recommendationPolicyService;

    public DoctorRecommendationService(DoctorDataRepository doctorDataRepository,
                                       DiseaseNormalizeService diseaseNormalizeService,
                                       RecommendationPolicyService recommendationPolicyService) {
        this.doctorDataRepository = doctorDataRepository;
        this.diseaseNormalizeService = diseaseNormalizeService;
        this.recommendationPolicyService = recommendationPolicyService;
    }

    public List<DoctorRecommendation> recommendDoctors(List<DepartmentRecommendation> departments,
                                                       List<CapabilityRecommendation> capabilities,
                                                       PopulationProfile profile,
                                                       RecommendationContext context) {
        List<Long> departmentIds = departments.stream().map(DepartmentRecommendation::departmentId).toList();
        Map<String, CapabilityRecommendation> capabilityMap = capabilities.stream()
                .collect(Collectors.toMap(CapabilityRecommendation::capabilityCode, Function.identity(), (a, b) -> a));
        Map<Long, DepartmentRecommendation> departmentMap = departments.stream()
                .collect(Collectors.toMap(DepartmentRecommendation::departmentId, Function.identity(), (a, b) -> a));
        List<DoctorRecord> doctors = doctorDataRepository.findDoctorsByDepartmentIds(departmentIds);
        Map<Long, DoctorRecommendation> recommendations = new LinkedHashMap<>();
        for (DoctorRecord doctor : doctors) {
            if (!diseaseNormalizeService.matchesGenderAndAge(doctor.genderRule(), doctor.ageMin(), doctor.ageMax(), profile.gender(), profile.age())) {
                continue;
            }
            List<String> crowdTags = diseaseNormalizeService.parseList(doctor.crowdTagsJson());
            if (!crowdTags.isEmpty() && profile.crowdTags().stream().noneMatch(crowdTags::contains)) {
                continue;
            }
            DepartmentRecommendation department = departmentMap.get(doctor.departmentId());
            CapabilityRecommendation capability = doctor.capabilityCode() == null ? null : capabilityMap.get(doctor.capabilityCode());
            double departmentScore = department == null ? 0D : recommendationPolicyService.normalizeClinicalScore(department.score());
            double capabilityScore = capability == null
                    ? departmentScore
                    : recommendationPolicyService.normalizeClinicalScore(capability.score() * (doctor.weight() == null ? 0.15D : doctor.weight()));
            double clinicalMatchScore = Math.max(departmentScore, capabilityScore);
            double authorityScore = recommendationPolicyService.normalizeAuthorityScore(resolveAuthorityScore(doctor));
            double distanceScore = recommendationPolicyService.calculateDistanceScore(
                    context.area(),
                    context.latitude(),
                    context.longitude(),
                    doctor.districtName(),
                    doctor.latitude(),
                    doctor.longitude()
            );
            double profileFitScore = 0.55D;
            if (doctor.specialtyText() != null && capability != null
                    && doctor.specialtyText().toLowerCase().contains(capability.capabilityName().toLowerCase())) {
                profileFitScore += 0.2D;
            }
            if (doctor.isExpert() != null && doctor.isExpert() == 1) {
                profileFitScore += 0.15D;
            }
            if (doctor.title() != null && doctor.title().toLowerCase().contains("chief")) {
                profileFitScore += 0.10D;
            }
            double score = recommendationPolicyService.calculateDoctorScore(
                    clinicalMatchScore,
                    authorityScore,
                    distanceScore,
                    recommendationPolicyService.normalizeProfileFit(profileFitScore),
                    context
            );
            DoctorRecommendation recommendation = new DoctorRecommendation(
                    doctor.doctorId(),
                    doctor.doctorName(),
                    doctor.title(),
                    doctor.hospitalName(),
                    doctor.departmentName(),
                    doctor.specialtyText(),
                    doctor.campusName(),
                    resolveAuthorityScore(doctor),
                    score
            );
            recommendations.compute(doctor.doctorId(), (key, existing) -> existing == null || recommendation.score() > existing.score() ? recommendation : existing);
        }
        return recommendations.values().stream()
                .sorted(Comparator.comparingDouble(DoctorRecommendation::score).reversed())
                .limit(5)
                .toList();
    }

    private Double resolveAuthorityScore(DoctorRecord doctor) {
        double authority = doctor.authorityScore() == null ? 0D : doctor.authorityScore();
        if (doctor.academicTitleScore() != null) {
            authority += doctor.academicTitleScore();
        }
        if (doctor.isExpert() != null && doctor.isExpert() == 1) {
            authority += 8D;
        }
        return authority;
    }
}
