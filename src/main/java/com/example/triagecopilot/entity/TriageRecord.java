package com.example.triagecopilot.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("triage_record")
public class TriageRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String patientName;

    private Integer patientAge;

    /**
     * 1-male, 2-female, 0-unknown
     */
    private Integer patientGender;

    private String symptoms;

    private String symptomDuration;

    private String recommendedDepartment;

    /**
     * 1-emergency, 2-normal, 3-low
     */
    private Integer urgencyLevel;

    private String aiAdvice;

    /**
     * 0-pending, 1-processed
     */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public Integer getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(Integer patientAge) {
        this.patientAge = patientAge;
    }

    public Integer getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(Integer patientGender) {
        this.patientGender = patientGender;
    }

    public String getSymptomDuration() {
        return symptomDuration;
    }

    public void setSymptomDuration(String symptomDuration) {
        this.symptomDuration = symptomDuration;
    }

    public String getRecommendedDepartment() {
        return recommendedDepartment;
    }

    public void setRecommendedDepartment(String recommendedDepartment) {
        this.recommendedDepartment = recommendedDepartment;
    }

    public Integer getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(Integer urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getAiAdvice() {
        return aiAdvice;
    }

    public void setAiAdvice(String aiAdvice) {
        this.aiAdvice = aiAdvice;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
