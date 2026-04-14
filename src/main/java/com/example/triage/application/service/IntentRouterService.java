package com.example.triage.application.service;

import com.example.triage.domain.triage.IntentType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
public class IntentRouterService {

    private static final List<String> HOSPITAL_KEYWORDS = List.of(
            "hospital", "which hospital", "nearby hospital", "best hospital",
            "医院", "哪家医院", "去哪个医院", "附近医院", "武汉哪个医院", "医院推荐"
    );
    private static final List<String> DEPARTMENT_KEYWORDS = List.of(
            "department", "clinic", "what department", "what clinic",
            "挂什么科", "看什么科", "看哪个科", "科室", "门诊", "亚科", "专病门诊"
    );
    private static final List<String> DOCTOR_KEYWORDS = List.of(
            "doctor", "expert", "specialist", "chief physician",
            "哪个医生", "医生推荐", "专家", "主任", "名医", "找谁看"
    );
    private static final List<String> SYMPTOM_KEYWORDS = List.of(
            "pain", "fever", "cough", "numb", "dizzy", "vomit", "swelling", "bleeding",
            "疼", "痛", "发热", "发烧", "咳嗽", "麻", "不舒服", "头晕", "腹泻", "呕吐", "胸闷", "气短"
    );

    public IntentType route(String message) {
        if (!StringUtils.hasText(message)) {
            return IntentType.UNKNOWN;
        }
        String normalized = message.trim().toLowerCase(Locale.ROOT);
        if (containsAny(normalized, DOCTOR_KEYWORDS)) {
            return IntentType.DIRECT_DOCTOR_QUERY;
        }
        if (containsAny(normalized, HOSPITAL_KEYWORDS)) {
            return IntentType.DIRECT_HOSPITAL_QUERY;
        }
        if (containsAny(normalized, DEPARTMENT_KEYWORDS)) {
            return IntentType.DIRECT_DEPARTMENT_QUERY;
        }
        if (containsAny(normalized, SYMPTOM_KEYWORDS)) {
            return IntentType.SYMPTOM_TRIAGE_QUERY;
        }
        return IntentType.UNKNOWN;
    }

    private boolean containsAny(String text, List<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
