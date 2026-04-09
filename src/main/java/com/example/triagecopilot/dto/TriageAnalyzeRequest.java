package com.example.triagecopilot.dto;

import jakarta.validation.constraints.NotBlank;

public class TriageAnalyzeRequest {

    @NotBlank(message = "symptoms cannot be blank")
    private String symptoms;

    private Integer age;

    /**
     * male/female/unknown
     */
    private String gender;

    private String duration;

    private String city;

    /**
     * optional notes such as pregnancy, child, elderly, chronic disease
     */
    private String specialCondition;

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSpecialCondition() {
        return specialCondition;
    }

    public void setSpecialCondition(String specialCondition) {
        this.specialCondition = specialCondition;
    }
}
