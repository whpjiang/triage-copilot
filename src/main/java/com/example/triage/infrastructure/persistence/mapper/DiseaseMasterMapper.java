package com.example.triage.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.triage.infrastructure.persistence.entity.DiseaseMasterEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DiseaseMasterMapper extends BaseMapper<DiseaseMasterEntity> {
}
