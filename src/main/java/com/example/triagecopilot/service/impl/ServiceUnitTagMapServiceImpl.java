package com.example.triagecopilot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.triagecopilot.entity.ServiceUnitTagMap;
import com.example.triagecopilot.mapper.ServiceUnitTagMapMapper;
import com.example.triagecopilot.service.ServiceUnitTagMapService;
import org.springframework.util.StringUtils;

public class ServiceUnitTagMapServiceImpl extends ServiceImpl<ServiceUnitTagMapMapper, ServiceUnitTagMap> implements ServiceUnitTagMapService {

    @Override
    public IPage<ServiceUnitTagMap> pageQuery(Page<ServiceUnitTagMap> page, String keyword) {
        LambdaQueryWrapper<ServiceUnitTagMap> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(ServiceUnitTagMap::getTagCode, keyword)                    .or()
                    .like(ServiceUnitTagMap::getTagType, keyword)                    .or()
                    .like(ServiceUnitTagMap::getSource, keyword)            );
        }
        wrapper.orderByDesc(ServiceUnitTagMap::getUpdatedAt);
        return page(page, wrapper);
    }
}
