package com.example.triage.application.service;

import com.example.triage.domain.triage.SeverityLevel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class SeverityStratificationService {

    private static final List<String> EMERGENT_KEYWORDS = List.of(
            "剧烈胸痛", "胸痛", "呼吸困难", "昏迷", "抽搐", "一侧肢体无力", "言语不清",
            "大出血", "呕血", "黑便", "剧烈腹痛", "孕妇腹痛伴出血", "高热不退伴精神差", "意识障碍",
            "chest pain", "shortness of breath", "coma", "convulsion", "major bleeding",
            "hematemesis", "melena", "severe abdominal pain", "altered mental status", "one-sided weakness"
    );
    private static final List<String> HIGH_RISK_KEYWORDS = List.of(
            "持续高热", "明显进行性加重", "进行性加重", "反复高热", "精神差", "移植术后异常", "术后异常",
            "persistent high fever", "progressive worsening", "post transplant", "postoperative complication"
    );
    private static final List<String> LOW_RISK_HINTS = List.of(
            "轻微", "偶尔", "一般", "普通", "轻度",
            "mild", "occasional", "common", "slight"
    );

    public SeverityAssessment assess(String symptoms, Integer age, String gender) {
        String normalized = normalize(symptoms);
        List<String> emergentMatches = collectMatches(normalized, EMERGENT_KEYWORDS);
        if (!emergentMatches.isEmpty()) {
            return new SeverityAssessment(SeverityLevel.EMERGENT, true, emergentMatches);
        }

        List<String> highMatches = new ArrayList<>(collectMatches(normalized, HIGH_RISK_KEYWORDS));
        if (age != null && age <= 6 && containsAny(normalized, List.of("高热", "抽搐", "呼吸困难", "持续呕吐"))) {
            highMatches.add("child_high_risk");
        }
        if (age != null && age >= 75 && containsAny(normalized, List.of("胸痛", "气短", "意识", "头晕", "腹痛"))) {
            highMatches.add("elderly_high_risk");
        }
        if (!highMatches.isEmpty()) {
            return new SeverityAssessment(SeverityLevel.HIGH, false, highMatches);
        }

        if (containsAny(normalized, LOW_RISK_HINTS)) {
            return new SeverityAssessment(SeverityLevel.LOW, false, List.of());
        }
        if (StringUtils.hasText(normalized)) {
            return new SeverityAssessment(SeverityLevel.LOW, false, List.of());
        }
        return new SeverityAssessment(SeverityLevel.MEDIUM, false, List.of());
    }

    private List<String> collectMatches(String text, List<String> keywords) {
        List<String> matches = new ArrayList<>();
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                matches.add(keyword);
            }
        }
        return matches;
    }

    private boolean containsAny(String text, List<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }

    public record SeverityAssessment(
            SeverityLevel severityLevel,
            boolean emergencyMatched,
            List<String> matchedKeywords
    ) {
    }
}
