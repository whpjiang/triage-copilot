package com.example.triage.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.triage.infrastructure.persistence.entity.MedicalCapabilityCatalogEntity;
import com.example.triage.infrastructure.persistence.mapper.MedicalCapabilityCatalogMapper;
import com.example.triage.infrastructure.persistence.model.CapabilityRecord;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class CapabilityDataRepository {

    private final MedicalCapabilityCatalogMapper medicalCapabilityCatalogMapper;

    public CapabilityDataRepository(MedicalCapabilityCatalogMapper medicalCapabilityCatalogMapper) {
        this.medicalCapabilityCatalogMapper = medicalCapabilityCatalogMapper;
    }

    public List<CapabilityRecord> findByCapabilityCodes(List<String> capabilityCodes) {
        if (capabilityCodes == null || capabilityCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return medicalCapabilityCatalogMapper.selectList(new QueryWrapper<MedicalCapabilityCatalogEntity>()
                        .eq("active_status", 1)
                        .in("capability_code", capabilityCodes))
                .stream()
                .map(this::toCapabilityRecord)
                .toList();
    }

    public int countCapabilities() {
        Long count = medicalCapabilityCatalogMapper.selectCount(new QueryWrapper<MedicalCapabilityCatalogEntity>()
                .eq("active_status", 1));
        return count == null ? 0 : count.intValue();
    }

    private CapabilityRecord toCapabilityRecord(MedicalCapabilityCatalogEntity entity) {
        return new CapabilityRecord(
                entity.capabilityCode,
                entity.capabilityName,
                entity.capabilityType,
                entity.parentCode,
                entity.standardDeptCode,
                entity.aliasesJson,
                entity.genderRule,
                entity.ageMin,
                entity.ageMax,
                entity.crowdTagsJson,
                entity.pathwayTagsJson,
                entity.activeStatus
        );
    }
}
