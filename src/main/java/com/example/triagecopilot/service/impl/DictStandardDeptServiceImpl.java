package com.example.triagecopilot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.triagecopilot.entity.DictStandardDept;
import com.example.triagecopilot.mapper.DictStandardDeptMapper;
import com.example.triagecopilot.service.DictStandardDeptService;
import org.springframework.util.StringUtils;

public class DictStandardDeptServiceImpl extends ServiceImpl<DictStandardDeptMapper, DictStandardDept> implements DictStandardDeptService {

    @Override
    public IPage<DictStandardDept> pageQuery(Page<DictStandardDept> page, String keyword) {
        LambdaQueryWrapper<DictStandardDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictStandardDept::getIsActive, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(DictStandardDept::getName, keyword)                    .or()
                    .like(DictStandardDept::getCode, keyword)                    .or()
                    .like(DictStandardDept::getParentCode, keyword)            );
        }
        wrapper.orderByDesc(DictStandardDept::getSortNo);
        return page(page, wrapper);
    }
}
