package com.example.triage.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.triage.infrastructure.persistence.entity.DiseaseCapabilityRelEntity;
import com.example.triage.infrastructure.persistence.entity.DiseaseMasterEntity;
import com.example.triage.infrastructure.persistence.mapper.DiseaseCapabilityRelMapper;
import com.example.triage.infrastructure.persistence.mapper.DiseaseMasterMapper;
import com.example.triage.infrastructure.persistence.model.DiseaseCapabilityRelationRecord;
import com.example.triage.infrastructure.persistence.model.DiseaseRecord;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class DiseaseDataRepository {

    private final DiseaseMasterMapper diseaseMasterMapper;
    private final DiseaseCapabilityRelMapper diseaseCapabilityRelMapper;

    public DiseaseDataRepository(DiseaseMasterMapper diseaseMasterMapper,
                                 DiseaseCapabilityRelMapper diseaseCapabilityRelMapper) {
        this.diseaseMasterMapper = diseaseMasterMapper;
        this.diseaseCapabilityRelMapper = diseaseCapabilityRelMapper;
    }

    public List<DiseaseRecord> findApprovedDiseases() {
        return diseaseMasterMapper.selectList(new QueryWrapper<DiseaseMasterEntity>()
                        .eq("deleted", 0)
                        .eq("review_status", "approved"))
                .stream()
                .map(this::toDiseaseRecord)
                .toList();
    }

    public List<DiseaseCapabilityRelationRecord> findRelationsByDiseaseCodes(List<String> diseaseCodes) {
        if (diseaseCodes == null || diseaseCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return diseaseCapabilityRelMapper.selectList(new QueryWrapper<DiseaseCapabilityRelEntity>()
                        .in("disease_code", diseaseCodes)
                        .orderByDesc("priority_score"))
                .stream()
                .map(this::toRelationRecord)
                .toList();
    }

    private DiseaseRecord toDiseaseRecord(DiseaseMasterEntity entity) {
        return new DiseaseRecord(
                entity.diseaseCode,
                entity.diseaseName,
                entity.aliasesJson,
                entity.symptomKeywords,
                entity.genderRule,
                entity.ageMin,
                entity.ageMax,
                entity.ageGroup,
                entity.urgencyLevel,
                entity.reviewStatus
        );
    }

    private DiseaseCapabilityRelationRecord toRelationRecord(DiseaseCapabilityRelEntity entity) {
        return new DiseaseCapabilityRelationRecord(
                entity.diseaseCode,
                entity.capabilityCode,
                entity.relType,
                entity.priorityScore == null ? 0D : entity.priorityScore.doubleValue(),
                entity.crowdConstraint,
                entity.note
        );
    }
}
