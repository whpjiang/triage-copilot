package com.example.triagecopilot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.triagecopilot.entity.ServiceUnitTagMap;

public interface ServiceUnitTagMapService extends IService<ServiceUnitTagMap> {

    IPage<ServiceUnitTagMap> pageQuery(Page<ServiceUnitTagMap> page, String keyword);
}
