package com.example.triage.application.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DepartmentCapabilityAutoMappingService {

    private final CapabilityAliasDictionaryService capabilityAliasDictionaryService;

    public DepartmentCapabilityAutoMappingService(CapabilityAliasDictionaryService capabilityAliasDictionaryService) {
        this.capabilityAliasDictionaryService = capabilityAliasDictionaryService;
    }

    public MappingEvaluation evaluate(String departmentName,
                                      String parentDepartmentName,
                                      String serviceScope,
                                      String intro) {
        List<CapabilityAliasDictionaryService.CapabilityAliasRule> directMatches =
                capabilityAliasDictionaryService.matchAll(departmentName);
        List<CapabilityAliasDictionaryService.CapabilityAliasRule> parentMatches =
                capabilityAliasDictionaryService.matchAll(parentDepartmentName);
        List<CapabilityAliasDictionaryService.CapabilityAliasRule> scopeMatches =
                capabilityAliasDictionaryService.matchAll(serviceScope, intro);

        Map<String, CapabilityMappingSuggestion> merged = new LinkedHashMap<>();
        mergeRules(merged, directMatches);
        mergeRules(merged, parentMatches);
        mergeRules(merged, scopeMatches);

        List<String> reviewIssues = new ArrayList<>();
        if (merged.isEmpty()) {
            reviewIssues.add("WAIT_CAPABILITY_MAPPING");
        }
        if (merged.size() > 1) {
            reviewIssues.add("AUTO_MAPPING_NEEDS_REVIEW");
        }
        if (hasConflict(directMatches, parentMatches)) {
            reviewIssues.add("PARENT_DEPARTMENT_CONFLICT");
        }
        if (hasConflict(directMatches, scopeMatches)) {
            reviewIssues.add("SERVICE_SCOPE_CONFLICT");
        }

        return new MappingEvaluation(new ArrayList<>(merged.values()), reviewIssues.stream().distinct().toList());
    }

    private void mergeRules(Map<String, CapabilityMappingSuggestion> merged,
                            List<CapabilityAliasDictionaryService.CapabilityAliasRule> rules) {
        for (CapabilityAliasDictionaryService.CapabilityAliasRule rule : rules) {
            merged.compute(rule.capabilityCode(), (key, existing) -> {
                CapabilityMappingSuggestion candidate =
                        new CapabilityMappingSuggestion(rule.capabilityCode(), rule.supportLevel(), rule.weight());
                if (existing == null) {
                    return candidate;
                }
                return existing.weight() >= candidate.weight() ? existing : candidate;
            });
        }
    }

    private boolean hasConflict(List<CapabilityAliasDictionaryService.CapabilityAliasRule> left,
                                List<CapabilityAliasDictionaryService.CapabilityAliasRule> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }
        Set<String> leftCodes = new LinkedHashSet<>();
        left.forEach(item -> leftCodes.add(item.capabilityCode()));
        boolean overlap = right.stream().map(CapabilityAliasDictionaryService.CapabilityAliasRule::capabilityCode).anyMatch(leftCodes::contains);
        return !overlap;
    }

    public record CapabilityMappingSuggestion(String capabilityCode, String supportLevel, double weight) {
    }

    public record MappingEvaluation(List<CapabilityMappingSuggestion> suggestions, List<String> reviewIssues) {
        public boolean hasAutoMapping() {
            return !suggestions.isEmpty();
        }

        public boolean needsReview() {
            return !reviewIssues.isEmpty();
        }

        public String reviewSuggestion() {
            if (reviewIssues.contains("PARENT_DEPARTMENT_CONFLICT")) {
                return "父级科室与当前科室推断能力冲突，请人工确认";
            }
            if (reviewIssues.contains("SERVICE_SCOPE_CONFLICT")) {
                return "科室名称与诊疗范围推断能力冲突，请人工确认";
            }
            if (reviewIssues.contains("AUTO_MAPPING_NEEDS_REVIEW")) {
                return "自动映射到多个医学能力，请人工复核";
            }
            return "请为本地科室补充或确认医学能力映射";
        }
    }
}
