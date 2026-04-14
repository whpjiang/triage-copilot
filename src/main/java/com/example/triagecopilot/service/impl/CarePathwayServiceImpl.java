package com.example.triagecopilot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.triagecopilot.entity.CarePathway;
import com.example.triagecopilot.mapper.CarePathwayMapper;
import com.example.triagecopilot.service.CarePathwayService;
import org.springframework.util.StringUtils;

public class CarePathwayServiceImpl extends ServiceImpl<CarePathwayMapper, CarePathway> implements CarePathwayService {

    @Override
    public IPage<CarePathway> pageQuery(Page<CarePathway> page, String keyword) {
        LambdaQueryWrapper<CarePathway> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarePathway::getIsActive, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(CarePathway::getTitle, keyword)                    .or()
                    .like(CarePathway::getStage, keyword)                    .or()
                    .like(CarePathway::getCrowdCode, keyword)            );
        }
        wrapper.orderByDesc(CarePathway::getUpdatedAt);
        return page(page, wrapper);
    }
}
