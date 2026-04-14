package com.example.triage.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.triage.infrastructure.persistence.entity.TriageSessionEntity;
import com.example.triage.infrastructure.persistence.entity.TriageSlotStateEntity;
import com.example.triage.infrastructure.persistence.entity.TriageTurnEntity;
import com.example.triage.infrastructure.persistence.mapper.TriageSessionMapper;
import com.example.triage.infrastructure.persistence.mapper.TriageSlotStateMapper;
import com.example.triage.infrastructure.persistence.mapper.TriageTurnMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TriageSessionRepository {

    private final TriageSessionMapper triageSessionMapper;
    private final TriageTurnMapper triageTurnMapper;
    private final TriageSlotStateMapper triageSlotStateMapper;

    public TriageSessionRepository(TriageSessionMapper triageSessionMapper,
                                   TriageTurnMapper triageTurnMapper,
                                   TriageSlotStateMapper triageSlotStateMapper) {
        this.triageSessionMapper = triageSessionMapper;
        this.triageTurnMapper = triageTurnMapper;
        this.triageSlotStateMapper = triageSlotStateMapper;
    }

    public TriageSessionEntity findSession(String sessionId) {
        return triageSessionMapper.selectOne(new QueryWrapper<TriageSessionEntity>()
                .eq("session_id", sessionId)
                .last("limit 1"));
    }

    public void saveSession(TriageSessionEntity entity) {
        if (entity.id == null) {
            triageSessionMapper.insert(entity);
            return;
        }
        triageSessionMapper.updateById(entity);
    }

    public TriageSlotStateEntity findSlotState(String sessionId) {
        return triageSlotStateMapper.selectOne(new QueryWrapper<TriageSlotStateEntity>()
                .eq("session_id", sessionId)
                .last("limit 1"));
    }

    public void saveSlotState(TriageSlotStateEntity entity) {
        if (entity.id == null) {
            triageSlotStateMapper.insert(entity);
            return;
        }
        triageSlotStateMapper.updateById(entity);
    }

    public int nextTurnNo(String sessionId) {
        Long count = triageTurnMapper.selectCount(new QueryWrapper<TriageTurnEntity>()
                .eq("session_id", sessionId));
        return count == null ? 1 : count.intValue() + 1;
    }

    public void appendTurn(TriageTurnEntity entity) {
        triageTurnMapper.insert(entity);
    }
}
