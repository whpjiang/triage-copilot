package com.example.triage.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class DiseaseNormalizeService {

    private static final int MAX_ALIAS_LENGTH = 80;
    private static final int MAX_KEYWORD_LENGTH = 40;

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
                }).stream()
                        .map(this::cleanListToken)
                        .filter(StringUtils::hasText)
                        .filter(value -> value.length() <= MAX_ALIAS_LENGTH)
                        .distinct()
                        .toList();
            }
        } catch (Exception ignored) {
        }
        return splitTokens(trimmed, MAX_ALIAS_LENGTH);
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
        Set<String> values = new LinkedHashSet<>();
        for (String token : splitTokens(text, MAX_KEYWORD_LENGTH)) {
            if (token.length() < 2) {
                continue;
            }
            values.add(token);
        }
        return new ArrayList<>(values);
    }

    public String normalizeGenderRule(String genderRule) {
        String value = normalizeText(genderRule);
        if (!StringUtils.hasText(value)
                || "all".equals(value)
                || "any".equals(value)
                || "unknown".equals(value)
                || value.contains("不限")
                || value.contains("通用")
                || value.contains("男女")
                || value.contains("全人群")) {
            return "all";
        }
        if (value.contains("female") || value.contains("女") || value.contains("妇")) {
            return "female_only";
        }
        if (value.contains("male") || value.contains("男") || value.contains("前列腺") || value.contains("男科")) {
            return "male_only";
        }
        return "all";
    }

    public String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT)
                .replace('\u3000', ' ')
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replace('（', '(')
                .replace('）', ')')
                .replace('【', '[')
                .replace('】', ']')
                .trim();
    }

    private List<String> splitTokens(String text, int maxLength) {
        String normalized = normalizeText(text)
                .replace('；', ',')
                .replace(';', ',')
                .replace('、', ',')
                .replace('|', ',')
                .replace('，', ',')
                .replace('/', ',')
                .replace('\\', ',')
                .replace('：', ',')
                .replace(':', ',');
        String[] rawTokens = normalized.split("[,\\s]+");
        Set<String> values = new LinkedHashSet<>();
        for (String rawToken : rawTokens) {
            String token = cleanListToken(rawToken);
            if (!StringUtils.hasText(token)) {
                continue;
            }
            if (token.length() > maxLength) {
                continue;
            }
            values.add(token);
        }
        return new ArrayList<>(values);
    }

    private String cleanListToken(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String token = normalizeText(value)
                .replace("\"", "")
                .replace("'", "")
                .replace("[", "")
                .replace("]", "")
                .replace("(", "")
                .replace(")", "")
                .replace("{", "")
                .replace("}", "")
                .trim();
        return token;
    }
}
