package com.example.triage.infrastructure.persistence.repository;

import com.example.triage.infrastructure.persistence.model.DiseaseCapabilityRelationRecord;
import com.example.triage.infrastructure.persistence.model.DiseaseRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DiseaseDataRepository {

    private final JdbcTemplate jdbcTemplate;

    public DiseaseDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DiseaseRecord> findApprovedDiseases() {
        String sql = """
                select disease_code, disease_name, aliases_json, symptom_keywords, gender_rule, age_min, age_max, age_group, urgency_level, review_status
                from disease_master
                where deleted = 0 and review_status = 'approved'
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapDisease(rs));
    }

    public List<DiseaseCapabilityRelationRecord> findRelationsByDiseaseCodes(List<String> diseaseCodes) {
        if (diseaseCodes == null || diseaseCodes.isEmpty()) {
            return Collections.emptyList();
        }
        String placeholders = diseaseCodes.stream().map(code -> "?").collect(Collectors.joining(","));
        String sql = """
                select disease_code, capability_code, rel_type, priority_score, crowd_constraint, note
                from disease_capability_rel
                where disease_code in (%s)
                order by priority_score desc
                """.formatted(placeholders);
        return jdbcTemplate.query(sql, diseaseCodes.toArray(), (rs, rowNum) ->
                new DiseaseCapabilityRelationRecord(
                        rs.getString("disease_code"),
                        rs.getString("capability_code"),
                        rs.getString("rel_type"),
                        rs.getDouble("priority_score"),
                        rs.getString("crowd_constraint"),
                        rs.getString("note")
                ));
    }

    private DiseaseRecord mapDisease(ResultSet rs) throws SQLException {
        return new DiseaseRecord(
                rs.getString("disease_code"),
                rs.getString("disease_name"),
                rs.getString("aliases_json"),
                rs.getString("symptom_keywords"),
                rs.getString("gender_rule"),
                rs.getObject("age_min", Integer.class),
                rs.getObject("age_max", Integer.class),
                rs.getString("age_group"),
                rs.getString("urgency_level"),
                rs.getString("review_status")
        );
    }
}
