package com.example.triagecopilot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.triagecopilot.entity.Doctor;
import com.example.triagecopilot.mapper.DoctorMapper;
import com.example.triagecopilot.service.DoctorService;
import org.springframework.util.StringUtils;

public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    @Override
    public IPage<Doctor> pageQuery(Page<Doctor> page, String keyword) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getIsActive, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Doctor::getNameNorm, keyword)                    .or()
                    .like(Doctor::getNameRaw, keyword)                    .or()
                    .like(Doctor::getDoctorCode, keyword)                    .or()
                    .like(Doctor::getTitle, keyword)            );
        }
        wrapper.orderByDesc(Doctor::getUpdatedAt);
        return page(page, wrapper);
    }
}
