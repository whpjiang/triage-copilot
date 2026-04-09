package com.example.triagecopilot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("doctor")
public class Doctor {

    @TableId(value = "doctor_id", type = IdType.AUTO)
    private Long doctorId;

    @TableField("doctor_code")
    private String doctorCode;

    @TableField("hospital_id")
    private Long hospitalId;

    @TableField("service_unit_id")
    private Long serviceUnitId;

    @TableField("name_raw")
    private String nameRaw;

    @TableField("name_norm")
    private String nameNorm;

    private String title;

    @TableField("intro_text")
    private String introText;

    @TableField("specialty_text")
    private String specialtyText;

    @TableField("crowd_limit")
    private String crowdLimit;

    @TableField("is_active")
    private Integer isActive;

    @TableField("source_platform")
    private String sourcePlatform;

    @TableField("source_url")
    private String sourceUrl;

    @TableField("crawl_time")
    private LocalDateTime crawlTime;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
    public String getDoctorCode() {
        return doctorCode;
    }

    public void setDoctorCode(String doctorCode) {
        this.doctorCode = doctorCode;
    }
    public Long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }
    public Long getServiceUnitId() {
        return serviceUnitId;
    }

    public void setServiceUnitId(Long serviceUnitId) {
        this.serviceUnitId = serviceUnitId;
    }
    public String getNameRaw() {
        return nameRaw;
    }

    public void setNameRaw(String nameRaw) {
        this.nameRaw = nameRaw;
    }
    public String getNameNorm() {
        return nameNorm;
    }

    public void setNameNorm(String nameNorm) {
        this.nameNorm = nameNorm;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getIntroText() {
        return introText;
    }

    public void setIntroText(String introText) {
        this.introText = introText;
    }
    public String getSpecialtyText() {
        return specialtyText;
    }

    public void setSpecialtyText(String specialtyText) {
        this.specialtyText = specialtyText;
    }
    public String getCrowdLimit() {
        return crowdLimit;
    }

    public void setCrowdLimit(String crowdLimit) {
        this.crowdLimit = crowdLimit;
    }
    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }
    public String getSourcePlatform() {
        return sourcePlatform;
    }

    public void setSourcePlatform(String sourcePlatform) {
        this.sourcePlatform = sourcePlatform;
    }
    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
    public LocalDateTime getCrawlTime() {
        return crawlTime;
    }

    public void setCrawlTime(LocalDateTime crawlTime) {
        this.crawlTime = crawlTime;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
