package com.example.triage.domain.capability;

import java.util.List;

public record CapabilityRecommendation(
        String capabilityCode,
        String capabilityName,
        String capabilityType,
        String standardDeptCode,
        List<String> matchedDiseases,
        double score
) {
}
