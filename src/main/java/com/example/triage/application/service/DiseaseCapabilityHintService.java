package com.example.triage.application.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DiseaseCapabilityHintService {

    private final CapabilityAliasDictionaryService capabilityAliasDictionaryService;

    public DiseaseCapabilityHintService(CapabilityAliasDictionaryService capabilityAliasDictionaryService) {
        this.capabilityAliasDictionaryService = capabilityAliasDictionaryService;
    }

    public HintMappingResult map(String standardDeptHint) {
        if (!StringUtils.hasText(standardDeptHint)) {
            return new HintMappingResult(List.of(), "MISSING_STANDARD_DEPT_HINT");
        }
        List<String> capabilityCodes = capabilityAliasDictionaryService.mapHintToCapabilityCodes(standardDeptHint);
        if (capabilityCodes.isEmpty()) {
            return new HintMappingResult(List.of(), "DISEASE_CAPABILITY_UNMAPPED");
        }
        if (capabilityCodes.size() > 1) {
            return new HintMappingResult(capabilityCodes, "DISEASE_CAPABILITY_MULTI_MATCH");
        }
        return new HintMappingResult(capabilityCodes, null);
    }

    public record HintMappingResult(List<String> capabilityCodes, String reviewIssueType) {
        public boolean mapped() {
            return !capabilityCodes.isEmpty() && reviewIssueType == null;
        }
    }
}
