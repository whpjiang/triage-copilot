package com.example.triage.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.triage.infrastructure.persistence.entity.DepartmentCapabilityRelEntity;
import com.example.triage.infrastructure.persistence.entity.DiseaseAliasEntity;
import com.example.triage.infrastructure.persistence.entity.DiseaseCapabilityRelEntity;
import com.example.triage.infrastructure.persistence.entity.DiseaseMasterEntity;
import com.example.triage.infrastructure.persistence.entity.HospitalDepartmentEntity;
import com.example.triage.infrastructure.persistence.entity.HospitalEntity;
import com.example.triage.infrastructure.persistence.entity.ImportFailureLogEntity;
import com.example.triage.infrastructure.persistence.entity.ImportJobRecordEntity;
import com.example.triage.infrastructure.persistence.entity.ImportReviewItemEntity;
import com.example.triage.infrastructure.persistence.entity.MedicalCapabilityCatalogEntity;
import com.example.triage.infrastructure.persistence.mapper.DepartmentCapabilityRelMapper;
import com.example.triage.infrastructure.persistence.mapper.DiseaseAliasMapper;
import com.example.triage.infrastructure.persistence.mapper.DiseaseCapabilityRelMapper;
import com.example.triage.infrastructure.persistence.mapper.DiseaseMasterMapper;
import com.example.triage.infrastructure.persistence.mapper.HospitalDepartmentMapper;
import com.example.triage.infrastructure.persistence.mapper.HospitalMapper;
import com.example.triage.infrastructure.persistence.mapper.ImportFailureLogMapper;
import com.example.triage.infrastructure.persistence.mapper.ImportJobRecordMapper;
import com.example.triage.infrastructure.persistence.mapper.ImportReviewItemMapper;
import com.example.triage.infrastructure.persistence.mapper.MedicalCapabilityCatalogMapper;
import com.example.triage.infrastructure.persistence.model.ImportFailureLogRecord;
import com.example.triage.infrastructure.persistence.model.ImportJobRecord;
import com.example.triage.infrastructure.persistence.model.ImportReviewItemRecord;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BaseDataAdminRepository {

    private final ImportJobRecordMapper importJobRecordMapper;
    private final ImportFailureLogMapper importFailureLogMapper;
    private final ImportReviewItemMapper importReviewItemMapper;
    private final DiseaseMasterMapper diseaseMasterMapper;
    private final DiseaseAliasMapper diseaseAliasMapper;
    private final DiseaseCapabilityRelMapper diseaseCapabilityRelMapper;
    private final HospitalMapper hospitalMapper;
    private final HospitalDepartmentMapper hospitalDepartmentMapper;
    private final MedicalCapabilityCatalogMapper medicalCapabilityCatalogMapper;
    private final DepartmentCapabilityRelMapper departmentCapabilityRelMapper;

    public BaseDataAdminRepository(ImportJobRecordMapper importJobRecordMapper,
                                   ImportFailureLogMapper importFailureLogMapper,
                                   ImportReviewItemMapper importReviewItemMapper,
                                   DiseaseMasterMapper diseaseMasterMapper,
                                   DiseaseAliasMapper diseaseAliasMapper,
                                   DiseaseCapabilityRelMapper diseaseCapabilityRelMapper,
                                   HospitalMapper hospitalMapper,
                                   HospitalDepartmentMapper hospitalDepartmentMapper,
                                   MedicalCapabilityCatalogMapper medicalCapabilityCatalogMapper,
                                   DepartmentCapabilityRelMapper departmentCapabilityRelMapper) {
        this.importJobRecordMapper = importJobRecordMapper;
        this.importFailureLogMapper = importFailureLogMapper;
        this.importReviewItemMapper = importReviewItemMapper;
        this.diseaseMasterMapper = diseaseMasterMapper;
        this.diseaseAliasMapper = diseaseAliasMapper;
        this.diseaseCapabilityRelMapper = diseaseCapabilityRelMapper;
        this.hospitalMapper = hospitalMapper;
        this.hospitalDepartmentMapper = hospitalDepartmentMapper;
        this.medicalCapabilityCatalogMapper = medicalCapabilityCatalogMapper;
        this.departmentCapabilityRelMapper = departmentCapabilityRelMapper;
    }

    public long createImportJob(String datasetType, String fileName) {
        ImportJobRecordEntity entity = new ImportJobRecordEntity();
        entity.datasetType = datasetType;
        entity.fileName = fileName;
        entity.status = "PROCESSING";
        entity.successCount = 0;
        entity.failureCount = 0;
        entity.reviewCount = 0;
        entity.autoMappedCount = 0;
        importJobRecordMapper.insert(entity);
        return entity.id == null ? 0L : entity.id;
    }

    public void finishImportJob(long jobId,
                                int successCount,
                                int failureCount,
                                int reviewCount,
                                int autoMappedCount,
                                String status,
                                String message) {
        importJobRecordMapper.update(null, new UpdateWrapper<ImportJobRecordEntity>()
                .eq("id", jobId)
                .set("success_count", successCount)
                .set("failure_count", failureCount)
                .set("review_count", reviewCount)
                .set("auto_mapped_count", autoMappedCount)
                .set("status", status)
                .set("message", message)
                .setSql("update_time = current_timestamp"));
    }

    public void addFailure(long jobId, int rowNumber, String rawContent, String errorMessage) {
        ImportFailureLogEntity entity = new ImportFailureLogEntity();
        entity.jobId = jobId;
        entity.rowNumber = rowNumber;
        entity.rawContent = rawContent;
        entity.errorMessage = errorMessage;
        importFailureLogMapper.insert(entity);
    }

    public void addReviewItem(long jobId, String datasetType, String itemKey, String issueType, String rawContent, String suggestion) {
        ImportReviewItemEntity entity = new ImportReviewItemEntity();
        entity.jobId = jobId;
        entity.datasetType = datasetType;
        entity.itemKey = itemKey;
        entity.issueType = issueType;
        entity.rawContent = rawContent;
        entity.suggestion = suggestion;
        entity.resolved = 0;
        importReviewItemMapper.insert(entity);
    }

    public void upsertDisease(String diseaseCode, String diseaseName, String aliasesJson, String symptomKeywords, String genderRule,
                              Integer ageMin, Integer ageMax, String ageGroup, String urgencyLevel, String reviewStatus) {
        DiseaseMasterEntity entity = diseaseMasterMapper.selectOne(new QueryWrapper<DiseaseMasterEntity>()
                .eq("disease_code", diseaseCode)
                .last("limit 1"));
        if (entity == null) {
            entity = new DiseaseMasterEntity();
            entity.diseaseCode = diseaseCode;
            entity.deleted = 0;
            entity.diseaseName = diseaseName;
            entity.aliasesJson = aliasesJson;
            entity.symptomKeywords = symptomKeywords;
            entity.genderRule = genderRule;
            entity.ageMin = ageMin;
            entity.ageMax = ageMax;
            entity.ageGroup = ageGroup;
            entity.urgencyLevel = urgencyLevel;
            entity.reviewStatus = reviewStatus;
            diseaseMasterMapper.insert(entity);
            return;
        }
        diseaseMasterMapper.update(null, new UpdateWrapper<DiseaseMasterEntity>()
                .eq("id", entity.id)
                .set("disease_name", diseaseName)
                .set("aliases_json", aliasesJson)
                .set("symptom_keywords", symptomKeywords)
                .set("gender_rule", genderRule)
                .set("age_min", ageMin)
                .set("age_max", ageMax)
                .set("age_group", ageGroup)
                .set("urgency_level", urgencyLevel)
                .set("review_status", reviewStatus)
                .setSql("update_time = current_timestamp"));
    }

    public void replaceDiseaseAliases(String diseaseCode, List<String> aliases) {
        diseaseAliasMapper.delete(new QueryWrapper<DiseaseAliasEntity>().eq("disease_code", diseaseCode));
        for (String alias : aliases) {
            DiseaseAliasEntity entity = new DiseaseAliasEntity();
            entity.diseaseCode = diseaseCode;
            entity.aliasName = alias;
            entity.aliasType = "imported";
            entity.source = "base-data-import";
            diseaseAliasMapper.insert(entity);
        }
    }

    public void upsertDiseaseCapabilityRelation(String diseaseCode,
                                                String capabilityCode,
                                                String relType,
                                                double priorityScore,
                                                String note) {
        DiseaseCapabilityRelEntity entity = diseaseCapabilityRelMapper.selectOne(new QueryWrapper<DiseaseCapabilityRelEntity>()
                .eq("disease_code", diseaseCode)
                .eq("capability_code", capabilityCode)
                .last("limit 1"));
        if (entity == null) {
            entity = new DiseaseCapabilityRelEntity();
            entity.diseaseCode = diseaseCode;
            entity.capabilityCode = capabilityCode;
            entity.relType = relType;
            entity.priorityScore = BigDecimal.valueOf(priorityScore);
            entity.note = note;
            diseaseCapabilityRelMapper.insert(entity);
            return;
        }
        diseaseCapabilityRelMapper.update(null, new UpdateWrapper<DiseaseCapabilityRelEntity>()
                .eq("id", entity.id)
                .set("rel_type", relType)
                .set("priority_score", BigDecimal.valueOf(priorityScore))
                .set("note", note));
    }

    public long upsertHospital(String hospitalCode, String hospitalName, String city) {
        HospitalEntity entity = hospitalMapper.selectOne(new QueryWrapper<HospitalEntity>()
                .eq("hospital_code", hospitalCode)
                .last("limit 1"));
        if (entity == null) {
            entity = new HospitalEntity();
            entity.hospitalCode = hospitalCode;
            entity.hospitalName = hospitalName;
            entity.city = city;
            entity.activeStatus = 1;
            entity.deleted = 0;
            hospitalMapper.insert(entity);
            return entity.id == null ? 0L : entity.id;
        }
        hospitalMapper.update(null, new UpdateWrapper<HospitalEntity>()
                .eq("id", entity.id)
                .set("hospital_name", hospitalName)
                .set("city", city)
                .setSql("update_time = current_timestamp"));
        return entity.id == null ? 0L : entity.id;
    }

    public long upsertDepartment(long hospitalId, String departmentName, String parentDepartmentName, String intro,
                                 String scope, String genderRule, Integer ageMin, Integer ageMax, String crowdTagsJson) {
        HospitalDepartmentEntity entity = hospitalDepartmentMapper.selectOne(new QueryWrapper<HospitalDepartmentEntity>()
                .eq("hospital_id", hospitalId)
                .eq("department_name", departmentName)
                .eq("deleted", 0)
                .last("limit 1"));
        if (entity == null) {
            entity = new HospitalDepartmentEntity();
            entity.hospitalId = hospitalId;
            entity.departmentName = departmentName;
            entity.parentDepartmentName = parentDepartmentName;
            entity.departmentIntro = intro;
            entity.serviceScope = scope;
            entity.activeStatus = 1;
            entity.deleted = 0;
            entity.genderRule = genderRule;
            entity.ageMin = ageMin;
            entity.ageMax = ageMax;
            entity.crowdTagsJson = crowdTagsJson;
            hospitalDepartmentMapper.insert(entity);
            return entity.id == null ? 0L : entity.id;
        }
        hospitalDepartmentMapper.update(null, new UpdateWrapper<HospitalDepartmentEntity>()
                .eq("id", entity.id)
                .set("parent_department_name", parentDepartmentName)
                .set("department_intro", intro)
                .set("service_scope", scope)
                .set("gender_rule", genderRule)
                .set("age_min", ageMin)
                .set("age_max", ageMax)
                .set("crowd_tags_json", crowdTagsJson)
                .setSql("update_time = current_timestamp"));
        return entity.id == null ? 0L : entity.id;
    }

    public boolean capabilityExists(String capabilityCode) {
        Long count = medicalCapabilityCatalogMapper.selectCount(new QueryWrapper<MedicalCapabilityCatalogEntity>()
                .eq("capability_code", capabilityCode)
                .eq("active_status", 1));
        return count != null && count > 0;
    }

    public void upsertDepartmentCapabilityRelation(long departmentId,
                                                   String capabilityCode,
                                                   String supportLevel,
                                                   double weight,
                                                   String source) {
        DepartmentCapabilityRelEntity entity = departmentCapabilityRelMapper.selectOne(new QueryWrapper<DepartmentCapabilityRelEntity>()
                .eq("department_id", departmentId)
                .eq("capability_code", capabilityCode)
                .last("limit 1"));
        if (entity == null) {
            entity = new DepartmentCapabilityRelEntity();
            entity.departmentId = departmentId;
            entity.capabilityCode = capabilityCode;
            entity.supportLevel = supportLevel;
            entity.weight = BigDecimal.valueOf(weight);
            entity.source = source;
            departmentCapabilityRelMapper.insert(entity);
            return;
        }
        departmentCapabilityRelMapper.update(null, new UpdateWrapper<DepartmentCapabilityRelEntity>()
                .eq("id", entity.id)
                .set("support_level", supportLevel)
                .set("weight", BigDecimal.valueOf(weight))
                .set("source", source));
    }

    public Map<String, Integer> aggregateCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("diseases", toInt(diseaseMasterMapper.selectCount(new QueryWrapper<DiseaseMasterEntity>().eq("deleted", 0))));
        counts.put("disease_aliases", toInt(diseaseAliasMapper.selectCount(new QueryWrapper<>())));
        counts.put("diseases_with_symptoms", toInt(diseaseMasterMapper.selectCount(new QueryWrapper<DiseaseMasterEntity>()
                .eq("deleted", 0)
                .isNotNull("symptom_keywords")
                .ne("symptom_keywords", "[]")
                .ne("symptom_keywords", ""))));
        counts.put("disease_capability_mapped", distinctCount(diseaseCapabilityRelMapper, "count(distinct disease_code) as total", null));
        counts.put("pending", toInt(importReviewItemMapper.selectCount(new QueryWrapper<ImportReviewItemEntity>().eq("resolved", 0))));
        counts.put("pending_department", toInt(importReviewItemMapper.selectCount(new QueryWrapper<ImportReviewItemEntity>()
                .eq("resolved", 0)
                .eq("dataset_type", "department"))));
        counts.put("pending_disease", toInt(importReviewItemMapper.selectCount(new QueryWrapper<ImportReviewItemEntity>()
                .eq("resolved", 0)
                .eq("dataset_type", "disease"))));
        counts.put("auto_mapped_departments", distinctCount(departmentCapabilityRelMapper, "count(distinct department_id) as total", "source like '%auto%'"));
        return counts;
    }

    public int countPendingReviews(String datasetType, Long jobId) {
        QueryWrapper<ImportReviewItemEntity> wrapper = new QueryWrapper<ImportReviewItemEntity>().eq("resolved", 0);
        appendReviewFilters(wrapper, datasetType, jobId);
        return toInt(importReviewItemMapper.selectCount(wrapper));
    }

    public Map<String, Integer> countReviewTypes(Long jobId) {
        return countReviewTypes(jobId, null);
    }

    public Map<String, Integer> countReviewTypes(Long jobId, String datasetType) {
        QueryWrapper<ImportReviewItemEntity> wrapper = new QueryWrapper<ImportReviewItemEntity>()
                .select("issue_type", "count(1) as total")
                .eq("resolved", 0);
        if (jobId != null) {
            wrapper.eq("job_id", jobId);
        }
        if (datasetType != null && !datasetType.isBlank()) {
            wrapper.eq("dataset_type", datasetType);
        }
        wrapper.groupBy("issue_type").last("order by total desc, issue_type asc");
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map<String, Object> row : importReviewItemMapper.selectMaps(wrapper)) {
            result.put(value(row.get("issue_type")), number(row.get("total")));
        }
        return result;
    }

    public Map<String, Integer> countFailureTypes(Long jobId) {
        QueryWrapper<ImportFailureLogEntity> wrapper = new QueryWrapper<ImportFailureLogEntity>()
                .select("error_message", "count(1) as total")
                .eq("job_id", jobId)
                .groupBy("error_message")
                .last("order by total desc, error_message asc");
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map<String, Object> row : importFailureLogMapper.selectMaps(wrapper)) {
            result.put(value(row.get("error_message")), number(row.get("total")));
        }
        return result;
    }

    public List<ImportReviewItemRecord> findPendingReviews(String datasetType, Long jobId, int limit) {
        QueryWrapper<ImportReviewItemEntity> wrapper = new QueryWrapper<ImportReviewItemEntity>()
                .eq("resolved", 0);
        appendReviewFilters(wrapper, datasetType, jobId);
        wrapper.orderByDesc("id").last("limit " + limit);
        return importReviewItemMapper.selectList(wrapper).stream()
                .map(this::toImportReviewItemRecord)
                .toList();
    }

    public int resolveReviewItem(Long reviewId, String resolutionNote) {
        return importReviewItemMapper.update(null, new UpdateWrapper<ImportReviewItemEntity>()
                .eq("id", reviewId)
                .eq("resolved", 0)
                .set("resolved", 1)
                .set("resolution_note", resolutionNote)
                .setSql("update_time = current_timestamp"));
    }

    public List<ImportJobRecord> findRecentJobs(int limit) {
        return importJobRecordMapper.selectList(new QueryWrapper<ImportJobRecordEntity>()
                        .orderByDesc("id")
                        .last("limit " + limit))
                .stream()
                .map(this::toImportJobRecord)
                .toList();
    }

    public ImportJobRecord findJobById(Long jobId) {
        ImportJobRecordEntity entity = importJobRecordMapper.selectById(jobId);
        return entity == null ? null : toImportJobRecord(entity);
    }

    public List<ImportFailureLogRecord> findFailuresByJobId(Long jobId, int limit) {
        return importFailureLogMapper.selectList(new QueryWrapper<ImportFailureLogEntity>()
                        .eq("job_id", jobId)
                        .orderByAsc("id")
                        .last("limit " + limit))
                .stream()
                .map(this::toImportFailureLogRecord)
                .toList();
    }

    public List<ImportReviewItemRecord> findRecentReviewsByJobId(Long jobId, int limit) {
        return importReviewItemMapper.selectList(new QueryWrapper<ImportReviewItemEntity>()
                        .eq("job_id", jobId)
                        .orderByDesc("id")
                        .last("limit " + limit))
                .stream()
                .map(this::toImportReviewItemRecord)
                .toList();
    }

    private void appendReviewFilters(QueryWrapper<ImportReviewItemEntity> wrapper, String datasetType, Long jobId) {
        if (datasetType != null && !datasetType.isBlank()) {
            wrapper.eq("dataset_type", datasetType);
        }
        if (jobId != null) {
            wrapper.eq("job_id", jobId);
        }
    }

    private ImportJobRecord toImportJobRecord(ImportJobRecordEntity entity) {
        return new ImportJobRecord(
                entity.id,
                entity.datasetType,
                entity.fileName,
                entity.status,
                entity.successCount,
                entity.failureCount,
                entity.reviewCount,
                entity.autoMappedCount,
                entity.message
        );
    }

    private ImportFailureLogRecord toImportFailureLogRecord(ImportFailureLogEntity entity) {
        return new ImportFailureLogRecord(
                entity.id,
                entity.jobId,
                entity.rowNumber,
                entity.rawContent,
                entity.errorMessage
        );
    }

    private ImportReviewItemRecord toImportReviewItemRecord(ImportReviewItemEntity entity) {
        return new ImportReviewItemRecord(
                entity.id,
                entity.jobId,
                entity.datasetType,
                entity.itemKey,
                entity.issueType,
                entity.rawContent,
                entity.suggestion,
                entity.resolved != null && entity.resolved == 1
        );
    }

    private int distinctCount(Object mapper, String selectSql, String customSegment) {
        List<Map<String, Object>> rows;
        if (mapper instanceof DiseaseCapabilityRelMapper diseaseMapper) {
            QueryWrapper<DiseaseCapabilityRelEntity> wrapper = new QueryWrapper<DiseaseCapabilityRelEntity>().select(selectSql);
            if (customSegment != null) {
                wrapper.apply(customSegment);
            }
            rows = diseaseMapper.selectMaps(wrapper);
        } else if (mapper instanceof DepartmentCapabilityRelMapper departmentMapper) {
            QueryWrapper<DepartmentCapabilityRelEntity> wrapper = new QueryWrapper<DepartmentCapabilityRelEntity>().select(selectSql);
            if (customSegment != null) {
                wrapper.apply(customSegment);
            }
            rows = departmentMapper.selectMaps(wrapper);
        } else {
            return 0;
        }
        if (rows.isEmpty()) {
            return 0;
        }
        return number(rows.get(0).get("total"));
    }

    private int toInt(Long value) {
        return value == null ? 0 : value.intValue();
    }

    private int number(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private String value(Object value) {
        return value == null ? "" : value.toString();
    }
}
