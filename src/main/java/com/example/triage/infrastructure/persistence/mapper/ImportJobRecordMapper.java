package com.example.triage.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.triage.infrastructure.persistence.entity.ImportJobRecordEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImportJobRecordMapper extends BaseMapper<ImportJobRecordEntity> {
}
