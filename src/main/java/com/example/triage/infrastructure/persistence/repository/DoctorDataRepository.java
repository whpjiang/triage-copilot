package com.example.triage.infrastructure.persistence.repository;

import com.example.triage.infrastructure.persistence.mapper.DoctorProfileMapper;
import com.example.triage.infrastructure.persistence.model.DoctorRecord;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class DoctorDataRepository {

    private final DoctorProfileMapper doctorProfileMapper;

    public DoctorDataRepository(DoctorProfileMapper doctorProfileMapper) {
        this.doctorProfileMapper = doctorProfileMapper;
    }

    public List<DoctorRecord> findDoctorsByDepartmentIds(List<Long> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return Collections.emptyList();
        }
        return doctorProfileMapper.selectDoctorsByDepartmentIds(departmentIds);
    }
}
