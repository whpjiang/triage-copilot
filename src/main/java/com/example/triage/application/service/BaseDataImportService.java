package com.example.triage.application.service;

import com.example.triage.application.dto.BaseDataImportResponse;
import com.example.triage.infrastructure.importer.TabularDataReader;
import com.example.triage.infrastructure.persistence.repository.BaseDataAdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private final DiseaseCapabilityHintService diseaseCapabilityHintService;

    public BaseDataImportService(TabularDataReader tabularDataReader,
                                 BaseDataAdminRepository baseDataAdminRepository,
                                 DiseaseNormalizeService diseaseNormalizeService,
                                 DepartmentCapabilityAutoMappingService departmentCapabilityAutoMappingService,
                                 WuhanImportAdapterService wuhanImportAdapterService,
                                 DiseaseCapabilityHintService diseaseCapabilityHintService) {
        this.tabularDataReader = tabularDataReader;
        this.baseDataAdminRepository = baseDataAdminRepository;
        this.diseaseNormalizeService = diseaseNormalizeService;
        this.departmentCapabilityAutoMappingService = departmentCapabilityAutoMappingService;
        this.wuhanImportAdapterService = wuhanImportAdapterService;
        this.diseaseCapabilityHintService = diseaseCapabilityHintService;
    }

    public BaseDataImportResponse importData(String datasetType, MultipartFile file) throws Exception {
        String normalizedType = normalizeDatasetType(datasetType);
        long jobId = baseDataAdminRepository.createImportJob(normalizedType, file.getOriginalFilename());
        int success = 0;
        int failure = 0;
        int review = 0;
        int autoMapped = 0;
        List<String> messages = new ArrayList<>();
        try {
            List<Map<String, String>> rows = tabularDataReader.read(file, resolveSheetName(normalizedType));
            int rowNumber = 1;
            for (Map<String, String> sourceRow : rows) {
                rowNumber++;
                try {
                    Map<String, String> row = wuhanImportAdapterService.adapt(normalizedType, sourceRow);
                    RowImportResult result = isDiseaseDataset(normalizedType)
                            ? importDiseaseRow(jobId, rowNumber, row)
                            : importDepartmentRow(jobId, rowNumber, row, normalizedType);
                    success++;
                    review += result.reviewCount();
                    autoMapped += result.autoMappedCount();
                } catch (Exception ex) {
                    failure++;
                    baseDataAdminRepository.addFailure(jobId, rowNumber, sourceRow.toString(), ex.getMessage());
                    review += createFailureReview(jobId, normalizedType, rowNumber, sourceRow, ex.getMessage());
                }
            }
            String summary = "Imported %s rows, failures %s, auto-mapped %s, reviews %s"
                    .formatted(success, failure, autoMapped, review);
            baseDataAdminRepository.finishImportJob(jobId, success, failure, review, autoMapped, "DONE", summary);
            messages.add(summary);
        } catch (Exception ex) {
            baseDataAdminRepository.finishImportJob(jobId, success, failure, review, autoMapped, "FAILED", ex.getMessage());
            throw ex;
        }
        BaseDataImportResponse response = new BaseDataImportResponse();
        response.setJobId(jobId);
        response.setDatasetType(normalizedType);
        response.setSuccessCount(success);
        response.setFailureCount(failure);
        response.setReviewCount(review);
        response.setAutoMappedCount(autoMapped);
        response.setReviewTypeDistribution(baseDataAdminRepository.countReviewTypes(jobId));
        response.setCommonIssueDistribution(baseDataAdminRepository.countFailureTypes(jobId));
        response.setMessages(messages);
        return response;
    }

    private RowImportResult importDiseaseRow(long jobId, int rowNumber, Map<String, String> row) {
        String diseaseName = cleanText(row.get("disease_name"));
        if (!StringUtils.hasText(diseaseName)) {
            throw new IllegalArgumentException("missing field: disease_name");
        }
        String diseaseCode = buildCode(firstNonBlank(row.get("disease_code"), diseaseName));
        List<String> aliases = diseaseNormalizeService.parseList(firstNonBlank(row.get("aliases"), row.get("aliases_json")));
        List<String> symptoms = diseaseNormalizeService.normalizeKeywords(firstNonBlank(row.get("symptom_keywords"), row.get("symptoms")));
        NormalizedGenderRule genderRule = normalizeGenderRule(row);
        AgeBoundary ageBoundary = resolveAgeBoundary(row);
        String ageGroup = firstNonBlank(row.get("age_group"), inferAgeGroup(ageBoundary.ageMin(), ageBoundary.ageMax()));
        String urgencyLevel = firstNonBlank(row.get("urgency_level"), "medium");

        baseDataAdminRepository.upsertDisease(
                diseaseCode,
                diseaseName.trim(),
                diseaseNormalizeService.toJsonArray(aliases),
                diseaseNormalizeService.toJsonArray(symptoms),
                genderRule.genderRule(),
                ageBoundary.ageMin(),
                ageBoundary.ageMax(),
                ageGroup,
                urgencyLevel,
                "approved"
        );
        baseDataAdminRepository.replaceDiseaseAliases(diseaseCode, aliases);

        int review = 0;
        if (symptoms.isEmpty()) {
            review += addReview(jobId, "disease", diseaseCode, "MISSING_SYMPTOM_KEYWORDS", row, "请补充症状关键词");
        }
        if (!genderRule.recognized()) {
            review += addReview(jobId, "disease", diseaseCode, "UNRECOGNIZED_GENDER_RULE", row, "请人工确认性别规则");
        }
        if (!ageBoundary.recognized()) {
            review += addReview(jobId, "disease", diseaseCode, "UNRECOGNIZED_AGE_RANGE", row, "请人工确认年龄范围");
        }

        DiseaseCapabilityHintService.HintMappingResult hintMapping = diseaseCapabilityHintService.map(row.get("standard_dept_hint"));
        if (hintMapping.mapped()) {
            String capabilityCode = hintMapping.capabilityCodes().getFirst();
            baseDataAdminRepository.upsertDiseaseCapabilityRelation(
                    diseaseCode,
                    capabilityCode,
                    "PRIMARY",
                    1.00D,
                    "auto-mapped from standard_dept_hint row " + rowNumber
            );
        } else {
            String issueType = hintMapping.reviewIssueType() == null ? "MISSING_STANDARD_DEPT_HINT" : hintMapping.reviewIssueType();
            review += addReview(jobId, "disease", diseaseCode, issueType, row,
                    reviewSuggestionForHint(issueType, row.get("standard_dept_hint"), hintMapping.capabilityCodes()));
        }
        return new RowImportResult(review, 0);
    }

    private RowImportResult importDepartmentRow(long jobId, int rowNumber, Map<String, String> row, String datasetType) {
        String hospitalName = require(row, "hospital_name");
        String departmentName = require(row, "department_name");
        String city = firstNonBlank(row.get("city"), "武汉");
        AgeBoundary ageBoundary = resolveAgeBoundary(row);

        long hospitalId = baseDataAdminRepository.upsertHospital(buildCode(hospitalName), cleanText(hospitalName), city);
        long departmentId = baseDataAdminRepository.upsertDepartment(
                hospitalId,
                cleanText(departmentName),
                cleanText(row.get("parent_department_name")),
                cleanText(row.get("department_intro")),
                cleanText(row.get("service_scope")),
                diseaseNormalizeService.normalizeGenderRule(row.get("gender_rule")),
                ageBoundary.ageMin(),
                ageBoundary.ageMax(),
                diseaseNormalizeService.toJsonArray(diseaseNormalizeService.parseList(firstNonBlank(row.get("crowd_tags"), row.get("age_group"))))
        );

        int review = 0;
        if (isHospitalNameSuspicious(hospitalName)) {
            review += addReview(jobId, "hospital", buildCode(hospitalName), "HOSPITAL_NAME_NEEDS_CONFIRM", row,
                    "医院名称疑似占位符或待确认，请人工确认");
        }

        DepartmentCapabilityAutoMappingService.MappingEvaluation evaluation =
                departmentCapabilityAutoMappingService.evaluate(
                        departmentName,
                        row.get("parent_department_name"),
                        row.get("service_scope"),
                        row.get("department_intro")
                );

        int autoMapped = 0;
        for (DepartmentCapabilityAutoMappingService.CapabilityMappingSuggestion suggestion : evaluation.suggestions()) {
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
            autoMapped++;
        }

        for (String issueType : evaluation.reviewIssues()) {
            review += addReview(jobId, "department", String.valueOf(departmentId), issueType, row, evaluation.reviewSuggestion());
        }
        if (!ageBoundary.recognized() && StringUtils.hasText(firstNonBlank(row.get("age_range"), row.get("crowd_tags")))) {
            review += addReview(jobId, "department", String.valueOf(departmentId), "UNRECOGNIZED_AGE_RANGE", row, "请人工确认科室适用年龄范围");
        }
        return new RowImportResult(review, autoMapped);
    }

    private int createFailureReview(long jobId,
                                    String datasetType,
                                    int rowNumber,
                                    Map<String, String> row,
                                    String errorMessage) {
        String issueType = "IMPORT_FAILURE";
        if (errorMessage != null && errorMessage.contains("disease_name")) {
            issueType = "MISSING_DISEASE_NAME";
        } else if (errorMessage != null && errorMessage.contains("hospital_name")) {
            issueType = "MISSING_HOSPITAL_NAME";
        } else if (errorMessage != null && errorMessage.contains("department_name")) {
            issueType = "MISSING_DEPARTMENT_NAME";
        }
        baseDataAdminRepository.addReviewItem(jobId, datasetKey(datasetType), "row-" + rowNumber, issueType, row.toString(), "该行导入失败，请人工修正后重试");
        return 1;
    }

    private int addReview(long jobId, String datasetType, String itemKey, String issueType, Map<String, String> row, String suggestion) {
        baseDataAdminRepository.addReviewItem(jobId, datasetType, itemKey, issueType, row.toString(), suggestion);
        return 1;
    }

    private String datasetKey(String normalizedType) {
        return isDiseaseDataset(normalizedType) ? "disease" : "department";
    }

    private NormalizedGenderRule normalizeGenderRule(Map<String, String> row) {
        String source = firstNonBlank(row.get("gender_rule"), row.get("crowd_tags"));
        if (!StringUtils.hasText(source)) {
            return new NormalizedGenderRule("all", true);
        }
        String normalized = diseaseNormalizeService.normalizeText(source);
        if (containsAny(normalized, "女", "female", "妇")) {
            return new NormalizedGenderRule("female_only", true);
        }
        if (containsAny(normalized, "男", "male", "前列腺", "andrology", "男科")) {
            return new NormalizedGenderRule("male_only", true);
        }
        if (containsAny(normalized, "all", "不限", "通用", "unknown", "全人群")) {
            return new NormalizedGenderRule("all", true);
        }
        return new NormalizedGenderRule("all", false);
    }

    private AgeBoundary resolveAgeBoundary(Map<String, String> row) {
        Integer ageMin = parseInteger(row.get("age_min"));
        Integer ageMax = parseInteger(row.get("age_max"));
        if (ageMin != null || ageMax != null) {
            return new AgeBoundary(ageMin, ageMax, true);
        }
        String range = firstNonBlank(row.get("age_range"), row.get("age_group"), row.get("crowd_tags"));
        if (!StringUtils.hasText(range)) {
            return new AgeBoundary(null, null, true);
        }
        String normalized = diseaseNormalizeService.normalizeText(range);
        if (containsAny(normalized, "儿童", "儿科", "child", "小儿")) {
            return new AgeBoundary(0, 11, true);
        }
        if (containsAny(normalized, "青少年", "adolescent", "teen")) {
            return new AgeBoundary(12, 17, true);
        }
        if (containsAny(normalized, "成人", "adult")) {
            return new AgeBoundary(18, 64, true);
        }
        if (containsAny(normalized, "老年", "elderly")) {
            return new AgeBoundary(65, 120, true);
        }
        String digits = normalized.replaceAll("[^0-9\\-~至岁]", " ").trim();
        if (digits.matches(".*\\d+.*\\d+.*")) {
            List<Integer> values = new ArrayList<>();
            for (String part : digits.split("[^0-9]+")) {
                if (StringUtils.hasText(part)) {
                    values.add(Integer.parseInt(part));
                }
            }
            if (values.size() >= 2) {
                return new AgeBoundary(values.get(0), values.get(1), true);
            }
            if (values.size() == 1) {
                return new AgeBoundary(values.get(0), null, false);
            }
        }
        return new AgeBoundary(null, null, false);
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

    private String resolveSheetName(String datasetType) {
        return switch (datasetType) {
            case "wuhan_disease" -> "Import_Disease";
            case "wuhan_department" -> "Import_Department";
            default -> null;
        };
    }

    private String require(Map<String, String> row, String key) {
        String value = row.get(key);
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("missing field: " + key);
        }
        return value.trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private Integer parseInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String digits = value.trim().replaceAll("[^0-9]", "");
        return StringUtils.hasText(digits) ? Integer.parseInt(digits) : null;
    }

    private String inferAgeGroup(Integer ageMin, Integer ageMax) {
        if (ageMin == null && ageMax == null) {
            return "all";
        }
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

    private String reviewSuggestionForHint(String issueType, String hint, List<String> capabilityCodes) {
        if ("DISEASE_CAPABILITY_UNMAPPED".equals(issueType)) {
            return "standard_dept_hint 无法映射到医学能力，请人工补充; hint=%s".formatted(firstNonBlank(hint));
        }
        if ("DISEASE_CAPABILITY_MULTI_MATCH".equals(issueType)) {
            return "standard_dept_hint 命中多个医学能力，请人工确认; hint=%s; matched=%s".formatted(firstNonBlank(hint), capabilityCodes);
        }
        return "请补充疾病到医学能力的映射线索";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isHospitalNameSuspicious(String hospitalName) {
        String normalized = diseaseNormalizeService.normalizeText(hospitalName);
        return normalized.matches("^医院[_-]?\\d+$")
                || normalized.matches("^hospital[_-]?\\d+$")
                || normalized.startsWith("demo");
    }

    private String cleanText(String value) {
        return value == null ? null : value.trim();
    }

    private String buildCode(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    private record RowImportResult(int reviewCount, int autoMappedCount) {
    }

    private record AgeBoundary(Integer ageMin, Integer ageMax, boolean recognized) {
    }

    private record NormalizedGenderRule(String genderRule, boolean recognized) {
    }
}
