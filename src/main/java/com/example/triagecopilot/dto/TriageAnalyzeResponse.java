package com.example.triagecopilot.dto;

import java.util.ArrayList;
import java.util.List;

public class TriageAnalyzeResponse {

    private String recommendedDepartment;
    private String recommendedFunctionalClinic;
    private String urgencyLevel;
    private String urgencyReason;
    private String advice;
    private String disclaimer;
    private List<String> nextSteps = new ArrayList<>();
    private List<TriageOption> hospitalOptions = new ArrayList<>();
    private List<TriageOption> doctorOptions = new ArrayList<>();
    private DebugInfo debug;

    public String getRecommendedDepartment() {
        return recommendedDepartment;
    }

    public void setRecommendedDepartment(String recommendedDepartment) {
        this.recommendedDepartment = recommendedDepartment;
    }

    public String getRecommendedFunctionalClinic() {
        return recommendedFunctionalClinic;
    }

    public void setRecommendedFunctionalClinic(String recommendedFunctionalClinic) {
        this.recommendedFunctionalClinic = recommendedFunctionalClinic;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getUrgencyReason() {
        return urgencyReason;
    }

    public void setUrgencyReason(String urgencyReason) {
        this.urgencyReason = urgencyReason;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public List<String> getNextSteps() {
        return nextSteps;
    }

    public void setNextSteps(List<String> nextSteps) {
        this.nextSteps = nextSteps;
    }

    public List<TriageOption> getHospitalOptions() {
        return hospitalOptions;
    }

    public void setHospitalOptions(List<TriageOption> hospitalOptions) {
        this.hospitalOptions = hospitalOptions;
    }

    public List<TriageOption> getDoctorOptions() {
        return doctorOptions;
    }

    public void setDoctorOptions(List<TriageOption> doctorOptions) {
        this.doctorOptions = doctorOptions;
    }

    public DebugInfo getDebug() {
        return debug;
    }

    public void setDebug(DebugInfo debug) {
        this.debug = debug;
    }

    public static class TriageOption {

        private String id;
        private String name;
        private String subtitle;
        private String extra;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public String getExtra() {
            return extra;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }
    }

    public static class DebugInfo {

        private List<String> matchedKeywords = new ArrayList<>();
        private List<ScoreItem> standardDepartmentScores = new ArrayList<>();
        private List<ScoreItem> functionalClinicScores = new ArrayList<>();
        private List<ScoreItem> hospitalScores = new ArrayList<>();
        private List<ScoreItem> doctorScores = new ArrayList<>();

        public List<String> getMatchedKeywords() {
            return matchedKeywords;
        }

        public void setMatchedKeywords(List<String> matchedKeywords) {
            this.matchedKeywords = matchedKeywords;
        }

        public List<ScoreItem> getStandardDepartmentScores() {
            return standardDepartmentScores;
        }

        public void setStandardDepartmentScores(List<ScoreItem> standardDepartmentScores) {
            this.standardDepartmentScores = standardDepartmentScores;
        }

        public List<ScoreItem> getFunctionalClinicScores() {
            return functionalClinicScores;
        }

        public void setFunctionalClinicScores(List<ScoreItem> functionalClinicScores) {
            this.functionalClinicScores = functionalClinicScores;
        }

        public List<ScoreItem> getHospitalScores() {
            return hospitalScores;
        }

        public void setHospitalScores(List<ScoreItem> hospitalScores) {
            this.hospitalScores = hospitalScores;
        }

        public List<ScoreItem> getDoctorScores() {
            return doctorScores;
        }

        public void setDoctorScores(List<ScoreItem> doctorScores) {
            this.doctorScores = doctorScores;
        }
    }

    public static class ScoreItem {
        private String id;
        private String name;
        private Double score;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }
    }
}
