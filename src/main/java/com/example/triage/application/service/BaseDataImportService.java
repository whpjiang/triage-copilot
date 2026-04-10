package com.example.triage.application.service;

import com.example.triage.application.dto.BaseDataImportResponse;
import com.example.triage.infrastructure.importer.TabularDataReader;
import com.example.triage.infrastructure.persistence.repository.BaseDataAdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class BaseDataImportService {

    private final TabularDataReader tabularDataReader;
    private final BaseDataAdminRepository baseDataAdminRepository;
    private final DiseaseNormalizeService diseaseNormalizeService;
    private final DepartmentCapabilityAutoMappingService departmentCapabilityAutoMappingService;
    private final WuhanImportAdapterService wuhanImportAdapterService;

    public BaseDataImportService(TabularDataReader tabularDataReader,
                                 BaseDataAdminRepository baseDataAdminRepository,
                                 DiseaseNormalizeService diseaseNormalizeService,
                                 DepartmentCapabilityAutoMappingService departmentCapabilityAutoMappingService,
                                 WuhanImportAdapterService wuhanImportAdapterService) {
        this.tabularDataReader = tabularDataReader;
        this.baseDataAdminRepository = baseDataAdminRepository;
        this.diseaseNormalizeService = diseaseNormalizeService;
        this.departmentCapabilityAutoMappingService = departmentCapabilityAutoMappingService;
        this.wuhanImportAdapterService = wuhanImportAdapterService;
    }

    public BaseDataImportResponse importData(String datasetType, MultipartFile file) throws Exception {
        String normalizedType = normalizeDatasetType(datasetType);
        long jobId = baseDataAdminRepository.createImportJob(normalizedType, file.getOriginalFilename());
        int success = 0;
        int failure = 0;
        int review = 0;
        List<String> messages = new ArrayList<>();
        try {
            List<Map<String, String>> rows = tabularDataReader.read(file);
            int rowNumber = 1;
            for (Map<String, String> sourceRow : rows) {
                rowNumber++;
                try {
                    Map<String, String> row = wuhanImportAdapterService.adapt(normalizedType, sourceRow);
                    if (isDiseaseDataset(normalizedType)) {
                        review += importDiseaseRow(jobId, row);
                    } else {
                        review += importDepartmentRow(jobId, row, normalizedType);
                    }
                    success++;
                } catch (Exception ex) {
                    failure++;
                    baseDataAdminRepository.addFailure(jobId, rowNumber, sourceRow.toString(), ex.getMessage());
                }
            }
            String summary = "Imported " + success + " rows, failures " + failure + ", reviews " + review;
            baseDataAdminRepository.finishImportJob(jobId, success, failure, review, "DONE", summary);
            messages.add(summary);
        } catch (Exception ex) {
            baseDataAdminRepository.finishImportJob(jobId, success, failure, review, "FAILED", ex.getMessage());
            throw ex;
        }
        BaseDataImportResponse response = new BaseDataImportResponse();
        response.setJobId(jobId);
        response.setDatasetType(normalizedType);
        response.setSuccessCount(success);
        response.setFailureCount(failure);
        response.setReviewCount(review);
        response.setMessages(messages);
        return response;
    }

    private int importDiseaseRow(long jobId, Map<String, String> row) {
        String diseaseName = require(row, "disease_name");
        String diseaseCode = buildCode(firstNonBlank(row.get("disease_code"), diseaseName));
        List<String> aliases = diseaseNormalizeService.parseList(firstNonBlank(row.get("aliases"), row.get("aliases_json")));
        List<String> symptoms = diseaseNormalizeService.normalizeKeywords(firstNonBlank(row.get("symptom_keywords"), row.get("symptoms")));
        String genderRule = diseaseNormalizeService.normalizeGenderRule(row.get("gender_rule"));
        Integer ageMin = parseInteger(row.get("age_min"));
        Integer ageMax = parseInteger(row.get("age_max"));
        String ageGroup = firstNonBlank(row.get("age_group"), inferAgeGroup(ageMin, ageMax));
        String urgencyLevel = firstNonBlank(row.get("urgency_level"), "medium");
        baseDataAdminRepository.upsertDisease(
                diseaseCode,
                diseaseName,
                diseaseNormalizeService.toJsonArray(aliases),
                diseaseNormalizeService.toJsonArray(symptoms),
                genderRule,
                ageMin,
                ageMax,
                ageGroup,
                urgencyLevel,
                "approved"
        );
        baseDataAdminRepository.replaceDiseaseAliases(diseaseCode, aliases);
        int review = 0;
        if (symptoms.isEmpty()) {
            review++;
            baseDataAdminRepository.addReviewItem(jobId, "disease", diseaseCode, "MISSING_SYMPTOM_KEYWORDS", row.toString(), "请补充症状关键词");
        }
        if (!StringUtils.hasText(row.get("standard_dept_hint"))) {
            review++;
            baseDataAdminRepository.addReviewItem(jobId, "disease", diseaseCode, "MISSING_STANDARD_DEPT_HINT", row.toString(), "请补充疾病到医学能力的映射线索");
        }
        return review;
    }

    private int importDepartmentRow(long jobId, Map<String, String> row, String datasetType) {
        String hospitalName = require(row, "hospital_name");
        String departmentName = require(row, "department_name");
        String city = firstNonBlank(row.get("city"), "本地");
        long hospitalId = baseDataAdminRepository.upsertHospital(buildCode(hospitalName), hospitalName, city);
        long departmentId = baseDataAdminRepository.upsertDepartment(
                hospitalId,
                departmentName,
                row.get("parent_department_name"),
                row.get("department_intro"),
                row.get("service_scope"),
                diseaseNormalizeService.normalizeGenderRule(row.get("gender_rule")),
                parseInteger(row.get("age_min")),
                parseInteger(row.get("age_max")),
                diseaseNormalizeService.toJsonArray(diseaseNormalizeService.parseList(row.get("crowd_tags")))
        );

        int review = 0;
        List<DepartmentCapabilityAutoMappingService.CapabilityMappingSuggestion> suggestions =
                departmentCapabilityAutoMappingService.suggest(
                        departmentName,
                        row.get("parent_department_name"),
                        row.get("service_scope"),
                        row.get("department_intro")
                );
        int mappedCount = 0;
        for (DepartmentCapabilityAutoMappingService.CapabilityMappingSuggestion suggestion : suggestions) {
            if (!baseDataAdminRepository.capabilityExists(suggestion.capabilityCode())) {
                continue;
            }
            baseDataAdminRepository.upsertDepartmentCapabilityRelation(
                    departmentId,
                    suggestion.capabilityCode(),
                    suggestion.supportLevel(),
                    suggestion.weight(),
                    datasetType.startsWith("wuhan_") ? "wuhan-auto-rule" : "import-auto-rule"
            );
            mappedCount++;
        }
        if (mappedCount == 0) {
            review++;
            baseDataAdminRepository.addReviewItem(jobId, "department", String.valueOf(departmentId), "WAIT_CAPABILITY_MAPPING", row.toString(), "请为本地科室补充医学能力映射");
        } else if (mappedCount > 1) {
            review++;
            baseDataAdminRepository.addReviewItem(jobId, "department", String.valueOf(departmentId), "AUTO_MAPPING_NEEDS_REVIEW", row.toString(), "自动映射了多个医学能力，请人工复核");
        }
        return review;
    }

    private boolean isDiseaseDataset(String datasetType) {
        return "disease".equals(datasetType) || "wuhan_disease".equals(datasetType);
    }

    private String normalizeDatasetType(String datasetType) {
        String value = datasetType == null ? "" : datasetType.trim().toLowerCase(Locale.ROOT);
        if ("wuhan_department".equals(value) || "wuhan_hospital_department".equals(value)) {
            return "wuhan_department";
        }
        if ("wuhan_disease".equals(value)) {
            return "wuhan_disease";
        }
        if ("department".equals(value) || "hospital_department".equals(value)) {
            return "department";
        }
        return "disease";
    }

    private String require(Map<String, String> row, String key) {
        String value = row.get(key);
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("missing field: " + key);
        }
        return value.trim();
    }

    private String firstNonBlank(String first, String second) {
        return StringUtils.hasText(first) ? first.trim() : (StringUtils.hasText(second) ? second.trim() : "");
    }

    private Integer parseInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return Integer.parseInt(value.trim());
    }

    private String inferAgeGroup(Integer ageMin, Integer ageMax) {
        if (ageMax != null && ageMax <= 11) {
            return "child";
        }
        if (ageMin != null && ageMin >= 12 && ageMax != null && ageMax <= 17) {
            return "adolescent";
        }
        if (ageMin != null && ageMin >= 65) {
            return "elderly";
        }
        if (ageMin != null && ageMin >= 18) {
            return "adult";
        }
        return "all";
    }

    private String buildCode(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }
}
