package com.example.triagecopilot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.triagecopilot.entity.DictCrowdSpecialty;

public interface DictCrowdSpecialtyService extends IService<DictCrowdSpecialty> {

    IPage<DictCrowdSpecialty> pageQuery(Page<DictCrowdSpecialty> page, String keyword);
}
