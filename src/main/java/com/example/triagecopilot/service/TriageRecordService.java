package com.example.triagecopilot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.triagecopilot.entity.TriageRecord;

public interface TriageRecordService extends IService<TriageRecord> {

    TriageRecord createRecord(TriageRecord record);

    IPage<TriageRecord> pageByPatientName(Page<TriageRecord> page, String patientName);
}
