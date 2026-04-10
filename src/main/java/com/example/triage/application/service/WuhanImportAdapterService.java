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
        row.put("hospital_name", first(source,
                "hospital_name", "医院名称", "医院", "医疗机构名称", "hospital_name_raw", "hospital_name_norm", "name_raw", "name_norm", "hospital_name_candidate"));
        row.put("department_name", first(source,
                "department_name", "科室名称", "科室", "门诊名称", "service_unit_name", "dept_name", "name_raw", "name_norm"));
        row.put("city", first(source,
                "city", "所属城市", "城市", "地区"));
        row.put("parent_department_name", first(source,
                "parent_department_name", "父级科室", "上级科室", "一级科室", "parent_name", "dept_parent_name"));
        row.put("department_intro", first(source,
                "department_intro", "科室简介", "简介", "intro_text", "科室介绍"));
        row.put("service_scope", first(source,
                "service_scope", "诊疗范围", "服务范围", "擅长方向", "specialty_text", "诊疗项目"));
        row.put("gender_rule", first(source,
                "gender_rule", "适用性别", "性别规则", "gender_limit", "gender"));
        row.put("age_min", first(source,
                "age_min", "最小年龄", "年龄下限"));
        row.put("age_max", first(source,
                "age_max", "最大年龄", "年龄上限"));
        row.put("age_range", first(source,
                "age_range", "年龄范围", "适龄人群"));
        row.put("crowd_tags", first(source,
                "crowd_tags", "人群标签", "crowd_limit"));
        if (!StringUtils.hasText(row.get("city"))) {
            row.put("city", "武汉");
        }
        return row;
    }

    private Map<String, String> adaptDisease(Map<String, String> source) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("disease_name", first(source,
                "disease_name", "疾病名称", "标准病种", "病种名称", "诊断名称", "名称"));
        row.put("disease_code", first(source,
                "disease_code", "疾病编码", "病种编码", "diag_code"));
        row.put("aliases", first(source,
                "aliases", "别名", "疾病别名", "同义词", "俗称"));
        row.put("symptom_keywords", first(source,
                "symptom_keywords", "症状关键词", "关键词", "常见症状", "症状", "主诉关键词"));
        row.put("gender_rule", first(source,
                "gender_rule", "性别规则", "适用性别", "性别", "gender_limit"));
        row.put("age_min", first(source,
                "age_min", "最小年龄", "年龄下限"));
        row.put("age_max", first(source,
                "age_max", "最大年龄", "年龄上限"));
        row.put("age_range", first(source,
                "age_range", "年龄范围", "适龄人群"));
        row.put("age_group", first(source,
                "age_group", "年龄分层", "年龄段"));
        row.put("urgency_level", first(source,
                "urgency_level", "紧急程度", "风险等级"));
        row.put("standard_dept_hint", first(source,
                "standard_dept_hint", "标准科室", "医学能力线索", "建议科室", "推荐科室"));
        return row;
    }

    private String first(Map<String, String> source, String... aliases) {
        for (String alias : aliases) {
            String value = source.get(alias == null ? null : alias.toLowerCase());
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }
}
