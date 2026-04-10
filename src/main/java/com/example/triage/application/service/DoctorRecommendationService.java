package com.example.triage.application.service;

import com.example.triage.domain.capability.CapabilityRecommendation;
import com.example.triage.domain.hospital.DepartmentRecommendation;
import com.example.triage.domain.hospital.DoctorRecommendation;
import com.example.triage.domain.population.PopulationProfile;
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

    public DoctorRecommendationService(DoctorDataRepository doctorDataRepository, DiseaseNormalizeService diseaseNormalizeService) {
        this.doctorDataRepository = doctorDataRepository;
        this.diseaseNormalizeService = diseaseNormalizeService;
    }

    public List<DoctorRecommendation> recommendDoctors(List<DepartmentRecommendation> departments,
                                                       List<CapabilityRecommendation> capabilities,
                                                       PopulationProfile profile) {
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
            double score = (department == null ? 0D : department.score());
            if (capability != null) {
                score += capability.score() * (doctor.weight() == null ? 0.15 : doctor.weight());
            }
            if (doctor.specialtyText() != null && capability != null && doctor.specialtyText().contains(capability.capabilityName())) {
                score += 1.0;
            }
            if (doctor.title() != null && (doctor.title().contains("主任") || doctor.title().toLowerCase().contains("chief"))) {
                score += 0.6;
            }
            DoctorRecommendation recommendation = new DoctorRecommendation(
                    doctor.doctorId(),
                    doctor.doctorName(),
                    doctor.title(),
                    doctor.hospitalName(),
                    doctor.departmentName(),
                    doctor.specialtyText(),
                    score
            );
            recommendations.compute(doctor.doctorId(), (key, existing) -> existing == null || recommendation.score() > existing.score() ? recommendation : existing);
        }
        return recommendations.values().stream()
                .sorted(Comparator.comparingDouble(DoctorRecommendation::score).reversed())
                .limit(5)
                .toList();
    }
}
