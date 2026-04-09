package com.example.triage.application.dto;

import java.util.ArrayList;
import java.util.List;

public class TriageAssessResponse {

    private PopulationProfileDto populationProfile;
    private List<String> pathwayTags = new ArrayList<>();
    private List<DiseaseCandidateDto> candidateDiseases = new ArrayList<>();
    private List<CapabilityRecommendationDto> capabilityRecommendations = new ArrayList<>();
    private List<DepartmentRecommendationDto> departmentRecommendations = new ArrayList<>();
    private String explanation;

    public PopulationProfileDto getPopulationProfile() {
        return populationProfile;
    }

    public void setPopulationProfile(PopulationProfileDto populationProfile) {
        this.populationProfile = populationProfile;
    }

    public List<DiseaseCandidateDto> getCandidateDiseases() {
        return candidateDiseases;
    }

    public void setCandidateDiseases(List<DiseaseCandidateDto> candidateDiseases) {
        this.candidateDiseases = candidateDiseases;
    }

    public List<String> getPathwayTags() {
        return pathwayTags;
    }

    public void setPathwayTags(List<String> pathwayTags) {
        this.pathwayTags = pathwayTags;
    }

    public List<CapabilityRecommendationDto> getCapabilityRecommendations() {
        return capabilityRecommendations;
    }

    public void setCapabilityRecommendations(List<CapabilityRecommendationDto> capabilityRecommendations) {
        this.capabilityRecommendations = capabilityRecommendations;
    }

    public List<DepartmentRecommendationDto> getDepartmentRecommendations() {
        return departmentRecommendations;
    }

    public void setDepartmentRecommendations(List<DepartmentRecommendationDto> departmentRecommendations) {
        this.departmentRecommendations = departmentRecommendations;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public static class PopulationProfileDto {
        private String gender;
        private Integer age;
        private String ageGroup;
        private List<String> crowdTags = new ArrayList<>();

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getAgeGroup() {
            return ageGroup;
        }

        public void setAgeGroup(String ageGroup) {
            this.ageGroup = ageGroup;
        }

        public List<String> getCrowdTags() {
            return crowdTags;
        }

        public void setCrowdTags(List<String> crowdTags) {
            this.crowdTags = crowdTags;
        }
    }

    public static class DiseaseCandidateDto {
        private String diseaseCode;
        private String diseaseName;
        private List<String> matchedKeywords = new ArrayList<>();
        private String urgencyLevel;
        private Double score;

        public String getDiseaseCode() {
            return diseaseCode;
        }

        public void setDiseaseCode(String diseaseCode) {
            this.diseaseCode = diseaseCode;
        }

        public String getDiseaseName() {
            return diseaseName;
        }

        public void setDiseaseName(String diseaseName) {
            this.diseaseName = diseaseName;
        }

        public List<String> getMatchedKeywords() {
            return matchedKeywords;
        }

        public void setMatchedKeywords(List<String> matchedKeywords) {
            this.matchedKeywords = matchedKeywords;
        }

        public String getUrgencyLevel() {
            return urgencyLevel;
        }

        public void setUrgencyLevel(String urgencyLevel) {
            this.urgencyLevel = urgencyLevel;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }
    }

    public static class CapabilityRecommendationDto {
        private String capabilityCode;
        private String capabilityName;
        private String capabilityType;
        private String standardDeptCode;
        private List<String> matchedDiseases = new ArrayList<>();
        private Double score;

        public String getCapabilityCode() {
            return capabilityCode;
        }

        public void setCapabilityCode(String capabilityCode) {
            this.capabilityCode = capabilityCode;
        }

        public String getCapabilityName() {
            return capabilityName;
        }

        public void setCapabilityName(String capabilityName) {
            this.capabilityName = capabilityName;
        }

        public String getCapabilityType() {
            return capabilityType;
        }

        public void setCapabilityType(String capabilityType) {
            this.capabilityType = capabilityType;
        }

        public String getStandardDeptCode() {
            return standardDeptCode;
        }

        public void setStandardDeptCode(String standardDeptCode) {
            this.standardDeptCode = standardDeptCode;
        }

        public List<String> getMatchedDiseases() {
            return matchedDiseases;
        }

        public void setMatchedDiseases(List<String> matchedDiseases) {
            this.matchedDiseases = matchedDiseases;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }
    }

    public static class DepartmentRecommendationDto {
        private Long departmentId;
        private String hospitalName;
        private String departmentName;
        private String parentDepartmentName;
        private String capabilityCode;
        private String supportLevel;
        private Double score;

        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public String getHospitalName() {
            return hospitalName;
        }

        public void setHospitalName(String hospitalName) {
            this.hospitalName = hospitalName;
        }

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }

        public String getParentDepartmentName() {
            return parentDepartmentName;
        }

        public void setParentDepartmentName(String parentDepartmentName) {
            this.parentDepartmentName = parentDepartmentName;
        }

        public String getCapabilityCode() {
            return capabilityCode;
        }

        public void setCapabilityCode(String capabilityCode) {
            this.capabilityCode = capabilityCode;
        }

        public String getSupportLevel() {
            return supportLevel;
        }

        public void setSupportLevel(String supportLevel) {
            this.supportLevel = supportLevel;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }
    }
}
