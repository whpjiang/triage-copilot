package com.example.triagecopilot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.triagecopilot.entity.DictCrowdSpecialty;
import com.example.triagecopilot.mapper.DictCrowdSpecialtyMapper;
import com.example.triagecopilot.service.DictCrowdSpecialtyService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DictCrowdSpecialtyServiceImpl extends ServiceImpl<DictCrowdSpecialtyMapper, DictCrowdSpecialty> implements DictCrowdSpecialtyService {

    @Override
    public IPage<DictCrowdSpecialty> pageQuery(Page<DictCrowdSpecialty> page, String keyword) {
        LambdaQueryWrapper<DictCrowdSpecialty> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictCrowdSpecialty::getIsActive, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(DictCrowdSpecialty::getName, keyword)                    .or()
                    .like(DictCrowdSpecialty::getCode, keyword)            );
        }
        wrapper.orderByDesc(DictCrowdSpecialty::getSortNo);
        return page(page, wrapper);
    }
}
