package com.example.triagecopilot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.triagecopilot.entity.DictFunctionalClinic;

public interface DictFunctionalClinicService extends IService<DictFunctionalClinic> {

    IPage<DictFunctionalClinic> pageQuery(Page<DictFunctionalClinic> page, String keyword);
}
