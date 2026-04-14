package com.example.triage.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.triage.infrastructure.persistence.entity.DoctorProfileEntity;
import com.example.triage.infrastructure.persistence.model.DoctorRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DoctorProfileMapper extends BaseMapper<DoctorProfileEntity> {

    @Select({
            "<script>",
            "select d.id as doctorId,",
            "       d.hospital_id as hospitalId,",
            "       d.department_id as departmentId,",
            "       d.doctor_name as doctorName,",
            "       d.title as title,",
            "       d.specialty_text as specialtyText,",
            "       d.gender_rule as genderRule,",
            "       d.age_min as ageMin,",
            "       d.age_max as ageMax,",
            "       d.crowd_tags_json as crowdTagsJson,",
            "       rel.capability_code as capabilityCode,",
            "       rel.weight as weight,",
            "       h.hospital_name as hospitalName,",
            "       hd.department_name as departmentName",
            "from doctor_profile d",
            "left join doctor_capability_rel rel on rel.doctor_id = d.id",
            "join hospital h on h.id = d.hospital_id and h.deleted = 0 and h.active_status = 1",
            "join hospital_department hd on hd.id = d.department_id and hd.deleted = 0 and hd.active_status = 1",
            "where d.active_status = 1 and d.department_id in",
            "<foreach collection='departmentIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"
    })
    List<DoctorRecord> selectDoctorsByDepartmentIds(@Param("departmentIds") List<Long> departmentIds);
}
