package com.example.triage.infrastructure.persistence.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AiRecallAuditRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AiRecallAuditRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
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
        jdbcTemplate.update("""
                        insert into ai_recall_audit_log(
                            symptoms, gender, age, age_group, eligible_disease_count,
                            rule_candidate_codes_json, suggested_codes_json, status, message
                        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                symptoms,
                gender,
                age,
                ageGroup,
                eligibleDiseaseCount,
                toJson(ruleCandidateCodes),
                toJson(suggestedCodes),
                status,
                message
        );
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (Exception ex) {
            return "[]";
        }
    }
}
