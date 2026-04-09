package com.example.triagecopilot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.triagecopilot.entity.Hospital;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HospitalMapper extends BaseMapper<Hospital> {
}
