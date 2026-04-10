package com.example.triage.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class DiseaseNormalizeService {

    private final ObjectMapper objectMapper;

    public DiseaseNormalizeService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean matchesGenderAndAge(String genderRule, Integer ageMin, Integer ageMax, String gender, Integer age) {
        String normalizedRule = normalizeGenderRule(genderRule);
        if ("male_only".equals(normalizedRule) && !"male".equals(gender)) {
            return false;
        }
        if ("female_only".equals(normalizedRule) && !"female".equals(gender)) {
            return false;
        }
        if (age != null && ageMin != null && age < ageMin) {
            return false;
        }
        if (age != null && ageMax != null && age > ageMax) {
            return false;
        }
        return true;
    }

    public List<String> parseList(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        String trimmed = text.trim();
        try {
            if (trimmed.startsWith("[")) {
                return objectMapper.readValue(trimmed, new TypeReference<List<String>>() {
                }).stream().map(this::normalizeText).filter(StringUtils::hasText).distinct().toList();
            }
        } catch (Exception ignored) {
        }
        return Arrays.stream(trimmed.split("[,，|/；;\\s]+"))
                .map(this::normalizeText)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    public String toJsonArray(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (Exception ex) {
            return "[]";
        }
    }

    public List<String> normalizeKeywords(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        String normalized = normalizeText(text);
        for (String token : normalized.split("[,，|/；;\\s]+")) {
            if (token.length() > 1) {
                values.add(token);
            }
        }
        return values.stream().distinct().toList();
    }

    public String normalizeGenderRule(String genderRule) {
        String value = normalizeText(genderRule);
        if (!StringUtils.hasText(value) || "all".equals(value) || "any".equals(value) || "unknown".equals(value)) {
            return "all";
        }
        if (value.contains("female") || value.contains("女")) {
            return "female_only";
        }
        if (value.contains("male") || value.contains("男")) {
            return "male_only";
        }
        return "all";
    }

    public String normalizeText(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).trim();
    }
}
