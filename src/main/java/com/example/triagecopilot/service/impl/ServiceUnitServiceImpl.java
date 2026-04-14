package com.example.triagecopilot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.triagecopilot.entity.ServiceUnit;
import com.example.triagecopilot.mapper.ServiceUnitMapper;
import com.example.triagecopilot.service.ServiceUnitService;
import org.springframework.util.StringUtils;

public class ServiceUnitServiceImpl extends ServiceImpl<ServiceUnitMapper, ServiceUnit> implements ServiceUnitService {

    @Override
    public IPage<ServiceUnit> pageQuery(Page<ServiceUnit> page, String keyword) {
        LambdaQueryWrapper<ServiceUnit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceUnit::getIsActive, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(ServiceUnit::getNameNorm, keyword)                    .or()
                    .like(ServiceUnit::getNameRaw, keyword)                    .or()
                    .like(ServiceUnit::getServiceUnitCode, keyword)            );
        }
        wrapper.orderByDesc(ServiceUnit::getUpdatedAt);
        return page(page, wrapper);
    }
}
