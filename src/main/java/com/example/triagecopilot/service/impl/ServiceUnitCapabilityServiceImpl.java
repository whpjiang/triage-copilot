package com.example.triagecopilot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.triagecopilot.entity.ServiceUnitCapability;
import com.example.triagecopilot.mapper.ServiceUnitCapabilityMapper;
import com.example.triagecopilot.service.ServiceUnitCapabilityService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ServiceUnitCapabilityServiceImpl extends ServiceImpl<ServiceUnitCapabilityMapper, ServiceUnitCapability> implements ServiceUnitCapabilityService {

    @Override
    public IPage<ServiceUnitCapability> pageQuery(Page<ServiceUnitCapability> page, String keyword) {
        LambdaQueryWrapper<ServiceUnitCapability> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(ServiceUnitCapability::getCapabilityName, keyword)                    .or()
                    .like(ServiceUnitCapability::getCapabilityCode, keyword)                    .or()
                    .like(ServiceUnitCapability::getSource, keyword)            );
        }
        wrapper.orderByDesc(ServiceUnitCapability::getId);
        return page(page, wrapper);
    }
}
