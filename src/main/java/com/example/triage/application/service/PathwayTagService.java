package com.example.triage.application.service;

import com.example.triage.domain.population.AgeGroup;
import com.example.triage.domain.population.PopulationProfile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class PathwayTagService {

    public List<String> inferPathwayTags(String symptoms, PopulationProfile profile) {
        String text = symptoms == null ? "" : symptoms.toLowerCase(Locale.ROOT);
        List<String> tags = new ArrayList<>();

        if ((profile.ageGroup() == AgeGroup.CHILD || profile.ageGroup() == AgeGroup.ADOLESCENT)
                && containsAny(text, "发热", "发烧", "fever")) {
            tags.add("child_fever_pathway");
        }
        if (profile.ageGroup() == AgeGroup.ELDERLY && hasMultiple(text, "记忆", "头晕", "乏力", "失眠", "步态", "反应慢")) {
            tags.add("elderly_multisymptom_pathway");
        }
        if (containsAny(text, "腰痛", "腰腿痛", "下肢麻木", "放射痛", "spine")) {
            tags.add("spine_pathway");
        }
        if (containsAny(text, "移植", "排异", "免疫抑制剂", "transplant")) {
            tags.add("transplant_followup");
        }
        if ("female".equals(profile.gender()) && containsAny(text, "下腹痛", "盆腔", "白带")) {
            tags.add("female_pelvic_pathway");
        }
        if ("male".equals(profile.gender()) && containsAny(text, "尿频", "尿急", "排尿", "夜尿")) {
            tags.add("male_urinary_pathway");
        }
        return tags.stream().distinct().toList();
    }

    private boolean hasMultiple(String text, String... keywords) {
        int count = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                count++;
            }
        }
        return count >= 2;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
