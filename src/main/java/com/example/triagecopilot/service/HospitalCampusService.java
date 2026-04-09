package com.example.triagecopilot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.triagecopilot.entity.HospitalCampus;

public interface HospitalCampusService extends IService<HospitalCampus> {

    IPage<HospitalCampus> pageQuery(Page<HospitalCampus> page, String keyword);
}
