package com.example.triage.application.service;

import com.example.triage.application.dto.TriageAssessRequest;
import com.example.triage.domain.disease.DiseaseCandidate;
import com.example.triage.domain.triage.IntentType;
import com.example.triage.domain.triage.RecommendationContext;
import com.example.triage.domain.triage.RouteType;
import com.example.triage.domain.triage.SeverityLevel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class RecommendationPolicyService {

    public RecommendationContext buildContext(TriageAssessRequest request,
                                              SeverityLevel severityLevel,
                                              boolean commonDisease,
                                              IntentType intentType) {
        return new RecommendationContext(
                request.getArea(),
                Boolean.TRUE.equals(request.getNearby()),
                severityLevel,
                commonDisease,
                intentType,
                request.getAge(),
                request.getGender(),
                request.getLatitude(),
                request.getLongitude()
        );
    }

    public RouteType resolveRouteType(RecommendationContext context) {
        if (context == null) {
            return RouteType.FALLBACK;
        }
        if (context.intentType() != null && context.intentType() != IntentType.SYMPTOM_TRIAGE_QUERY) {
            return RouteType.DIRECT_ENTITY;
        }
        if (context.severityLevel() == SeverityLevel.EMERGENT || context.severityLevel() == SeverityLevel.HIGH) {
            return RouteType.AUTHORITY;
        }
        if (context.nearbyRequested()) {
            return RouteType.NEARBY;
        }
        if (StringUtils.hasText(context.area()) && context.commonDisease()) {
            return RouteType.NEARBY;
        }
        if (context.commonDisease() && context.severityLevel() == SeverityLevel.LOW && context.nearbyRequested()) {
            return RouteType.NEARBY;
        }
        return RouteType.AUTHORITY;
    }

    public SeverityLevel resolveSeverityLevel(List<DiseaseCandidate> diseases) {
        if (diseases == null || diseases.isEmpty()) {
            return SeverityLevel.MEDIUM;
        }
        return diseases.stream()
                .map(DiseaseCandidate::urgencyLevel)
                .map(this::mapUrgencyLevel)
                .max(Enum::compareTo)
                .orElse(SeverityLevel.MEDIUM);
    }

    public boolean isCommonDisease(List<DiseaseCandidate> diseases) {
        if (diseases == null || diseases.isEmpty()) {
            return false;
        }
        DiseaseCandidate top = diseases.getFirst();
        SeverityLevel topSeverity = mapUrgencyLevel(top.urgencyLevel());
        if (topSeverity == SeverityLevel.HIGH || topSeverity == SeverityLevel.EMERGENT) {
            return false;
        }
        return top.score() >= 1.2D;
    }

    public double calculateDepartmentScore(double clinicalMatchScore,
                                           double authorityScore,
                                           double distanceScore,
                                           double profileFitScore,
                                           RecommendationContext context) {
        RouteType routeType = resolveRouteType(context);
        double score = routeType == RouteType.NEARBY
                ? clinicalMatchScore * 0.50 + distanceScore * 0.25 + authorityScore * 0.15 + profileFitScore * 0.10
                : clinicalMatchScore * 0.45 + authorityScore * 0.30 + distanceScore * 0.10 + profileFitScore * 0.15;
        return roundScore(score * 10D);
    }

    public double calculateDoctorScore(double clinicalMatchScore,
                                       double authorityScore,
                                       double distanceScore,
                                       double profileFitScore,
                                       RecommendationContext context) {
        RouteType routeType = resolveRouteType(context);
        double score = routeType == RouteType.NEARBY
                ? clinicalMatchScore * 0.50 + distanceScore * 0.25 + authorityScore * 0.15 + profileFitScore * 0.10
                : clinicalMatchScore * 0.45 + authorityScore * 0.30 + distanceScore * 0.10 + profileFitScore * 0.15;
        return roundScore(score * 10D);
    }

    public double calculateDistanceScore(String requestArea,
                                         Double requestLatitude,
                                         Double requestLongitude,
                                         String candidateArea,
                                         Double candidateLatitude,
                                         Double candidateLongitude) {
        if (StringUtils.hasText(requestArea) && StringUtils.hasText(candidateArea)) {
            return requestArea.trim().equalsIgnoreCase(candidateArea.trim()) ? 1.0D : 0.45D;
        }
        if (requestLatitude != null && requestLongitude != null && candidateLatitude != null && candidateLongitude != null) {
            double latDiff = requestLatitude - candidateLatitude;
            double lngDiff = requestLongitude - candidateLongitude;
            double distance = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff);
            return clamp(1.0D - distance * 8D);
        }
        return 0.55D;
    }

    public double normalizeAuthorityScore(Double authorityScore) {
        if (authorityScore == null) {
            return 0.35D;
        }
        return clamp(authorityScore / 100D);
    }

    public double normalizeClinicalScore(Double clinicalScore) {
        if (clinicalScore == null) {
            return 0D;
        }
        return clamp(clinicalScore / 5D);
    }

    public double normalizeProfileFit(double profileFit) {
        return clamp(profileFit);
    }

    private SeverityLevel mapUrgencyLevel(String urgencyLevel) {
        if (!StringUtils.hasText(urgencyLevel)) {
            return SeverityLevel.MEDIUM;
        }
        return switch (urgencyLevel.trim().toUpperCase()) {
            case "EMERGENT", "EMERGENCY", "CRITICAL" -> SeverityLevel.EMERGENT;
            case "HIGH", "URGENT" -> SeverityLevel.HIGH;
            case "LOW", "ROUTINE" -> SeverityLevel.LOW;
            default -> SeverityLevel.MEDIUM;
        };
    }

    private double roundScore(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private double clamp(double value) {
        if (value < 0D) {
            return 0D;
        }
        return Math.min(value, 1D);
    }
}
