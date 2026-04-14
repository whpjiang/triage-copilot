package com.example.triage.application.service;

import com.example.triage.application.dto.TriageChatRequest;
import com.example.triage.application.dto.TriageChatResponse;
import com.example.triage.infrastructure.persistence.entity.TriageSessionEntity;
import com.example.triage.infrastructure.persistence.entity.TriageSlotStateEntity;
import com.example.triage.infrastructure.persistence.entity.TriageTurnEntity;
import com.example.triage.infrastructure.persistence.repository.TriageSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationStateService {

    private static final DateTimeFormatter SESSION_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final TriageSessionRepository triageSessionRepository;
    private final ObjectMapper objectMapper;

    public ConversationStateService(TriageSessionRepository triageSessionRepository,
                                    ObjectMapper objectMapper) {
        this.triageSessionRepository = triageSessionRepository;
        this.objectMapper = objectMapper;
    }

    public TriageSessionEntity createOrLoadSession(TriageChatRequest request) {
        String sessionId = resolveSessionId(request.getSessionId());
        TriageSessionEntity entity = triageSessionRepository.findSession(sessionId);
        if (entity == null) {
            entity = new TriageSessionEntity();
            entity.sessionId = sessionId;
            entity.currentStage = "INIT";
            entity.askRound = 0;
            entity.invalidAnswerCount = 0;
            entity.status = "active";
        }
        mergeRequestFields(entity, request);
        triageSessionRepository.saveSession(entity);
        return entity;
    }

    public void appendTurn(String sessionId,
                           String userMessage,
                           String normalizedQuery,
                           String intent,
                           TriageChatResponse response) {
        TriageTurnEntity entity = new TriageTurnEntity();
        entity.sessionId = sessionId;
        entity.turnNo = triageSessionRepository.nextTurnNo(sessionId);
        entity.userMessage = userMessage;
        entity.normalizedQuery = normalizedQuery;
        entity.intent = intent;
        entity.stage = response.getStage();
        entity.replyText = response.getReplyText();
        entity.rawDecisionJson = toJson(response);
        triageSessionRepository.appendTurn(entity);
    }

    public void increaseAskRound(String sessionId) {
        TriageSessionEntity entity = triageSessionRepository.findSession(sessionId);
        if (entity == null) {
            return;
        }
        entity.askRound = (entity.askRound == null ? 0 : entity.askRound) + 1;
        triageSessionRepository.saveSession(entity);
    }

    public void markInvalidAnswer(String sessionId) {
        TriageSessionEntity entity = triageSessionRepository.findSession(sessionId);
        if (entity == null) {
            return;
        }
        entity.invalidAnswerCount = (entity.invalidAnswerCount == null ? 0 : entity.invalidAnswerCount) + 1;
        triageSessionRepository.saveSession(entity);
    }

    public void resetInvalidAnswerCount(String sessionId) {
        TriageSessionEntity entity = triageSessionRepository.findSession(sessionId);
        if (entity == null) {
            return;
        }
        entity.invalidAnswerCount = 0;
        triageSessionRepository.saveSession(entity);
    }

    public void saveMissingSlots(String sessionId, List<String> symptoms, List<String> missingSlots) {
        TriageSlotStateEntity entity = triageSessionRepository.findSlotState(sessionId);
        if (entity == null) {
            entity = new TriageSlotStateEntity();
            entity.sessionId = sessionId;
        }
        entity.symptomsJson = toJson(symptoms == null ? List.of() : symptoms);
        entity.missingSlotsJson = toJson(missingSlots == null ? List.of() : missingSlots);
        triageSessionRepository.saveSlotState(entity);
    }

    public void updateStage(String sessionId, String stage) {
        TriageSessionEntity entity = triageSessionRepository.findSession(sessionId);
        if (entity == null) {
            return;
        }
        entity.currentStage = stage;
        triageSessionRepository.saveSession(entity);
    }

    public void updateDecisionSummary(String sessionId, String severityLevel, String routeType) {
        TriageSessionEntity entity = triageSessionRepository.findSession(sessionId);
        if (entity == null) {
            return;
        }
        entity.severityLevel = severityLevel;
        entity.routeType = routeType;
        triageSessionRepository.saveSession(entity);
    }

    public TriageSessionEntity getSession(String sessionId) {
        return triageSessionRepository.findSession(sessionId);
    }

    public List<String> getStoredSymptoms(String sessionId) {
        TriageSlotStateEntity entity = triageSessionRepository.findSlotState(sessionId);
        return entity == null ? List.of() : parseJsonList(entity.symptomsJson);
    }

    public List<String> getMissingSlots(String sessionId) {
        TriageSlotStateEntity entity = triageSessionRepository.findSlotState(sessionId);
        return entity == null ? List.of() : parseJsonList(entity.missingSlotsJson);
    }

    private String resolveSessionId(String sessionId) {
        if (StringUtils.hasText(sessionId)) {
            return sessionId.trim();
        }
        return "sess_" + LocalDateTime.now().format(SESSION_TIME_FORMATTER) + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void mergeRequestFields(TriageSessionEntity entity, TriageChatRequest request) {
        if (StringUtils.hasText(request.getUserId())) {
            entity.userId = request.getUserId().trim();
        }
        if (StringUtils.hasText(request.getDialogId())) {
            entity.dialogId = request.getDialogId().trim();
        }
        if (StringUtils.hasText(request.getCity())) {
            entity.city = request.getCity().trim();
        }
        if (StringUtils.hasText(request.getArea())) {
            entity.area = request.getArea().trim();
        }
        if (request.getNearby() != null) {
            entity.nearby = Boolean.TRUE.equals(request.getNearby()) ? 1 : 0;
        }
        if (request.getLatitude() != null) {
            entity.latitude = BigDecimal.valueOf(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            entity.longitude = BigDecimal.valueOf(request.getLongitude());
        }
        if (request.getAge() != null) {
            entity.patientAge = request.getAge();
        }
        if (StringUtils.hasText(request.getGender())) {
            entity.patientGender = request.getGender().trim();
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private List<String> parseJsonList(String json) {
        try {
            if (!StringUtils.hasText(json)) {
                return List.of();
            }
            return objectMapper.readerForListOf(String.class).readValue(json);
        } catch (Exception ex) {
            return List.of();
        }
    }
}
