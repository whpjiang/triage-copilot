package com.example.triage.application.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class WuhanImportAdapterService {

    public Map<String, String> adapt(String datasetType, Map<String, String> source) {
        if ("wuhan_department".equals(datasetType)) {
            return adaptDepartment(source);
        }
        if ("wuhan_disease".equals(datasetType)) {
            return adaptDisease(source);
        }
        return source;
    }

    private Map<String, String> adaptDepartment(Map<String, String> source) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("hospital_name", first(source, "hospital_name", "医院名称", "医院", "医疗机构名称"));
        row.put("department_name", first(source, "department_name", "科室名称", "科室"));
        row.put("city", first(source, "city", "所属城市", "城市", "地区"));
        row.put("parent_department_name", first(source, "parent_department_name", "父级科室", "上级科室", "一级科室"));
        row.put("department_intro", first(source, "department_intro", "科室简介", "简介"));
        row.put("service_scope", first(source, "service_scope", "诊疗范围", "服务范围", "擅长方向"));
        row.put("gender_rule", first(source, "gender_rule", "适用性别", "性别规则"));
        row.put("age_min", first(source, "age_min", "最小年龄", "年龄下限"));
        row.put("age_max", first(source, "age_max", "最大年龄", "年龄上限"));
        row.put("crowd_tags", first(source, "crowd_tags", "人群标签"));
        if (!StringUtils.hasText(row.get("city"))) {
            row.put("city", "武汉");
        }
        return row;
    }

    private Map<String, String> adaptDisease(Map<String, String> source) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("disease_name", first(source, "disease_name", "疾病名称", "标准病种"));
        row.put("disease_code", first(source, "disease_code", "疾病编码"));
        row.put("aliases", first(source, "aliases", "别名", "疾病别名"));
        row.put("symptom_keywords", first(source, "symptom_keywords", "症状关键词", "关键词", "常见症状"));
        row.put("gender_rule", first(source, "gender_rule", "性别规则", "适用性别"));
        row.put("age_min", first(source, "age_min", "最小年龄", "年龄下限"));
        row.put("age_max", first(source, "age_max", "最大年龄", "年龄上限"));
        row.put("age_group", first(source, "age_group", "年龄分层", "年龄段"));
        row.put("urgency_level", first(source, "urgency_level", "紧急程度"));
        row.put("standard_dept_hint", first(source, "standard_dept_hint", "标准科室", "医学能力线索", "建议科室"));
        return row;
    }

    private String first(Map<String, String> source, String... aliases) {
        for (String alias : aliases) {
            String value = source.get(alias);
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }
}
