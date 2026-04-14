package com.example.triage.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.triage.infrastructure.persistence.entity.DepartmentCapabilityRelEntity;
import com.example.triage.infrastructure.persistence.model.DepartmentMappingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DepartmentCapabilityRelMapper extends BaseMapper<DepartmentCapabilityRelEntity> {

    @Select({
            "<script>",
            "select hd.id as departmentId,",
            "       h.id as hospitalId,",
            "       h.hospital_name as hospitalName,",
            "       hd.department_name as departmentName,",
            "       hd.parent_department_name as parentDepartmentName,",
            "       hd.department_intro as departmentIntro,",
            "       hd.service_scope as serviceScope,",
            "       hd.gender_rule as genderRule,",
            "       hd.age_min as ageMin,",
            "       hd.age_max as ageMax,",
            "       hd.crowd_tags_json as crowdTagsJson,",
            "       rel.capability_code as capabilityCode,",
            "       rel.support_level as supportLevel,",
            "       rel.weight as weight,",
            "       rel.source as source,",
            "       coalesce(hd.district_name, h.district_name) as districtName,",
            "       coalesce(hd.latitude, h.latitude) as latitude,",
            "       coalesce(hd.longitude, h.longitude) as longitude,",
            "       hd.standard_dept_code as standardDeptCode,",
            "       hd.subspecialty_code as subspecialtyCode,",
            "       hd.is_emergency as isEmergency,",
            "       coalesce(hd.authority_score, h.authority_score, 0) as authorityScore,",
            "       h.hospital_level as hospitalLevel",
            "from department_capability_rel rel",
            "join hospital_department hd on hd.id = rel.department_id and hd.deleted = 0 and hd.active_status = 1",
            "join hospital h on h.id = hd.hospital_id and h.deleted = 0 and h.active_status = 1",
            "where rel.capability_code in",
            "<foreach collection='capabilityCodes' item='code' open='(' separator=',' close=')'>#{code}</foreach>",
            "<if test='city != null and city != \"\"'>and h.city = #{city}</if>",
            "order by rel.weight desc, hd.id asc",
            "</script>"
    })
    List<DepartmentMappingRecord> selectDepartmentMappings(@Param("capabilityCodes") List<String> capabilityCodes,
                                                           @Param("city") String city);
}
