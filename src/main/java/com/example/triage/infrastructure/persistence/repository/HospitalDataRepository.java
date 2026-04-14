package com.example.triage.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.triage.infrastructure.persistence.entity.HospitalDepartmentEntity;
import com.example.triage.infrastructure.persistence.entity.HospitalEntity;
import com.example.triage.infrastructure.persistence.mapper.DepartmentCapabilityRelMapper;
import com.example.triage.infrastructure.persistence.mapper.HospitalDepartmentMapper;
import com.example.triage.infrastructure.persistence.mapper.HospitalMapper;
import com.example.triage.infrastructure.persistence.model.DepartmentMappingRecord;
import com.example.triage.infrastructure.persistence.model.DepartmentSearchRecord;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Repository
public class HospitalDataRepository {

    private final DepartmentCapabilityRelMapper departmentCapabilityRelMapper;
    private final HospitalMapper hospitalMapper;
    private final HospitalDepartmentMapper hospitalDepartmentMapper;

    public HospitalDataRepository(DepartmentCapabilityRelMapper departmentCapabilityRelMapper,
                                  HospitalMapper hospitalMapper,
                                  HospitalDepartmentMapper hospitalDepartmentMapper) {
        this.departmentCapabilityRelMapper = departmentCapabilityRelMapper;
        this.hospitalMapper = hospitalMapper;
        this.hospitalDepartmentMapper = hospitalDepartmentMapper;
    }

    public List<DepartmentMappingRecord> findDepartmentMappings(List<String> capabilityCodes, String city) {
        if (capabilityCodes == null || capabilityCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return departmentCapabilityRelMapper.selectDepartmentMappings(capabilityCodes, city);
    }

    public List<HospitalEntity> searchHospitalsByQuery(String city, String area, String query) {
        QueryWrapper<HospitalEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0).eq("active_status", 1);
        if (StringUtils.hasText(city)) {
            queryWrapper.eq("city", city);
        }
        if (StringUtils.hasText(area)) {
            queryWrapper.eq("district_name", area);
        }
        if (StringUtils.hasText(query)) {
            queryWrapper.and(w -> w.like("hospital_name", query)
                    .or().like("hospital_code", query)
                    .or().like("hospital_level", query));
        }
        queryWrapper.orderByDesc("authority_score").orderByAsc("id");
        return hospitalMapper.selectList(queryWrapper);
    }

    public List<DepartmentSearchRecord> searchDepartmentsByQuery(String city, String area, String query) {
        return hospitalDepartmentMapper.selectDepartmentsByQuery(city, area, query);
    }

    public List<HospitalEntity> findEmergencyHospitals(String city, String area) {
        QueryWrapper<HospitalEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                .eq("active_status", 1)
                .eq("is_emergency", 1);
        if (StringUtils.hasText(city)) {
            queryWrapper.eq("city", city);
        }
        if (StringUtils.hasText(area)) {
            queryWrapper.eq("district_name", area);
        }
        queryWrapper.orderByDesc("authority_score").orderByAsc("id");
        return hospitalMapper.selectList(queryWrapper);
    }

    public int countHospitals() {
        Long count = hospitalMapper.selectCount(new QueryWrapper<HospitalEntity>()
                .eq("deleted", 0)
                .eq("active_status", 1));
        return count == null ? 0 : count.intValue();
    }

    public int countDepartments() {
        Long count = hospitalDepartmentMapper.selectCount(new QueryWrapper<HospitalDepartmentEntity>()
                .eq("deleted", 0)
                .eq("active_status", 1));
        return count == null ? 0 : count.intValue();
    }

    public int countDepartmentCapabilityRelations() {
        Long count = departmentCapabilityRelMapper.selectCount(new QueryWrapper<>());
        return count == null ? 0 : count.intValue();
    }
}
