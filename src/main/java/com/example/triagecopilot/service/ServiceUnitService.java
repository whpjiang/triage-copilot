package com.example.triagecopilot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.triagecopilot.entity.ServiceUnit;

public interface ServiceUnitService extends IService<ServiceUnit> {

    IPage<ServiceUnit> pageQuery(Page<ServiceUnit> page, String keyword);
}
