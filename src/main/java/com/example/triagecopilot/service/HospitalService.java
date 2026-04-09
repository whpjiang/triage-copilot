package com.example.triagecopilot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.triagecopilot.entity.Hospital;

public interface HospitalService extends IService<Hospital> {

    IPage<Hospital> pageQuery(Page<Hospital> page, String keyword);
}
