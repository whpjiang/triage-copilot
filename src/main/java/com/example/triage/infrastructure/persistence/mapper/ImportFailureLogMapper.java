package com.example.triage.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.triage.infrastructure.persistence.entity.ImportFailureLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImportFailureLogMapper extends BaseMapper<ImportFailureLogEntity> {
}
