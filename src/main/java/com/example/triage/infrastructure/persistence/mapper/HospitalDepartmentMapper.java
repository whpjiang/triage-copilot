package com.example.triage.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.triage.infrastructure.persistence.entity.HospitalDepartmentEntity;
import com.example.triage.infrastructure.persistence.model.DepartmentSearchRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HospitalDepartmentMapper extends BaseMapper<HospitalDepartmentEntity> {

    @Select({
            "<script>",
            "select hd.id as departmentId,",
            "       h.id as hospitalId,",
            "       h.hospital_name as hospitalName,",
            "       hd.department_name as departmentName,",
            "       hd.parent_department_name as parentDepartmentName,",
            "       coalesce(hd.district_name, h.district_name) as districtName,",
            "       coalesce(hd.authority_score, h.authority_score, 0) as authorityScore,",
            "       hd.standard_dept_code as standardDeptCode,",
            "       hd.subspecialty_code as subspecialtyCode,",
            "       hd.is_emergency as isEmergency",
            "from hospital_department hd",
            "join hospital h on h.id = hd.hospital_id and h.deleted = 0 and h.active_status = 1",
            "where hd.deleted = 0 and hd.active_status = 1",
            "<if test='city != null and city != \"\"'>and h.city = #{city}</if>",
            "<if test='area != null and area != \"\"'>and coalesce(hd.district_name, h.district_name) = #{area}</if>",
            "<if test='query != null and query != \"\"'>",
            "  and (hd.department_name like concat('%', #{query}, '%')",
            "       or hd.parent_department_name like concat('%', #{query}, '%')",
            "       or hd.service_scope like concat('%', #{query}, '%')",
            "       or hd.department_intro like concat('%', #{query}, '%'))",
            "</if>",
            "order by coalesce(hd.authority_score, h.authority_score, 0) desc, hd.id asc",
            "</script>"
    })
    List<DepartmentSearchRecord> selectDepartmentsByQuery(@Param("city") String city,
                                                          @Param("area") String area,
                                                          @Param("query") String query);
}
