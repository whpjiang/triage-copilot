package com.example.triage.application.orchestrator;

import com.example.triage.application.dto.RecommendationCardDto;
import com.example.triage.application.dto.TriageAssessRequest;
import com.example.triage.application.dto.TriageChatRequest;
import com.example.triage.application.dto.TriageChatResponse;
import com.example.triage.application.service.ConversationStateService;
import com.example.triage.application.service.DirectEntityRecommendationService;
import com.example.triage.application.service.IntentRouterService;
import com.example.triage.application.service.SeverityStratificationService;
import com.example.triage.application.service.SymptomClarificationService;
import com.example.triage.application.service.TriageDecisionService;
import com.example.triage.domain.hospital.DepartmentRecommendation;
import com.example.triage.domain.hospital.DoctorRecommendation;
import com.example.triage.domain.triage.ConversationStage;
import com.example.triage.domain.triage.IntentType;
import com.example.triage.domain.triage.RouteType;
import com.example.triage.domain.triage.SeverityLevel;
import com.example.triage.domain.triage.TriageAssessment;
import com.example.triage.infrastructure.persistence.entity.TriageSessionEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class TriageConversationOrchestrator {

    private final TriageDecisionService triageDecisionService;
    private final ConversationStateService conversationStateService;
    private final DirectEntityRecommendationService directEntityRecommendationService;
    private final IntentRouterService intentRouterService;
    private final SymptomClarificationService symptomClarificationService;
    private final SeverityStratificationService severityStratificationService;

    public TriageConversationOrchestrator(TriageDecisionService triageDecisionService,
                                          ConversationStateService conversationStateService,
                                          DirectEntityRecommendationService directEntityRecommendationService,
                                          IntentRouterService intentRouterService,
                                          SymptomClarificationService symptomClarificationService,
                                          SeverityStratificationService severityStratificationService) {
        this.triageDecisionService = triageDecisionService;
        this.conversationStateService = conversationStateService;
        this.directEntityRecommendationService = directEntityRecommendationService;
        this.intentRouterService = intentRouterService;
        this.symptomClarificationService = symptomClarificationService;
        this.severityStratificationService = severityStratificationService;
    }

    public TriageChatResponse chat(TriageChatRequest request) {
        TriageSessionEntity session = conversationStateService.createOrLoadSession(request);
        String sessionId = session.sessionId;
        String normalizedMessage = normalizeMessage(request);
        IntentType intentType = intentRouterService.route(normalizedMessage);

        if (intentType == IntentType.UNKNOWN) {
            return persistAndReturn(
                    sessionId,
                    normalizedMessage,
                    intentType,
                    buildUnknownResponse(session),
                    List.of(),
                    List.of("message")
            );
        }

        if (intentType != IntentType.SYMPTOM_TRIAGE_QUERY) {
            return persistAndReturn(
                    sessionId,
                    normalizedMessage,
                    intentType,
                    directEntityRecommendationService.recommend(sessionId, intentType, request, normalizedMessage),
                    conversationStateService.getStoredSymptoms(sessionId),
                    conversationStateService.getMissingSlots(sessionId)
            );
        }

        List<String> storedSymptoms = conversationStateService.getStoredSymptoms(sessionId);
        List<String> mergedSymptoms = symptomClarificationService.mergeSymptoms(storedSymptoms, normalizedMessage);
        String mergedText = String.join(" ; ", mergedSymptoms);
        SeverityStratificationService.SeverityAssessment severityAssessment = severityStratificationService.assess(
                mergedText,
                request.getAge(),
                request.getGender()
        );

        if (severityAssessment.severityLevel() == SeverityLevel.EMERGENT) {
            return persistAndReturn(
                    sessionId,
                    normalizedMessage,
                    intentType,
                    buildEmergencyResponse(sessionId, severityAssessment),
                    mergedSymptoms,
                    List.of()
            );
        }

        List<String> previousMissingSlots = conversationStateService.getMissingSlots(sessionId);
        SymptomClarificationService.ClarificationResult clarification = symptomClarificationService.evaluate(
                normalizedMessage,
                mergedSymptoms,
                previousMissingSlots,
                session.askRound,
                session.invalidAnswerCount
        );

        if (!clarification.enoughInfo() && !clarification.shouldFallback()) {
            conversationStateService.increaseAskRound(sessionId);
            if (clarification.invalidAnswer()) {
                conversationStateService.markInvalidAnswer(sessionId);
            } else {
                conversationStateService.resetInvalidAnswerCount(sessionId);
            }
            TriageSessionEntity refreshed = conversationStateService.getSession(sessionId);
            TriageChatResponse response = buildFollowUpResponse(refreshed, clarification, severityAssessment.severityLevel());
            return persistAndReturn(
                    sessionId,
                    normalizedMessage,
                    intentType,
                    response,
                    mergedSymptoms,
                    clarification.missingSlots()
            );
        }

        if (clarification.invalidAnswer()) {
            conversationStateService.markInvalidAnswer(sessionId);
        } else {
            conversationStateService.resetInvalidAnswerCount(sessionId);
        }

        TriageAssessRequest assessRequest = new TriageAssessRequest();
        assessRequest.setSymptoms(mergedText);
        assessRequest.setGender(request.getGender());
        assessRequest.setAge(request.getAge());
        assessRequest.setCity(request.getCity());
        assessRequest.setArea(request.getArea());
        assessRequest.setNearby(request.getNearby());
        assessRequest.setLatitude(request.getLatitude());
        assessRequest.setLongitude(request.getLongitude());
        assessRequest.setSpecialCondition(request.getPatientInfo());

        TriageAssessment assessment = triageDecisionService.assess(assessRequest);
        TriageSessionEntity refreshed = conversationStateService.getSession(sessionId);
        String routeType = clarification.shouldFallback()
                ? RouteType.FALLBACK.name()
                : assessment.routeType().name();
        TriageChatResponse response = buildRecommendResponse(sessionId, request, assessment, refreshed, routeType, clarification, severityAssessment);
        return persistAndReturn(
                sessionId,
                normalizedMessage,
                intentType,
                response,
                mergedSymptoms,
                clarification.shouldFallback() ? clarification.missingSlots() : List.of()
        );
    }

    private TriageChatResponse buildUnknownResponse(TriageSessionEntity session) {
        TriageChatResponse response = new TriageChatResponse();
        response.setSessionId(session.sessionId);
        response.setStage(ConversationStage.ASK_FOLLOWUP.name());
        response.setReplyText("I need a little more detail before I can continue.");
        response.setFollowUpQuestion("Please describe the symptom, body part, duration, age, and gender.");
        response.setEmergency(false);
        response.setSeverityLevel(SeverityLevel.MEDIUM.name());
        response.setRouteType(RouteType.FALLBACK.name());
        Map<String, Object> trace = new LinkedHashMap<>();
        trace.put("intent", IntentType.UNKNOWN.name());
        trace.put("askRound", session.askRound == null ? 0 : session.askRound);
        trace.put("invalidAnswerCount", session.invalidAnswerCount == null ? 0 : session.invalidAnswerCount);
        trace.put("missingSlots", List.of("message"));
        response.setDecisionTrace(trace);
        return response;
    }

    private TriageChatResponse buildEmergencyResponse(String sessionId,
                                                      SeverityStratificationService.SeverityAssessment severityAssessment) {
        TriageChatResponse response = new TriageChatResponse();
        response.setSessionId(sessionId);
        response.setStage(ConversationStage.EMERGENCY.name());
        response.setReplyText("Your description suggests a possible emergency. Please seek immediate in-person care, and call emergency services if needed.");
        response.setFollowUpQuestion(null);
        response.setEmergency(true);
        response.setSeverityLevel(SeverityLevel.EMERGENT.name());
        response.setRouteType(RouteType.AUTHORITY.name());
        RecommendationCardDto card = new RecommendationCardDto();
        card.setCardType("emergency");
        card.setTitle("Seek emergency care immediately");
        card.setSubtitle("Emergency department or emergency services");
        card.setDescription("Do not wait for further online triage if symptoms are severe, worsening, or life-threatening.");
        card.setDepartmentName("Emergency");
        card.setTag("Urgent");
        card.setScore(10.0);
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("matchedKeywords", severityAssessment.matchedKeywords());
        card.setExtra(extra);
        response.setCards(List.of(card));
        Map<String, Object> trace = new LinkedHashMap<>();
        trace.put("intent", IntentType.SYMPTOM_TRIAGE_QUERY.name());
        trace.put("emergencyMatched", true);
        trace.put("matchedKeywords", severityAssessment.matchedKeywords());
        response.setDecisionTrace(trace);
        return response;
    }

    private TriageChatResponse buildFollowUpResponse(TriageSessionEntity session,
                                                     SymptomClarificationService.ClarificationResult clarification,
                                                     SeverityLevel severityLevel) {
        TriageChatResponse response = new TriageChatResponse();
        response.setSessionId(session.sessionId);
        response.setStage(ConversationStage.ASK_FOLLOWUP.name());
        response.setReplyText("I need a bit more information before I can give a stable triage recommendation.");
        response.setFollowUpQuestion(clarification.followUpQuestion());
        response.setEmergency(false);
        response.setSeverityLevel(severityLevel.name());
        response.setRouteType(RouteType.FALLBACK.name());
        Map<String, Object> decisionTrace = new LinkedHashMap<>();
        decisionTrace.put("intent", IntentType.SYMPTOM_TRIAGE_QUERY.name());
        decisionTrace.put("missingSlots", clarification.missingSlots());
        decisionTrace.put("askRound", session.askRound == null ? 0 : session.askRound);
        decisionTrace.put("invalidAnswerCount", session.invalidAnswerCount == null ? 0 : session.invalidAnswerCount);
        decisionTrace.put("invalidAnswer", clarification.invalidAnswer());
        decisionTrace.put("severityLevel", severityLevel.name());
        response.setDecisionTrace(decisionTrace);
        return response;
    }

    private TriageChatResponse buildRecommendResponse(String sessionId,
                                                      TriageChatRequest request,
                                                      TriageAssessment assessment,
                                                      TriageSessionEntity session,
                                                      String routeType,
                                                      SymptomClarificationService.ClarificationResult clarification,
                                                      SeverityStratificationService.SeverityAssessment severityAssessment) {
        TriageChatResponse response = new TriageChatResponse();
        response.setSessionId(sessionId);
        response.setStage(ConversationStage.RECOMMEND.name());
        response.setEmergency(false);
        response.setSeverityLevel(resolveSeverityLevel(assessment, severityAssessment).name());
        response.setRouteType(routeType);
        response.setReplyText(buildReplyText(assessment, request, clarification.shouldFallback()));
        response.setFollowUpQuestion(null);
        response.setCards(buildCards(assessment, request));
        response.setDecisionTrace(buildDecisionTrace(assessment, request, session, clarification, routeType, severityAssessment));
        return response;
    }

    private TriageChatResponse persistAndReturn(String sessionId,
                                                String normalizedMessage,
                                                IntentType intentType,
                                                TriageChatResponse response,
                                                List<String> symptoms,
                                                List<String> missingSlots) {
        conversationStateService.saveMissingSlots(sessionId, symptoms, missingSlots);
        conversationStateService.updateStage(sessionId, response.getStage());
        conversationStateService.updateDecisionSummary(sessionId, response.getSeverityLevel(), response.getRouteType());
        conversationStateService.appendTurn(sessionId, normalizedMessage, normalizedMessage, intentType.name(), response);
        return response;
    }

    private String normalizeMessage(TriageChatRequest request) {
        if (StringUtils.hasText(request.getMessage())) {
            return request.getMessage().trim();
        }
        if (StringUtils.hasText(request.getPatientInfo())) {
            return request.getPatientInfo().trim();
        }
        return null;
    }

    private SeverityLevel resolveSeverityLevel(TriageAssessment assessment,
                                               SeverityStratificationService.SeverityAssessment preAssessment) {
        SeverityLevel baseLevel = preAssessment.severityLevel();
        if (assessment.candidateDiseases().isEmpty()) {
            return baseLevel;
        }
        String urgencyLevel = assessment.candidateDiseases().getFirst().urgencyLevel();
        if (!StringUtils.hasText(urgencyLevel)) {
            return baseLevel;
        }
        SeverityLevel diseaseLevel = switch (urgencyLevel.trim().toUpperCase()) {
            case "EMERGENT", "EMERGENCY", "CRITICAL" -> SeverityLevel.EMERGENT;
            case "HIGH", "URGENT" -> SeverityLevel.HIGH;
            case "LOW", "ROUTINE" -> SeverityLevel.LOW;
            default -> SeverityLevel.MEDIUM;
        };
        return diseaseLevel.ordinal() > baseLevel.ordinal() ? diseaseLevel : baseLevel;
    }

    private String buildReplyText(TriageAssessment assessment,
                                  TriageChatRequest request,
                                  boolean fallbackTriggered) {
        if (StringUtils.hasText(assessment.explanation())) {
            if (fallbackTriggered) {
                return "The information is still incomplete, so I am giving a conservative recommendation first. " + assessment.explanation();
            }
            return assessment.explanation();
        }
        if (!assessment.departmentRecommendations().isEmpty()) {
            DepartmentRecommendation item = assessment.departmentRecommendations().getFirst();
            return "Based on the current information, start with %s at %s.".formatted(item.departmentName(), item.hospitalName());
        }
        if (!assessment.doctorRecommendations().isEmpty()) {
            DoctorRecommendation item = assessment.doctorRecommendations().getFirst();
            return "Based on the current information, consider %s %s at %s.".formatted(item.title(), item.doctorName(), item.hospitalName());
        }
        return Boolean.TRUE.equals(request.getNearby())
                ? "A preliminary triage has been completed. Start with a nearby hospital that can handle the problem."
                : "A preliminary triage has been completed. Start with a hospital or department with stronger matching capability.";
    }

    private List<RecommendationCardDto> buildCards(TriageAssessment assessment, TriageChatRequest request) {
        List<RecommendationCardDto> cards = new ArrayList<>();
        assessment.departmentRecommendations().stream().limit(3).forEach(item -> {
            RecommendationCardDto card = new RecommendationCardDto();
            card.setCardType("department");
            card.setTitle("%s | %s".formatted(item.hospitalName(), item.departmentName()));
            card.setSubtitle(StringUtils.hasText(item.parentDepartmentName())
                    ? item.parentDepartmentName()
                    : "Department recommendation");
            card.setDescription("Start with this department for the current symptom description.");
            card.setHospitalName(item.hospitalName());
            card.setDepartmentName(item.departmentName());
            card.setTag(assessment.routeType() == RouteType.NEARBY ? "Nearby preferred" : "Authority preferred");
            card.setScore(item.score());
            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("supportLevel", item.supportLevel());
            extra.put("capabilityCode", item.capabilityCode());
            extra.put("departmentId", item.departmentId());
            extra.put("districtName", item.districtName());
            extra.put("authorityScore", item.authorityScore());
            card.setExtra(extra);
            cards.add(card);
        });

        if (cards.isEmpty()) {
            assessment.doctorRecommendations().stream().limit(3).forEach(item -> {
                RecommendationCardDto card = new RecommendationCardDto();
                card.setCardType("doctor");
                card.setTitle("%s | %s".formatted(item.hospitalName(), item.doctorName()));
                card.setSubtitle("%s | %s".formatted(item.departmentName(), item.title()));
                card.setDescription(item.specialtyText());
                card.setHospitalName(item.hospitalName());
                card.setDepartmentName(item.departmentName());
                card.setDoctorName(item.doctorName());
                card.setTag(assessment.routeType() == RouteType.NEARBY ? "Nearby preferred" : "Authority preferred");
                card.setScore(item.score());
                Map<String, Object> extra = new LinkedHashMap<>();
                extra.put("doctorId", item.doctorId());
                extra.put("campusName", item.campusName());
                extra.put("authorityScore", item.authorityScore());
                card.setExtra(extra);
                cards.add(card);
            });
        }
        return cards;
    }

    private Map<String, Object> buildDecisionTrace(TriageAssessment assessment,
                                                   TriageChatRequest request,
                                                   TriageSessionEntity session,
                                                   SymptomClarificationService.ClarificationResult clarification,
                                                   String routeType,
                                                   SeverityStratificationService.SeverityAssessment severityAssessment) {
        Map<String, Object> trace = new LinkedHashMap<>();
        trace.put("intent", IntentType.SYMPTOM_TRIAGE_QUERY.name());
        trace.put("commonDisease", assessment.commonDisease());
        trace.put("nearbyRequested", Boolean.TRUE.equals(request.getNearby()));
        trace.put("candidateDiseaseCount", assessment.candidateDiseases().size());
        trace.put("departmentCount", assessment.departmentRecommendations().size());
        trace.put("doctorCount", assessment.doctorRecommendations().size());
        trace.put("askRound", session.askRound == null ? 0 : session.askRound);
        trace.put("invalidAnswerCount", session.invalidAnswerCount == null ? 0 : session.invalidAnswerCount);
        trace.put("routeType", assessment.routeType().name());
        trace.put("severityLevel", severityAssessment.severityLevel().name());
        trace.put("severityMatchedKeywords", severityAssessment.matchedKeywords());
        if (clarification.shouldFallback()) {
            trace.put("fallbackTriggered", true);
            trace.put("missingSlots", clarification.missingSlots());
        }
        if (!assessment.candidateDiseases().isEmpty()) {
            trace.put("topDisease", assessment.candidateDiseases().getFirst().diseaseName());
        }
        return trace;
    }
}
