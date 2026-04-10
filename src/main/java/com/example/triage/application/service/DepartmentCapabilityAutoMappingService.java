package com.example.triage.application.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class DepartmentCapabilityAutoMappingService {

    public List<CapabilityMappingSuggestion> suggest(String departmentName,
                                                     String parentDepartmentName,
                                                     String serviceScope,
                                                     String intro) {
        String text = normalize(departmentName) + " " + normalize(parentDepartmentName) + " "
                + normalize(serviceScope) + " " + normalize(intro);
        List<CapabilityMappingSuggestion> suggestions = new ArrayList<>();

        if (containsAny(text, "儿科", "儿童")) {
            suggestions.add(new CapabilityMappingSuggestion("cap_pediatrics", "PRIMARY", 1.00D));
        }
        if (containsAny(text, "发热") && containsAny(text, "儿", "儿童")) {
            suggestions.add(new CapabilityMappingSuggestion("cap_pediatric_fever_clinic", "PRIMARY", 1.15D));
        }
        if (containsAny(text, "老年")) {
            suggestions.add(new CapabilityMappingSuggestion("cap_geriatrics", "PRIMARY", 1.00D));
        }
        if (containsAny(text, "记忆", "认知")) {
            suggestions.add(new CapabilityMappingSuggestion("cap_memory_clinic", "PRIMARY", 1.20D));
        }
        if (containsAny(text, "妇科")) {
            suggestions.add(new CapabilityMappingSuggestion("cap_gynecology", "PRIMARY", 1.00D));
        }
        if (containsAny(text, "男科", "前列腺", "男性")) {
            suggestions.add(new CapabilityMappingSuggestion("cap_andrology", "PRIMARY", 1.00D));
        }
        if (containsAny(text, "排尿", "尿频", "尿急")) {
            suggestions.add(new CapabilityMappingSuggestion("cap_male_urinary_clinic", "SECONDARY", 1.10D));
        }
        if (containsAny(text, "骨科")) {
            suggestions.add(new CapabilityMappingSuggestion("cap_orthopedics", "PRIMARY", 0.95D));
        }
        if (containsAny(text, "脊柱")) {
            suggestions.add(new CapabilityMappingSuggestion("cap_spine_surgery", "PRIMARY", 1.10D));
        }
        if (containsAny(text, "腰腿痛", "脊柱疼痛", "腰痛")) {
            suggestions.add(new CapabilityMappingSuggestion("cap_spine_pain_clinic", "SECONDARY", 1.15D));
        }
        if (containsAny(text, "移植")) {
            suggestions.add(new CapabilityMappingSuggestion("cap_transplant_followup", "PRIMARY", 1.20D));
        }

        List<CapabilityMappingSuggestion> deduplicated = new ArrayList<>();
        for (CapabilityMappingSuggestion suggestion : suggestions) {
            boolean exists = deduplicated.stream().anyMatch(item -> item.capabilityCode().equals(suggestion.capabilityCode()));
            if (!exists) {
                deduplicated.add(suggestion);
            }
        }
        return deduplicated;
    }

    private boolean containsAny(String text, String... keywords) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(normalize(keyword))) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).trim();
    }

    public record CapabilityMappingSuggestion(String capabilityCode, String supportLevel, double weight) {
    }
}
