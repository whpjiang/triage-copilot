package com.example.triage.application.service;

import com.example.triage.application.dto.BaseDataCheckResponse;
import com.example.triage.infrastructure.persistence.repository.BaseDataAdminRepository;
import com.example.triage.infrastructure.persistence.repository.CapabilityDataRepository;
import com.example.triage.infrastructure.persistence.repository.HospitalDataRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BaseDataCheckService {

    private final BaseDataAdminRepository baseDataAdminRepository;
    private final CapabilityDataRepository capabilityDataRepository;
    private final HospitalDataRepository hospitalDataRepository;

    public BaseDataCheckService(BaseDataAdminRepository baseDataAdminRepository,
                                CapabilityDataRepository capabilityDataRepository,
                                HospitalDataRepository hospitalDataRepository) {
        this.baseDataAdminRepository = baseDataAdminRepository;
        this.capabilityDataRepository = capabilityDataRepository;
        this.hospitalDataRepository = hospitalDataRepository;
    }

    public BaseDataCheckResponse check() {
        Map<String, Integer> counts = baseDataAdminRepository.aggregateCounts();
        BaseDataCheckResponse response = new BaseDataCheckResponse();
        response.setDiseaseCount(counts.getOrDefault("diseases", 0));
        response.setCapabilityCount(capabilityDataRepository.countCapabilities());
        response.setHospitalCount(hospitalDataRepository.countHospitals());
        response.setDepartmentCount(hospitalDataRepository.countDepartments());
        response.setRelationCount(hospitalDataRepository.countDepartmentCapabilityRelations());
        response.setPendingReviewCount(counts.getOrDefault("pending", 0));
        List<String> warnings = new ArrayList<>();
        if (response.getDiseaseCount() == 0) {
            warnings.add("疾病主数据为空");
        }
        if (response.getCapabilityCount() == 0) {
            warnings.add("医学能力目录为空");
        }
        if (response.getPendingReviewCount() > 0) {
            warnings.add("存在待人工复核的数据导入项");
        }
        response.setWarnings(warnings);
        return response;
    }
}
