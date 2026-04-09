package com.example.triage.infrastructure.persistence.repository;

import com.example.triage.infrastructure.persistence.model.CapabilityRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CapabilityDataRepository {

    private final JdbcTemplate jdbcTemplate;

    public CapabilityDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CapabilityRecord> findByCapabilityCodes(List<String> capabilityCodes) {
        if (capabilityCodes == null || capabilityCodes.isEmpty()) {
            return Collections.emptyList();
        }
        String placeholders = capabilityCodes.stream().map(code -> "?").collect(Collectors.joining(","));
        String sql = """
                select capability_code, capability_name, capability_type, parent_code, standard_dept_code,
                       aliases_json, gender_rule, age_min, age_max, crowd_tags_json, pathway_tags_json, active_status
                from medical_capability_catalog
                where active_status = 1 and capability_code in (%s)
                """.formatted(placeholders);
        return jdbcTemplate.query(sql, capabilityCodes.toArray(), (rs, rowNum) ->
                new CapabilityRecord(
                        rs.getString("capability_code"),
                        rs.getString("capability_name"),
                        rs.getString("capability_type"),
                        rs.getString("parent_code"),
                        rs.getString("standard_dept_code"),
                        rs.getString("aliases_json"),
                        rs.getString("gender_rule"),
                        rs.getObject("age_min", Integer.class),
                        rs.getObject("age_max", Integer.class),
                        rs.getString("crowd_tags_json"),
                        rs.getString("pathway_tags_json"),
                        rs.getObject("active_status", Integer.class)
                ));
    }

    public int countCapabilities() {
        Integer count = jdbcTemplate.queryForObject("select count(1) from medical_capability_catalog where active_status = 1", Integer.class);
        return count == null ? 0 : count;
    }
}
