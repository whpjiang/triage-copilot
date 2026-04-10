package com.example.triage.application.service;

import org.springframework.stereotype.Service;

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
        mergeRules(merged, directMatches, "department_name");
        mergeRules(merged, parentMatches, "parent_department_name");
        mergeRules(merged, scopeMatches, "service_scope");

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
                            List<CapabilityAliasDictionaryService.CapabilityAliasRule> rules,
                            String evidenceSource) {
        for (CapabilityAliasDictionaryService.CapabilityAliasRule rule : rules) {
            merged.compute(rule.capabilityCode(), (key, existing) -> {
                Set<String> evidences = new LinkedHashSet<>();
                evidences.add(evidenceSource + ":" + rule.alias());
                CapabilityMappingSuggestion candidate =
                        new CapabilityMappingSuggestion(rule.capabilityCode(), rule.supportLevel(), rule.weight(), new ArrayList<>(evidences));
                if (existing == null) {
                    return candidate;
                }
                Set<String> mergedEvidence = new LinkedHashSet<>(existing.evidence());
                mergedEvidence.addAll(candidate.evidence());
                return new CapabilityMappingSuggestion(
                        existing.capabilityCode(),
                        existing.weight() >= candidate.weight() ? existing.supportLevel() : candidate.supportLevel(),
                        Math.max(existing.weight(), candidate.weight()),
                        new ArrayList<>(mergedEvidence)
                );
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
        boolean overlap = right.stream()
                .map(CapabilityAliasDictionaryService.CapabilityAliasRule::capabilityCode)
                .anyMatch(leftCodes::contains);
        return !overlap;
    }

    public record CapabilityMappingSuggestion(String capabilityCode,
                                              String supportLevel,
                                              double weight,
                                              List<String> evidence) {
    }

    public record MappingEvaluation(List<CapabilityMappingSuggestion> suggestions, List<String> reviewIssues) {
        public boolean hasAutoMapping() {
            return !suggestions.isEmpty();
        }

        public boolean needsReview() {
            return !reviewIssues.isEmpty();
        }

        public List<String> matchedCapabilityCodes() {
            return suggestions.stream().map(CapabilityMappingSuggestion::capabilityCode).toList();
        }

        public List<String> evidenceSummary() {
            return suggestions.stream()
                    .flatMap(item -> item.evidence().stream())
                    .distinct()
                    .toList();
        }

        public String reviewSuggestion() {
            String details = "matched=" + matchedCapabilityCodes() + "; evidence=" + evidenceSummary();
            if (reviewIssues.contains("PARENT_DEPARTMENT_CONFLICT")) {
                return "父级科室与当前科室推断能力冲突，请人工确认; " + details;
            }
            if (reviewIssues.contains("SERVICE_SCOPE_CONFLICT")) {
                return "科室名称与诊疗范围推断能力冲突，请人工确认; " + details;
            }
            if (reviewIssues.contains("AUTO_MAPPING_NEEDS_REVIEW")) {
                return "自动映射到多个医学能力，请人工复核; " + details;
            }
            return "请为本地科室补充或确认医学能力映射; " + details;
        }
    }
}
