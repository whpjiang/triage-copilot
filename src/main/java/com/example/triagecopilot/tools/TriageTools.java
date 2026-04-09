package com.example.triagecopilot.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.triagecopilot.entity.DictFunctionalClinic;
import com.example.triagecopilot.entity.DictStandardDept;
import com.example.triagecopilot.entity.Doctor;
import com.example.triagecopilot.entity.Hospital;
import com.example.triagecopilot.entity.ServiceUnit;
import com.example.triagecopilot.mapper.DictFunctionalClinicMapper;
import com.example.triagecopilot.mapper.DictStandardDeptMapper;
import com.example.triagecopilot.mapper.DoctorMapper;
import com.example.triagecopilot.mapper.HospitalMapper;
import com.example.triagecopilot.mapper.ServiceUnitMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class TriageTools {

    private final DictStandardDeptMapper dictStandardDeptMapper;
    private final DictFunctionalClinicMapper dictFunctionalClinicMapper;
    private final HospitalMapper hospitalMapper;
    private final DoctorMapper doctorMapper;
    private final ServiceUnitMapper serviceUnitMapper;

    public TriageTools(DictStandardDeptMapper dictStandardDeptMapper,
                       DictFunctionalClinicMapper dictFunctionalClinicMapper,
                       HospitalMapper hospitalMapper,
                       DoctorMapper doctorMapper,
                       ServiceUnitMapper serviceUnitMapper) {
        this.dictStandardDeptMapper = dictStandardDeptMapper;
        this.dictFunctionalClinicMapper = dictFunctionalClinicMapper;
        this.hospitalMapper = hospitalMapper;
        this.doctorMapper = doctorMapper;
        this.serviceUnitMapper = serviceUnitMapper;
    }

    @Tool(description = "Search standard medical departments by keyword")
    public List<Map<String, Object>> searchStandardDepartments(String keyword) {
        LambdaQueryWrapper<DictStandardDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictStandardDept::getIsActive, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(DictStandardDept::getName, keyword)
                    .or()
                    .like(DictStandardDept::getCode, keyword));
        }
        wrapper.orderByAsc(DictStandardDept::getSortNo).last("limit 8");

        List<DictStandardDept> rows = dictStandardDeptMapper.selectList(wrapper);
        List<Map<String, Object>> out = new ArrayList<>();
        for (DictStandardDept row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", row.getCode());
            item.put("name", row.getName());
            item.put("parentCode", row.getParentCode());
            out.add(item);
        }
        return out;
    }

    @Tool(description = "Search functional clinics by keyword")
    public List<Map<String, Object>> searchFunctionalClinics(String keyword) {
        LambdaQueryWrapper<DictFunctionalClinic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictFunctionalClinic::getIsActive, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(DictFunctionalClinic::getName, keyword)
                    .or()
                    .like(DictFunctionalClinic::getCode, keyword));
        }
        wrapper.orderByAsc(DictFunctionalClinic::getSortNo).last("limit 8");

        List<DictFunctionalClinic> rows = dictFunctionalClinicMapper.selectList(wrapper);
        List<Map<String, Object>> out = new ArrayList<>();
        for (DictFunctionalClinic row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", row.getCode());
            item.put("name", row.getName());
            out.add(item);
        }
        return out;
    }

    @Tool(description = "Search hospitals by city and department keyword")
    public List<Map<String, Object>> searchHospitals(String city, String keyword) {
        LambdaQueryWrapper<Hospital> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Hospital::getIsActive, 1)
                .like(StringUtils.hasText(city), Hospital::getCity, city)
                .and(StringUtils.hasText(keyword), w -> w.like(Hospital::getNameNorm, keyword)
                        .or().like(Hospital::getNameRaw, keyword)
                        .or().like(Hospital::getHospitalType, keyword))
                .orderByDesc(Hospital::getHospitalLevel)
                .last("limit 10");

        List<Hospital> rows = hospitalMapper.selectList(wrapper);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Hospital row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.getHospitalId());
            item.put("name", StringUtils.hasText(row.getNameNorm()) ? row.getNameNorm() : row.getNameRaw());
            item.put("city", row.getCity());
            item.put("level", row.getHospitalLevel());
            item.put("hasEmergency", row.getHasEmergency());
            item.put("regPhone", row.getRegEntryPhone());
            out.add(item);
        }
        return out;
    }

    @Tool(description = "Search service units by hospital id and keyword")
    public List<Map<String, Object>> searchServiceUnits(Long hospitalId, String keyword) {
        LambdaQueryWrapper<ServiceUnit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceUnit::getIsActive, 1)
                .eq(hospitalId != null, ServiceUnit::getHospitalId, hospitalId)
                .and(StringUtils.hasText(keyword), w -> w.like(ServiceUnit::getNameNorm, keyword)
                        .or().like(ServiceUnit::getNameRaw, keyword))
                .last("limit 10");

        List<ServiceUnit> rows = serviceUnitMapper.selectList(wrapper);
        List<Map<String, Object>> out = new ArrayList<>();
        for (ServiceUnit row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.getServiceUnitId());
            item.put("hospitalId", row.getHospitalId());
            item.put("name", StringUtils.hasText(row.getNameNorm()) ? row.getNameNorm() : row.getNameRaw());
            item.put("unitType", row.getUnitType());
            out.add(item);
        }
        return out;
    }

    @Tool(description = "Search doctors by keyword")
    public List<Map<String, Object>> searchDoctors(String keyword) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getIsActive, 1)
                .and(StringUtils.hasText(keyword), w -> w.like(Doctor::getNameNorm, keyword)
                        .or().like(Doctor::getNameRaw, keyword)
                        .or().like(Doctor::getSpecialtyText, keyword))
                .last("limit 10");

        List<Doctor> rows = doctorMapper.selectList(wrapper);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Doctor row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.getDoctorId());
            item.put("name", StringUtils.hasText(row.getNameNorm()) ? row.getNameNorm() : row.getNameRaw());
            item.put("title", row.getTitle());
            item.put("hospitalId", row.getHospitalId());
            item.put("specialty", row.getSpecialtyText());
            out.add(item);
        }
        return out;
    }
}
