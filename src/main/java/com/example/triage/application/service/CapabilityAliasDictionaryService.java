package com.example.triage.application.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CapabilityAliasDictionaryService {

    private final DiseaseNormalizeService diseaseNormalizeService;
    private final Map<String, CapabilityAliasRule> aliasRules;

    public CapabilityAliasDictionaryService(DiseaseNormalizeService diseaseNormalizeService) {
        this.diseaseNormalizeService = diseaseNormalizeService;
        this.aliasRules = buildAliasRules();
    }

    public List<CapabilityAliasRule> matchAll(String... texts) {
        Map<String, CapabilityAliasRule> matched = new LinkedHashMap<>();
        for (String text : texts) {
            String normalized = diseaseNormalizeService.normalizeText(text);
            if (!StringUtils.hasText(normalized)) {
                continue;
            }
            for (Map.Entry<String, CapabilityAliasRule> entry : aliasRules.entrySet()) {
                if (normalized.contains(entry.getKey())) {
                    matched.compute(entry.getValue().capabilityCode(), (key, existing) -> {
                        CapabilityAliasRule candidate = entry.getValue();
                        if (existing == null) {
                            return candidate;
                        }
                        return existing.weight() >= candidate.weight() ? existing : candidate;
                    });
                }
            }
        }
        return matched.values().stream()
                .sorted(Comparator.comparingDouble(CapabilityAliasRule::weight).reversed())
                .toList();
    }

    public List<String> mapHintToCapabilityCodes(String hint) {
        return matchAll(hint).stream().map(CapabilityAliasRule::capabilityCode).distinct().toList();
    }

    private Map<String, CapabilityAliasRule> buildAliasRules() {
        Map<String, CapabilityAliasRule> rules = new LinkedHashMap<>();
        register(rules, "cap_pediatrics", "PRIMARY", 1.00D, "儿科", "儿童门诊", "儿内科");
        register(rules, "cap_pediatric_fever_clinic", "PRIMARY", 1.20D, "儿童发热门诊", "发热门诊", "儿科发热");
        register(rules, "cap_geriatrics", "PRIMARY", 1.00D, "老年病科", "老年综合门诊", "老年综合评估");
        register(rules, "cap_memory_clinic", "PRIMARY", 1.20D, "记忆门诊", "记忆障碍门诊", "认知门诊");
        register(rules, "cap_gynecology", "PRIMARY", 1.00D, "妇科", "妇产科");
        register(rules, "cap_andrology", "PRIMARY", 1.00D, "男科");
        register(rules, "cap_male_urinary_clinic", "SECONDARY", 1.15D, "排尿异常门诊", "前列腺门诊", "前列腺专病", "男性排尿异常门诊");
        register(rules, "cap_orthopedics", "PRIMARY", 0.95D, "骨科");
        register(rules, "cap_spine_surgery", "PRIMARY", 1.10D, "脊柱外科", "脊柱门诊", "脊柱专科", "脊柱");
        register(rules, "cap_spine_pain_clinic", "SECONDARY", 1.20D, "脊柱疼痛门诊", "腰腿痛门诊", "脊柱疼痛专病门诊");
        register(rules, "cap_transplant_followup", "PRIMARY", 1.20D, "器官移植随访门诊", "移植门诊", "移植复查门诊", "移植随访");
        return rules;
    }

    private void register(Map<String, CapabilityAliasRule> rules,
                          String capabilityCode,
                          String supportLevel,
                          double weight,
                          String... aliases) {
        for (String alias : aliases) {
            String normalized = alias.toLowerCase(Locale.ROOT).trim();
            rules.put(normalized, new CapabilityAliasRule(normalized, capabilityCode, supportLevel, weight));
        }
    }

    public record CapabilityAliasRule(String alias, String capabilityCode, String supportLevel, double weight) {
    }
}
