package com.example.triage.infrastructure.persistence.repository;

import com.example.triage.infrastructure.persistence.model.DepartmentMappingRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class HospitalDataRepository {

    private final JdbcTemplate jdbcTemplate;

    public HospitalDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DepartmentMappingRecord> findDepartmentMappings(List<String> capabilityCodes, String city) {
        if (capabilityCodes == null || capabilityCodes.isEmpty()) {
            return Collections.emptyList();
        }
        String placeholders = capabilityCodes.stream().map(code -> "?").collect(Collectors.joining(","));
        String sql = """
                select hd.id as department_id,
                       h.id as hospital_id,
                       h.hospital_name,
                       hd.department_name,
                       hd.parent_department_name,
                       hd.department_intro,
                       hd.service_scope,
                       hd.gender_rule,
                       hd.age_min,
                       hd.age_max,
                       hd.crowd_tags_json,
                       rel.capability_code,
                       rel.support_level,
                       rel.weight,
                       rel.source
                from department_capability_rel rel
                join hospital_department hd on hd.id = rel.department_id and hd.deleted = 0 and hd.active_status = 1
                join hospital h on h.id = hd.hospital_id and h.deleted = 0 and h.active_status = 1
                where rel.capability_code in (%s)
                """.formatted(placeholders);
        Object[] args;
        if (StringUtils.hasText(city)) {
            sql += " and h.city = ? ";
            args = new Object[capabilityCodes.size() + 1];
            for (int i = 0; i < capabilityCodes.size(); i++) {
                args[i] = capabilityCodes.get(i);
            }
            args[capabilityCodes.size()] = city;
        } else {
            args = capabilityCodes.toArray();
        }
        sql += " order by rel.weight desc, hd.id asc";
        return jdbcTemplate.query(sql, args, (rs, rowNum) ->
                new DepartmentMappingRecord(
                        rs.getLong("department_id"),
                        rs.getLong("hospital_id"),
                        rs.getString("hospital_name"),
                        rs.getString("department_name"),
                        rs.getString("parent_department_name"),
                        rs.getString("department_intro"),
                        rs.getString("service_scope"),
                        rs.getString("gender_rule"),
                        rs.getObject("age_min", Integer.class),
                        rs.getObject("age_max", Integer.class),
                        rs.getString("crowd_tags_json"),
                        rs.getString("capability_code"),
                        rs.getString("support_level"),
                        rs.getDouble("weight"),
                        rs.getString("source")
                ));
    }

    public int countHospitals() {
        Integer count = jdbcTemplate.queryForObject("select count(1) from hospital where deleted = 0 and active_status = 1", Integer.class);
        return count == null ? 0 : count;
    }

    public int countDepartments() {
        Integer count = jdbcTemplate.queryForObject("select count(1) from hospital_department where deleted = 0 and active_status = 1", Integer.class);
        return count == null ? 0 : count;
    }

    public int countDepartmentCapabilityRelations() {
        Integer count = jdbcTemplate.queryForObject("select count(1) from department_capability_rel", Integer.class);
        return count == null ? 0 : count;
    }
}
