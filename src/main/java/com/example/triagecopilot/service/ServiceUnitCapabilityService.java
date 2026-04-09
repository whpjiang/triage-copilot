package com.example.triagecopilot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.triagecopilot.entity.ServiceUnitCapability;

public interface ServiceUnitCapabilityService extends IService<ServiceUnitCapability> {

    IPage<ServiceUnitCapability> pageQuery(Page<ServiceUnitCapability> page, String keyword);
}
