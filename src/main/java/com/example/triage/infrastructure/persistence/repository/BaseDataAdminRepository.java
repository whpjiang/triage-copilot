package com.example.triage.infrastructure.persistence.repository;

import com.example.triage.infrastructure.persistence.model.ImportFailureLogRecord;
import com.example.triage.infrastructure.persistence.model.ImportJobRecord;
import com.example.triage.infrastructure.persistence.model.ImportReviewItemRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BaseDataAdminRepository {

    private final JdbcTemplate jdbcTemplate;

    public BaseDataAdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long createImportJob(String datasetType, String fileName) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "insert into import_job_record(dataset_type, file_name, status, success_count, failure_count, review_count, auto_mapped_count) values (?, ?, ?, 0, 0, 0, 0)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, datasetType);
            ps.setString(2, fileName);
            ps.setString(3, "PROCESSING");
            return ps;
        }, keyHolder);
        return extractGeneratedId(keyHolder);
    }

    public void finishImportJob(long jobId,
                                int successCount,
                                int failureCount,
                                int reviewCount,
                                int autoMappedCount,
                                String status,
                                String message) {
        jdbcTemplate.update(
                "update import_job_record set success_count = ?, failure_count = ?, review_count = ?, auto_mapped_count = ?, status = ?, message = ?, update_time = current_timestamp where id = ?",
                successCount, failureCount, reviewCount, autoMappedCount, status, message, jobId
        );
    }

    public void addFailure(long jobId, int rowNumber, String rawContent, String errorMessage) {
        jdbcTemplate.update(
                "insert into import_failure_log(job_id, row_number, raw_content, error_message) values (?, ?, ?, ?)",
                jobId, rowNumber, rawContent, errorMessage
        );
    }

    public void addReviewItem(long jobId, String datasetType, String itemKey, String issueType, String rawContent, String suggestion) {
        jdbcTemplate.update(
                "insert into import_review_item(job_id, dataset_type, item_key, issue_type, raw_content, suggestion, resolved) values (?, ?, ?, ?, ?, ?, 0)",
                jobId, datasetType, itemKey, issueType, rawContent, suggestion
        );
    }

    public void upsertDisease(String diseaseCode, String diseaseName, String aliasesJson, String symptomKeywords, String genderRule,
                              Integer ageMin, Integer ageMax, String ageGroup, String urgencyLevel, String reviewStatus) {
        Integer exists = jdbcTemplate.queryForObject("select count(1) from disease_master where disease_code = ?", Integer.class, diseaseCode);
        if (exists != null && exists > 0) {
            jdbcTemplate.update("""
                    update disease_master
                    set disease_name = ?, aliases_json = ?, symptom_keywords = ?, gender_rule = ?, age_min = ?, age_max = ?, age_group = ?, urgency_level = ?, review_status = ?, update_time = current_timestamp
                    where disease_code = ?
                    """, diseaseName, aliasesJson, symptomKeywords, genderRule, ageMin, ageMax, ageGroup, urgencyLevel, reviewStatus, diseaseCode);
        } else {
            jdbcTemplate.update("""
                    insert into disease_master(disease_code, disease_name, aliases_json, symptom_keywords, gender_rule, age_min, age_max, age_group, urgency_level, review_status, deleted)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                    """, diseaseCode, diseaseName, aliasesJson, symptomKeywords, genderRule, ageMin, ageMax, ageGroup, urgencyLevel, reviewStatus);
        }
    }

    public void replaceDiseaseAliases(String diseaseCode, List<String> aliases) {
        jdbcTemplate.update("delete from disease_alias where disease_code = ?", diseaseCode);
        for (String alias : aliases) {
            jdbcTemplate.update(
                    "insert into disease_alias(disease_code, alias_name, alias_type, source) values (?, ?, ?, ?)",
                    diseaseCode, alias, "imported", "base-data-import"
            );
        }
    }

    public void upsertDiseaseCapabilityRelation(String diseaseCode,
                                                String capabilityCode,
                                                String relType,
                                                double priorityScore,
                                                String note) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(1) from disease_capability_rel where disease_code = ? and capability_code = ?",
                Integer.class,
                diseaseCode,
                capabilityCode
        );
        if (count != null && count > 0) {
            jdbcTemplate.update(
                    "update disease_capability_rel set rel_type = ?, priority_score = ?, note = ? where disease_code = ? and capability_code = ?",
                    relType, priorityScore, note, diseaseCode, capabilityCode
            );
            return;
        }
        jdbcTemplate.update(
                "insert into disease_capability_rel(disease_code, capability_code, rel_type, priority_score, note) values (?, ?, ?, ?, ?)",
                diseaseCode, capabilityCode, relType, priorityScore, note
        );
    }

    public long upsertHospital(String hospitalCode, String hospitalName, String city) {
        List<Long> ids = jdbcTemplate.query("select id from hospital where hospital_code = ?", (rs, rowNum) -> rs.getLong("id"), hospitalCode);
        if (!ids.isEmpty()) {
            Long id = ids.get(0);
            jdbcTemplate.update("update hospital set hospital_name = ?, city = ?, update_time = current_timestamp where id = ?", hospitalName, city, id);
            return id;
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "insert into hospital(hospital_code, hospital_name, city, active_status, deleted) values (?, ?, ?, 1, 0)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, hospitalCode);
            ps.setString(2, hospitalName);
            ps.setString(3, city);
            return ps;
        }, keyHolder);
        return extractGeneratedId(keyHolder);
    }

    public long upsertDepartment(long hospitalId, String departmentName, String parentDepartmentName, String intro,
                                 String scope, String genderRule, Integer ageMin, Integer ageMax, String crowdTagsJson) {
        List<Long> ids = jdbcTemplate.query(
                "select id from hospital_department where hospital_id = ? and department_name = ? and deleted = 0",
                (rs, rowNum) -> rs.getLong("id"),
                hospitalId, departmentName
        );
        if (!ids.isEmpty()) {
            Long id = ids.get(0);
            jdbcTemplate.update("""
                    update hospital_department
                    set parent_department_name = ?, department_intro = ?, service_scope = ?, gender_rule = ?, age_min = ?, age_max = ?, crowd_tags_json = ?, update_time = current_timestamp
                    where id = ?
                    """, parentDepartmentName, intro, scope, genderRule, ageMin, ageMax, crowdTagsJson, id);
            return id;
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "insert into hospital_department(hospital_id, department_name, parent_department_name, department_intro, service_scope, active_status, deleted, gender_rule, age_min, age_max, crowd_tags_json) values (?, ?, ?, ?, ?, 1, 0, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, hospitalId);
            ps.setString(2, departmentName);
            ps.setString(3, parentDepartmentName);
            ps.setString(4, intro);
            ps.setString(5, scope);
            ps.setString(6, genderRule);
            ps.setObject(7, ageMin);
            ps.setObject(8, ageMax);
            ps.setString(9, crowdTagsJson);
            return ps;
        }, keyHolder);
        return extractGeneratedId(keyHolder);
    }

    public boolean capabilityExists(String capabilityCode) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(1) from medical_capability_catalog where capability_code = ? and active_status = 1",
                Integer.class,
                capabilityCode
        );
        return count != null && count > 0;
    }

    public void upsertDepartmentCapabilityRelation(long departmentId,
                                                   String capabilityCode,
                                                   String supportLevel,
                                                   double weight,
                                                   String source) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(1) from department_capability_rel where department_id = ? and capability_code = ?",
                Integer.class,
                departmentId,
                capabilityCode
        );
        if (count != null && count > 0) {
            jdbcTemplate.update(
                    "update department_capability_rel set support_level = ?, weight = ?, source = ? where department_id = ? and capability_code = ?",
                    supportLevel, weight, source, departmentId, capabilityCode
            );
            return;
        }
        jdbcTemplate.update(
                "insert into department_capability_rel(department_id, capability_code, support_level, weight, source) values (?, ?, ?, ?, ?)",
                departmentId, capabilityCode, supportLevel, weight, source
        );
    }

    public Map<String, Integer> aggregateCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("diseases", queryCount("select count(1) from disease_master where deleted = 0"));
        counts.put("disease_aliases", queryCount("select count(1) from disease_alias"));
        counts.put("diseases_with_symptoms", queryCount("select count(1) from disease_master where deleted = 0 and symptom_keywords is not null and symptom_keywords <> '[]' and symptom_keywords <> ''"));
        counts.put("disease_capability_mapped", queryCount("select count(distinct disease_code) from disease_capability_rel"));
        counts.put("pending", queryCount("select count(1) from import_review_item where resolved = 0"));
        counts.put("pending_department", queryCount("select count(1) from import_review_item where resolved = 0 and dataset_type = 'department'"));
        counts.put("pending_disease", queryCount("select count(1) from import_review_item where resolved = 0 and dataset_type = 'disease'"));
        counts.put("auto_mapped_departments", queryCount("select count(distinct department_id) from department_capability_rel where source like '%auto%'"));
        return counts;
    }

    public int countPendingReviews(String datasetType, Long jobId) {
        StringBuilder sql = new StringBuilder("select count(1) from import_review_item where resolved = 0");
        List<Object> args = new ArrayList<>();
        appendReviewFilters(sql, args, datasetType, jobId);
        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        return count == null ? 0 : count;
    }

    public Map<String, Integer> countReviewTypes(Long jobId) {
        return countReviewTypes(jobId, null);
    }

    public Map<String, Integer> countReviewTypes(Long jobId, String datasetType) {
        StringBuilder sql = new StringBuilder("""
                select issue_type, count(1) as total
                from import_review_item
                where resolved = 0
                """);
        List<Object> args = new ArrayList<>();
        if (jobId != null) {
            sql.append(" and job_id = ?");
            args.add(jobId);
        }
        if (datasetType != null && !datasetType.isBlank()) {
            sql.append(" and dataset_type = ?");
            args.add(datasetType);
        }
        sql.append(" group by issue_type order by total desc, issue_type asc");
        return jdbcTemplate.query(sql.toString(), rs -> {
            Map<String, Integer> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getString("issue_type"), rs.getInt("total"));
            }
            return result;
        }, args.toArray());
    }

    public Map<String, Integer> countFailureTypes(Long jobId) {
        return jdbcTemplate.query("""
                select error_message, count(1) as total
                from import_failure_log
                where job_id = ?
                group by error_message
                order by total desc, error_message asc
                """, rs -> {
            Map<String, Integer> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getString("error_message"), rs.getInt("total"));
            }
            return result;
        }, jobId);
    }

    public List<ImportReviewItemRecord> findPendingReviews(String datasetType, Long jobId, int limit) {
        StringBuilder sql = new StringBuilder("""
                select id, job_id, dataset_type, item_key, issue_type, raw_content, suggestion, resolved
                from import_review_item
                where resolved = 0
                """);
        List<Object> args = new ArrayList<>();
        appendReviewFilters(sql, args, datasetType, jobId);
        sql.append(" order by id desc limit ?");
        args.add(limit);
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> new ImportReviewItemRecord(
                rs.getLong("id"),
                rs.getLong("job_id"),
                rs.getString("dataset_type"),
                rs.getString("item_key"),
                rs.getString("issue_type"),
                rs.getString("raw_content"),
                rs.getString("suggestion"),
                rs.getBoolean("resolved")
        ), args.toArray());
    }

    public int resolveReviewItem(Long reviewId, String resolutionNote) {
        return jdbcTemplate.update(
                "update import_review_item set resolved = 1, resolution_note = ?, update_time = current_timestamp where id = ? and resolved = 0",
                resolutionNote, reviewId
        );
    }

    public List<ImportJobRecord> findRecentJobs(int limit) {
        return jdbcTemplate.query("""
                select id, dataset_type, file_name, status, success_count, failure_count, review_count, auto_mapped_count, message
                from import_job_record
                order by id desc
                limit ?
                """, (rs, rowNum) -> new ImportJobRecord(
                rs.getLong("id"),
                rs.getString("dataset_type"),
                rs.getString("file_name"),
                rs.getString("status"),
                rs.getInt("success_count"),
                rs.getInt("failure_count"),
                rs.getInt("review_count"),
                rs.getInt("auto_mapped_count"),
                rs.getString("message")
        ), limit);
    }

    public ImportJobRecord findJobById(Long jobId) {
        List<ImportJobRecord> jobs = jdbcTemplate.query("""
                select id, dataset_type, file_name, status, success_count, failure_count, review_count, auto_mapped_count, message
                from import_job_record
                where id = ?
                """, (rs, rowNum) -> new ImportJobRecord(
                rs.getLong("id"),
                rs.getString("dataset_type"),
                rs.getString("file_name"),
                rs.getString("status"),
                rs.getInt("success_count"),
                rs.getInt("failure_count"),
                rs.getInt("review_count"),
                rs.getInt("auto_mapped_count"),
                rs.getString("message")
        ), jobId);
        return jobs.isEmpty() ? null : jobs.getFirst();
    }

    public List<ImportFailureLogRecord> findFailuresByJobId(Long jobId, int limit) {
        return jdbcTemplate.query("""
                select id, job_id, row_number, raw_content, error_message
                from import_failure_log
                where job_id = ?
                order by id asc
                limit ?
                """, (rs, rowNum) -> new ImportFailureLogRecord(
                rs.getLong("id"),
                rs.getLong("job_id"),
                rs.getInt("row_number"),
                rs.getString("raw_content"),
                rs.getString("error_message")
        ), jobId, limit);
    }

    public List<ImportReviewItemRecord> findRecentReviewsByJobId(Long jobId, int limit) {
        return jdbcTemplate.query("""
                select id, job_id, dataset_type, item_key, issue_type, raw_content, suggestion, resolved
                from import_review_item
                where job_id = ?
                order by id desc
                limit ?
                """, (rs, rowNum) -> new ImportReviewItemRecord(
                rs.getLong("id"),
                rs.getLong("job_id"),
                rs.getString("dataset_type"),
                rs.getString("item_key"),
                rs.getString("issue_type"),
                rs.getString("raw_content"),
                rs.getString("suggestion"),
                rs.getBoolean("resolved")
        ), jobId, limit);
    }

    private void appendReviewFilters(StringBuilder sql, List<Object> args, String datasetType, Long jobId) {
        if (datasetType != null && !datasetType.isBlank()) {
            sql.append(" and dataset_type = ?");
            args.add(datasetType);
        }
        if (jobId != null) {
            sql.append(" and job_id = ?");
            args.add(jobId);
        }
    }

    private long extractGeneratedId(KeyHolder keyHolder) {
        if (keyHolder.getKeyList().isEmpty()) {
            return 0L;
        }
        Object id = keyHolder.getKeyList().get(0).get("id");
        if (id instanceof Number number) {
            return number.longValue();
        }
        Number fallback = keyHolder.getKey();
        return fallback == null ? 0L : fallback.longValue();
    }

    private int queryCount(String sql) {
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count == null ? 0 : count;
    }
}
