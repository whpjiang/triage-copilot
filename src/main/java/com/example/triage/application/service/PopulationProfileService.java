package com.example.triage.application.service;

import com.example.triage.application.dto.TriageAssessRequest;
import com.example.triage.domain.population.AgeGroup;
import com.example.triage.domain.population.PopulationProfile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class PopulationProfileService {

    private static final int CHILD_MAX_AGE = 11;
    private static final int ADOLESCENT_MAX_AGE = 17;
    private static final int ELDERLY_MIN_AGE = 65;

    public PopulationProfile buildProfile(TriageAssessRequest request) {
        String gender = normalizeGender(request.getGender());
        Integer age = request.getAge();
        AgeGroup ageGroup = resolveAgeGroup(age);
        List<String> crowdTags = new ArrayList<>();
        crowdTags.add(ageGroup.name().toLowerCase(Locale.ROOT));

        String special = normalizeText(request.getSpecialCondition() + " " + request.getSymptoms());
        if (special.contains("pregnan") || special.contains("孕")) {
            crowdTags.add("pregnancy");
        }
        if (special.contains("移植") || special.contains("transplant")) {
            crowdTags.add("transplant_followup");
        }
        if (ageGroup == AgeGroup.CHILD) {
            crowdTags.add("child");
        }
        if (ageGroup == AgeGroup.ADOLESCENT) {
            crowdTags.add("adolescent");
        }
        if (ageGroup == AgeGroup.ELDERLY) {
            crowdTags.add("elderly");
        }
        return new PopulationProfile(gender, age, ageGroup, crowdTags.stream().distinct().toList());
    }

    public AgeGroup resolveAgeGroup(Integer age) {
        if (age == null || age <= CHILD_MAX_AGE) {
            return AgeGroup.CHILD;
        }
        if (age <= ADOLESCENT_MAX_AGE) {
            return AgeGroup.ADOLESCENT;
        }
        if (age < ELDERLY_MIN_AGE) {
            return AgeGroup.ADULT;
        }
        return AgeGroup.ELDERLY;
    }

    public String normalizeGender(String gender) {
        if (!StringUtils.hasText(gender)) {
            return "unknown";
        }
        String text = normalizeText(gender);
        if (text.contains("女") || text.contains("female") || text.contains("woman")) {
            return "female";
        }
        if (text.contains("男") || text.contains("male") || text.contains("man")) {
            return "male";
        }
        return "unknown";
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).trim();
    }
}
