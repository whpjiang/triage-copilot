package com.example.triage.application.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class SymptomClarificationService {

    private static final int MAX_ASK_ROUND = 3;

    private static final List<String> INVALID_ANSWERS = List.of(
            "不知道", "说不清", "随便", "你看着办", "都行", "就这样", "不想说",
            "not sure", "don't know", "whatever", "anything", "up to you"
    );
    private static final List<String> SYMPTOM_WORDS = List.of(
            "疼", "痛", "发烧", "发热", "咳嗽", "麻", "腹泻", "呕吐", "头晕", "胸闷", "气短",
            "pain", "fever", "cough", "numb", "dizzy", "diarrhea", "vomit", "shortness of breath"
    );
    private static final List<String> BODY_PART_WORDS = List.of(
            "头", "胸", "腹", "胃", "腰", "背", "腿", "膝", "咽", "喉", "肩", "颈", "眼", "耳", "鼻",
            "head", "chest", "abdomen", "stomach", "waist", "back", "leg", "knee", "throat", "neck", "eye", "ear", "nose"
    );
    private static final List<String> CROWD_TAG_WORDS = List.of(
            "儿童", "小孩", "老人", "老年", "孕妇", "产后", "术后", "移植", "婴儿",
            "child", "children", "elderly", "pregnant", "postpartum", "postoperative", "transplant"
    );
    private static final List<String> SEVERITY_WORDS = List.of(
            "剧烈", "严重", "加重", "明显", "持续高热",
            "severe", "worse", "progressive", "persistent high fever"
    );
    private static final Pattern DURATION_PATTERN = Pattern.compile(
            "(\\d+\\s*(天|周|个月|月|年|小时|hour|hours|day|days|week|weeks|month|months|year|years))|(两天|三天|一周|两周|半年|今天|昨天|最近)",
            Pattern.CASE_INSENSITIVE
    );

    public ClarificationResult evaluate(String currentMessage,
                                        List<String> accumulatedMessages,
                                        List<String> previousMissingSlots,
                                        Integer askRound,
                                        Integer invalidAnswerCount) {
        String normalizedCurrent = normalize(currentMessage);
        if (!StringUtils.hasText(normalizedCurrent)) {
            List<String> missingSlots = previousMissingSlots.isEmpty() ? List.of("main_symptom", "duration") : previousMissingSlots;
            return new ClarificationResult(false, true, nextInvalidCount(invalidAnswerCount) >= 2, missingSlots, buildQuestion(missingSlots));
        }

        if (isInvalidAnswer(normalizedCurrent)) {
            List<String> missingSlots = previousMissingSlots.isEmpty() ? List.of("main_symptom", "body_part", "duration") : previousMissingSlots;
            return new ClarificationResult(false, true, nextInvalidCount(invalidAnswerCount) >= 2, missingSlots, buildQuestion(missingSlots));
        }

        String mergedText = String.join(" ; ", accumulatedMessages);
        List<String> missingSlots = detectMissingSlots(mergedText);
        boolean enoughInfo = missingSlots.isEmpty();
        boolean shouldFallback = !enoughInfo && (askRound != null && askRound >= MAX_ASK_ROUND);
        return new ClarificationResult(enoughInfo, false, shouldFallback, missingSlots, enoughInfo ? null : buildQuestion(missingSlots));
    }

    public List<String> mergeSymptoms(List<String> storedSymptoms, String currentMessage) {
        Set<String> merged = new LinkedHashSet<>();
        if (storedSymptoms != null) {
            for (String item : storedSymptoms) {
                if (StringUtils.hasText(item)) {
                    merged.add(item.trim());
                }
            }
        }
        if (StringUtils.hasText(currentMessage)) {
            merged.add(currentMessage.trim());
        }
        return new ArrayList<>(merged);
    }

    private List<String> detectMissingSlots(String mergedText) {
        List<String> missingSlots = new ArrayList<>();
        if (!containsAny(mergedText, SYMPTOM_WORDS)) {
            missingSlots.add("main_symptom");
        }
        if (!containsAny(mergedText, BODY_PART_WORDS)) {
            missingSlots.add("body_part");
        }
        if (!DURATION_PATTERN.matcher(mergedText).find()) {
            missingSlots.add("duration");
        }
        if (!containsAny(mergedText, SEVERITY_WORDS)) {
            missingSlots.add("severity_desc");
        }
        return missingSlots;
    }

    private boolean isInvalidAnswer(String text) {
        return containsAny(text, INVALID_ANSWERS);
    }

    private int nextInvalidCount(Integer invalidAnswerCount) {
        return (invalidAnswerCount == null ? 0 : invalidAnswerCount) + 1;
    }

    private String buildQuestion(List<String> missingSlots) {
        if (missingSlots.contains("main_symptom") && missingSlots.contains("body_part") && missingSlots.contains("duration")) {
            return "Please describe the main symptom, the body part involved, and how long it has lasted.";
        }
        if (missingSlots.contains("body_part") && missingSlots.contains("duration")) {
            return "Which body part is mainly affected, and how long has it lasted?";
        }
        if (missingSlots.contains("duration")) {
            return "How long has this been going on?";
        }
        if (missingSlots.contains("body_part")) {
            return "Which body part is mainly affected?";
        }
        if (missingSlots.contains("severity_desc")) {
            return "Is it getting worse, severe, or affecting daily activity?";
        }
        return "Please add a little more detail so I can continue the triage.";
    }

    private boolean containsAny(String text, List<String> keywords) {
        String normalized = normalize(text);
        for (String keyword : keywords) {
            if (normalized.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }

    public record ClarificationResult(
            boolean enoughInfo,
            boolean invalidAnswer,
            boolean shouldFallback,
            List<String> missingSlots,
            String followUpQuestion
    ) {
    }
}
