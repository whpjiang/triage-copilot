package com.example.triage.infrastructure.persistence.repository;

import com.example.triage.infrastructure.persistence.entity.AiRecallAuditLogEntity;
import com.example.triage.infrastructure.persistence.mapper.AiRecallAuditLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AiRecallAuditRepository {

    private final AiRecallAuditLogMapper aiRecallAuditLogMapper;
    private final ObjectMapper objectMapper;

    public AiRecallAuditRepository(AiRecallAuditLogMapper aiRecallAuditLogMapper, ObjectMapper objectMapper) {
        this.aiRecallAuditLogMapper = aiRecallAuditLogMapper;
        this.objectMapper = objectMapper;
    }

    public void save(String symptoms,
                     String gender,
                     Integer age,
                     String ageGroup,
                     int eligibleDiseaseCount,
                     List<String> ruleCandidateCodes,
                     List<String> suggestedCodes,
                     String status,
                     String message) {
        AiRecallAuditLogEntity entity = new AiRecallAuditLogEntity();
        entity.symptoms = symptoms;
        entity.gender = gender;
        entity.age = age;
        entity.ageGroup = ageGroup;
        entity.eligibleDiseaseCount = eligibleDiseaseCount;
        entity.ruleCandidateCodesJson = toJson(ruleCandidateCodes);
        entity.suggestedCodesJson = toJson(suggestedCodes);
        entity.status = status;
        entity.message = message;
        aiRecallAuditLogMapper.insert(entity);
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (Exception ex) {
            return "[]";
        }
    }
}
