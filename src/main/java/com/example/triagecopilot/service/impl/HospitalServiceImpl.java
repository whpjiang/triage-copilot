package com.example.triagecopilot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.triagecopilot.entity.Hospital;
import com.example.triagecopilot.mapper.HospitalMapper;
import com.example.triagecopilot.service.HospitalService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HospitalServiceImpl extends ServiceImpl<HospitalMapper, Hospital> implements HospitalService {

    @Override
    public IPage<Hospital> pageQuery(Page<Hospital> page, String keyword) {
        LambdaQueryWrapper<Hospital> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Hospital::getIsActive, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Hospital::getNameNorm, keyword)                    .or()
                    .like(Hospital::getNameRaw, keyword)                    .or()
                    .like(Hospital::getHospitalCode, keyword)                    .or()
                    .like(Hospital::getCity, keyword)            );
        }
        wrapper.orderByDesc(Hospital::getUpdatedAt);
        return page(page, wrapper);
    }
}
