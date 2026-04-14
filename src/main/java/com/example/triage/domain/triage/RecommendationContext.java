package com.example.triage.domain.triage;

public record RecommendationContext(
        String area,
        boolean nearbyRequested,
        SeverityLevel severityLevel,
        boolean commonDisease,
        IntentType intentType,
        Integer age,
        String gender,
        Double latitude,
        Double longitude
) {
}
