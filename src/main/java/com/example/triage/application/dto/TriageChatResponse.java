package com.example.triage.application.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TriageChatResponse {

    private String sessionId;
    private String stage;
    private String replyText;
    private String followUpQuestion;
    private Boolean emergency;
    private String severityLevel;
    private String routeType;
    private List<RecommendationCardDto> cards = new ArrayList<>();
    private Map<String, Object> decisionTrace = new LinkedHashMap<>();

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getReplyText() {
        return replyText;
    }

    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }

    public String getFollowUpQuestion() {
        return followUpQuestion;
    }

    public void setFollowUpQuestion(String followUpQuestion) {
        this.followUpQuestion = followUpQuestion;
    }

    public Boolean getEmergency() {
        return emergency;
    }

    public void setEmergency(Boolean emergency) {
        this.emergency = emergency;
    }

    public String getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(String severityLevel) {
        this.severityLevel = severityLevel;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public List<RecommendationCardDto> getCards() {
        return cards;
    }

    public void setCards(List<RecommendationCardDto> cards) {
        this.cards = cards;
    }

    public Map<String, Object> getDecisionTrace() {
        return decisionTrace;
    }

    public void setDecisionTrace(Map<String, Object> decisionTrace) {
        this.decisionTrace = decisionTrace;
    }
}
