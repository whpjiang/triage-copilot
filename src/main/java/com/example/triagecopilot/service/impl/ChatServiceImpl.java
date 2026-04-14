package com.example.triagecopilot.service.impl;

import com.example.triage.application.dto.TriageAssessRequest;
import com.example.triage.application.dto.TriageAssessResponse;
import com.example.triage.application.orchestrator.TriageDecisionOrchestrator;
import com.example.triagecopilot.dto.ChatRequest;
import com.example.triagecopilot.service.ChatService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatServiceImpl implements ChatService {

    private static final Pattern AGE_PATTERN = Pattern.compile("(\\d{1,3})\\s*(\\u5c81|year|years|yo)", Pattern.CASE_INSENSITIVE);
    private static final Pattern AGE_PREFIX_PATTERN = Pattern.compile("(age|\\u5e74\\u9f84)\\s*[:\\uFF1A]?\\s*(\\d{1,3})", Pattern.CASE_INSENSITIVE);
    private static final Pattern GENDER_MALE_PATTERN = Pattern.compile("\\b(male|man|boy)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern GENDER_FEMALE_PATTERN = Pattern.compile("\\b(female|woman|girl)\\b", Pattern.CASE_INSENSITIVE);

    private final TriageDecisionOrchestrator triageDecisionOrchestrator;

    public ChatServiceImpl(TriageDecisionOrchestrator triageDecisionOrchestrator) {
        this.triageDecisionOrchestrator = triageDecisionOrchestrator;
    }

    @Override
    public String chat(ChatRequest request) {
        if (!StringUtils.hasText(request.getMessage())) {
            return "Please enter symptoms before sending.";
        }

        String message = request.getMessage().trim();
        Integer age = extractAge(message);
        String gender = extractGender(message);

        if (age == null && !StringUtils.hasText(gender)) {
            return "Please provide age and gender first, for example: 35 years old, female, fever and cough for 3 days.";
        }
        if (age == null) {
            return "Please provide age, for example: 8 years old or 35 years old.";
        }
        if (!StringUtils.hasText(gender)) {
            return "Please provide gender: male or female.";
        }

        TriageAssessRequest assessRequest = new TriageAssessRequest();
        assessRequest.setSymptoms(message);
        assessRequest.setAge(age);
        assessRequest.setGender(gender);
        TriageAssessResponse result = triageDecisionOrchestrator.assess(assessRequest);

        String recommendedDepartment = result.getCapabilityRecommendations().isEmpty()
                ? "-"
                : nvl(result.getCapabilityRecommendations().get(0).getCapabilityName());
        String recommendedClinic = result.getDepartmentRecommendations().isEmpty()
                ? "-"
                : nvl(result.getDepartmentRecommendations().get(0).getDepartmentName());
        String urgency = result.getCandidateDiseases().isEmpty()
                ? "MEDIUM"
                : nvl(result.getCandidateDiseases().get(0).getUrgencyLevel()).toUpperCase();
        String reason = result.getCandidateDiseases().isEmpty()
                ? "Structured triage evaluation completed."
                : "Top candidate disease: " + nvl(result.getCandidateDiseases().get(0).getDiseaseName());
        String doctor = result.getDoctorRecommendations().isEmpty()
                ? "-"
                : nvl(result.getDoctorRecommendations().get(0).getDoctorName());

        return """
                Recommended Department: %s
                Recommended Clinic: %s
                Recommended Doctor: %s
                Urgency: %s
                Reason: %s
                Advice: %s
                """.formatted(
                recommendedDepartment,
                recommendedClinic,
                doctor,
                urgency,
                reason,
                nvl(result.getExplanation())
        );
    }

    private Integer extractAge(String text) {
        Matcher m = AGE_PATTERN.matcher(text);
        if (m.find()) {
            return parseAge(m.group(1));
        }
        Matcher m2 = AGE_PREFIX_PATTERN.matcher(text);
        if (m2.find()) {
            return parseAge(m2.group(2));
        }
        return null;
    }

    private Integer parseAge(String raw) {
        try {
            int age = Integer.parseInt(raw);
            if (age >= 0 && age <= 120) {
                return age;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String extractGender(String text) {
        String t = text.toLowerCase();
        if (t.contains("\u5973") || GENDER_FEMALE_PATTERN.matcher(t).find()) {
            return "female";
        }
        if (t.contains("\u7537") || GENDER_MALE_PATTERN.matcher(t).find()) {
            return "male";
        }
        return null;
    }

    private String nvl(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }
}
