package com.example.triagecopilot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.triagecopilot.entity.DictStandardDept;

public interface DictStandardDeptService extends IService<DictStandardDept> {

    IPage<DictStandardDept> pageQuery(Page<DictStandardDept> page, String keyword);
}
