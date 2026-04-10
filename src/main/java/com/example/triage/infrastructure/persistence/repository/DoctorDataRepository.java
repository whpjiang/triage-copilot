package com.example.triage.infrastructure.persistence.repository;

import com.example.triage.infrastructure.persistence.model.DoctorRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DoctorDataRepository {

    private final JdbcTemplate jdbcTemplate;

    public DoctorDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DoctorRecord> findDoctorsByDepartmentIds(List<Long> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return Collections.emptyList();
        }
        String placeholders = departmentIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = """
                select d.id as doctor_id,
                       d.hospital_id,
                       d.department_id,
                       d.doctor_name,
                       d.title,
                       d.specialty_text,
                       d.gender_rule,
                       d.age_min,
                       d.age_max,
                       d.crowd_tags_json,
                       rel.capability_code,
                       rel.weight,
                       h.hospital_name,
                       hd.department_name
                from doctor_profile d
                left join doctor_capability_rel rel on rel.doctor_id = d.id
                join hospital h on h.id = d.hospital_id and h.deleted = 0 and h.active_status = 1
                join hospital_department hd on hd.id = d.department_id and hd.deleted = 0 and hd.active_status = 1
                where d.active_status = 1 and d.department_id in (%s)
                """.formatted(placeholders);
        return jdbcTemplate.query(sql, departmentIds.toArray(), (rs, rowNum) ->
                new DoctorRecord(
                        rs.getLong("doctor_id"),
                        rs.getLong("hospital_id"),
                        rs.getLong("department_id"),
                        rs.getString("doctor_name"),
                        rs.getString("title"),
                        rs.getString("specialty_text"),
                        rs.getString("gender_rule"),
                        rs.getObject("age_min", Integer.class),
                        rs.getObject("age_max", Integer.class),
                        rs.getString("crowd_tags_json"),
                        rs.getString("capability_code"),
                        rs.getDouble("weight"),
                        rs.getString("hospital_name"),
                        rs.getString("department_name")
                ));
    }
}
