package com.example.triage.application.dto;

import java.util.ArrayList;
import java.util.List;

public class BaseDataCheckResponse {

    private Integer diseaseCount;
    private Integer diseaseAliasCount;
    private Integer diseaseWithSymptomsCount;
    private Integer diseaseCapabilityMappedCount;
    private Integer capabilityCount;
    private Integer hospitalCount;
    private Integer departmentCount;
    private Integer relationCount;
    private Integer autoMappedDepartmentCount;
    private Integer pendingReviewCount;
    private Integer pendingDepartmentReviewCount;
    private Integer pendingDiseaseReviewCount;
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

    public Integer getDiseaseAliasCount() {
        return diseaseAliasCount;
    }

    public void setDiseaseAliasCount(Integer diseaseAliasCount) {
        this.diseaseAliasCount = diseaseAliasCount;
    }

    public Integer getDiseaseWithSymptomsCount() {
        return diseaseWithSymptomsCount;
    }

    public void setDiseaseWithSymptomsCount(Integer diseaseWithSymptomsCount) {
        this.diseaseWithSymptomsCount = diseaseWithSymptomsCount;
    }

    public Integer getDiseaseCapabilityMappedCount() {
        return diseaseCapabilityMappedCount;
    }

    public void setDiseaseCapabilityMappedCount(Integer diseaseCapabilityMappedCount) {
        this.diseaseCapabilityMappedCount = diseaseCapabilityMappedCount;
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

    public Integer getAutoMappedDepartmentCount() {
        return autoMappedDepartmentCount;
    }

    public void setAutoMappedDepartmentCount(Integer autoMappedDepartmentCount) {
        this.autoMappedDepartmentCount = autoMappedDepartmentCount;
    }

    public Integer getPendingReviewCount() {
        return pendingReviewCount;
    }

    public void setPendingReviewCount(Integer pendingReviewCount) {
        this.pendingReviewCount = pendingReviewCount;
    }

    public Integer getPendingDepartmentReviewCount() {
        return pendingDepartmentReviewCount;
    }

    public void setPendingDepartmentReviewCount(Integer pendingDepartmentReviewCount) {
        this.pendingDepartmentReviewCount = pendingDepartmentReviewCount;
    }

    public Integer getPendingDiseaseReviewCount() {
        return pendingDiseaseReviewCount;
    }

    public void setPendingDiseaseReviewCount(Integer pendingDiseaseReviewCount) {
        this.pendingDiseaseReviewCount = pendingDiseaseReviewCount;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
