package com.example.triagecopilot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.triagecopilot.entity.CarePathway;

public interface CarePathwayService extends IService<CarePathway> {

    IPage<CarePathway> pageQuery(Page<CarePathway> page, String keyword);
}
