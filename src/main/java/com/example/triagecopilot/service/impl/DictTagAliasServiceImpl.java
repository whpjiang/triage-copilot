package com.example.triagecopilot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.triagecopilot.entity.DictTagAlias;
import com.example.triagecopilot.mapper.DictTagAliasMapper;
import com.example.triagecopilot.service.DictTagAliasService;
import org.springframework.util.StringUtils;

public class DictTagAliasServiceImpl extends ServiceImpl<DictTagAliasMapper, DictTagAlias> implements DictTagAliasService {

    @Override
    public IPage<DictTagAlias> pageQuery(Page<DictTagAlias> page, String keyword) {
        LambdaQueryWrapper<DictTagAlias> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictTagAlias::getIsActive, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(DictTagAlias::getAliasText, keyword)                    .or()
                    .like(DictTagAlias::getTagCode, keyword)                    .or()
                    .like(DictTagAlias::getTagType, keyword)            );
        }
        wrapper.orderByDesc(DictTagAlias::getId);
        return page(page, wrapper);
    }
}
