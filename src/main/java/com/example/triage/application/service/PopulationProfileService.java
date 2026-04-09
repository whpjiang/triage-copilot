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
        if (ageGroup == AgeGroup.ELDERLY) {
            crowdTags.add("elderly");
        }
        if (ageGroup == AgeGroup.CHILD) {
            crowdTags.add("child");
        }
        return new PopulationProfile(gender, age, ageGroup, crowdTags.stream().distinct().toList());
    }

    public AgeGroup resolveAgeGroup(Integer age) {
        if (age == null || age < 14) {
            return AgeGroup.CHILD;
        }
        if (age < 18) {
            return AgeGroup.ADOLESCENT;
        }
        if (age < 65) {
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
