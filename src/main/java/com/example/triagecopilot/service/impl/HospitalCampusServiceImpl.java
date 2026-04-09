package com.example.triagecopilot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.triagecopilot.entity.HospitalCampus;
import com.example.triagecopilot.mapper.HospitalCampusMapper;
import com.example.triagecopilot.service.HospitalCampusService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HospitalCampusServiceImpl extends ServiceImpl<HospitalCampusMapper, HospitalCampus> implements HospitalCampusService {

    @Override
    public IPage<HospitalCampus> pageQuery(Page<HospitalCampus> page, String keyword) {
        LambdaQueryWrapper<HospitalCampus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HospitalCampus::getIsActive, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(HospitalCampus::getCampusNameNorm, keyword)                    .or()
                    .like(HospitalCampus::getCampusNameRaw, keyword)                    .or()
                    .like(HospitalCampus::getCampusCode, keyword)            );
        }
        wrapper.orderByDesc(HospitalCampus::getUpdatedAt);
        return page(page, wrapper);
    }
}
