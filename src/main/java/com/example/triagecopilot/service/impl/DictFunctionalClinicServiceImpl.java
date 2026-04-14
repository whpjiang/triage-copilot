package com.example.triagecopilot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.triagecopilot.entity.DictFunctionalClinic;
import com.example.triagecopilot.mapper.DictFunctionalClinicMapper;
import com.example.triagecopilot.service.DictFunctionalClinicService;
import org.springframework.util.StringUtils;

public class DictFunctionalClinicServiceImpl extends ServiceImpl<DictFunctionalClinicMapper, DictFunctionalClinic> implements DictFunctionalClinicService {

    @Override
    public IPage<DictFunctionalClinic> pageQuery(Page<DictFunctionalClinic> page, String keyword) {
        LambdaQueryWrapper<DictFunctionalClinic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictFunctionalClinic::getIsActive, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(DictFunctionalClinic::getName, keyword)                    .or()
                    .like(DictFunctionalClinic::getCode, keyword)            );
        }
        wrapper.orderByDesc(DictFunctionalClinic::getSortNo);
        return page(page, wrapper);
    }
}
