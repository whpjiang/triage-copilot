package com.example.triagecopilot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.triagecopilot.entity.TriageRecord;
import com.example.triagecopilot.mapper.TriageRecordMapper;
import com.example.triagecopilot.service.TriageRecordService;
import org.springframework.util.StringUtils;

public class TriageRecordServiceImpl extends ServiceImpl<TriageRecordMapper, TriageRecord> implements TriageRecordService {

    @Override
    public TriageRecord createRecord(TriageRecord record) {
        save(record);
        return record;
    }

    @Override
    public IPage<TriageRecord> pageByPatientName(Page<TriageRecord> page, String patientName) {
        LambdaQueryWrapper<TriageRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(patientName), TriageRecord::getPatientName, patientName)
                .orderByDesc(TriageRecord::getCreateTime);
        return page(page, wrapper);
    }
}
