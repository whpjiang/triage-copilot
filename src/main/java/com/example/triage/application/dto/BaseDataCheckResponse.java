package com.example.triage.application.dto;

import java.util.ArrayList;
import java.util.List;

public class BaseDataCheckResponse {

    private Integer diseaseCount;
    private Integer capabilityCount;
    private Integer hospitalCount;
    private Integer departmentCount;
    private Integer relationCount;
    private Integer pendingReviewCount;
    private List<String> warnings = new ArrayList<>();

    public Integer getDiseaseCount() {
        return diseaseCount;
    }

    public void setDiseaseCount(Integer diseaseCount) {
        this.diseaseCount = diseaseCount;
    }

    public Integer getCapabilityCount() {
        return capabilityCount;
    }

    public void setCapabilityCount(Integer capabilityCount) {
        this.capabilityCount = capabilityCount;
    }

    public Integer getHospitalCount() {
        return hospitalCount;
    }

    public void setHospitalCount(Integer hospitalCount) {
        this.hospitalCount = hospitalCount;
    }

    public Integer getDepartmentCount() {
        return departmentCount;
    }

    public void setDepartmentCount(Integer departmentCount) {
        this.departmentCount = departmentCount;
    }

    public Integer getRelationCount() {
        return relationCount;
    }

    public void setRelationCount(Integer relationCount) {
        this.relationCount = relationCount;
    }

    public Integer getPendingReviewCount() {
        return pendingReviewCount;
    }

    public void setPendingReviewCount(Integer pendingReviewCount) {
        this.pendingReviewCount = pendingReviewCount;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
