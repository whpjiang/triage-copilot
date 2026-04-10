package com.example.triage.application.service;

import com.example.triage.application.dto.BaseDataTemplateResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class BaseDataTemplateService {

    public BaseDataTemplateResponse getTemplate(String datasetType) {
        String normalizedType = normalizeDatasetType(datasetType);
        BaseDataTemplateResponse response = new BaseDataTemplateResponse();
        response.setDatasetType(normalizedType);
        if ("department".equals(normalizedType)) {
            response.setFileName("department-template.csv");
            response.setRequiredFields(List.of("hospital_name", "department_name"));
            response.setOptionalFields(List.of(
                    "city",
                    "parent_department_name",
                    "department_intro",
                    "service_scope",
                    "gender_rule",
                    "age_min",
                    "age_max",
                    "crowd_tags"
            ));
            response.setCsvTemplate("""
                    hospital_name,department_name,city,parent_department_name,department_intro,service_scope,gender_rule,age_min,age_max,crowd_tags
                    示例医院,脊柱外科门诊,上海,骨科,诊治脊柱退变与腰腿痛,腰椎间盘突出|脊柱退变,all,16,80,adult|spine
                    """);
            return response;
        }
        response.setFileName("disease-template.csv");
        response.setRequiredFields(List.of("disease_name"));
        response.setOptionalFields(List.of(
                "disease_code",
                "aliases",
                "symptom_keywords",
                "gender_rule",
                "age_min",
                "age_max",
                "age_group",
                "urgency_level",
                "standard_dept_hint"
        ));
        response.setCsvTemplate("""
                disease_name,disease_code,aliases,symptom_keywords,gender_rule,age_min,age_max,age_group,urgency_level,standard_dept_hint
                腰椎间盘突出,lumbar_disc_herniation,腰突|腰腿痛,腰痛|下肢麻木|放射痛,all,16,80,adult,medium,脊柱外科
                """);
        return response;
    }

    private String normalizeDatasetType(String datasetType) {
        String value = datasetType == null ? "" : datasetType.trim().toLowerCase(Locale.ROOT);
        if ("department".equals(value) || "hospital_department".equals(value)) {
            return "department";
        }
        return "disease";
    }
}
