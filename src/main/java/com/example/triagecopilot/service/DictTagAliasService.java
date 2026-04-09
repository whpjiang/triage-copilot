package com.example.triagecopilot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.triagecopilot.entity.DictTagAlias;

public interface DictTagAliasService extends IService<DictTagAlias> {

    IPage<DictTagAlias> pageQuery(Page<DictTagAlias> page, String keyword);
}
