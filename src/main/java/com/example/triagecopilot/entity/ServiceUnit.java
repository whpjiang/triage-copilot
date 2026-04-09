package com.example.triagecopilot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("service_unit")
public class ServiceUnit {

    @TableId(value = "service_unit_id", type = IdType.AUTO)
    private Long serviceUnitId;

    @TableField("service_unit_code")
    private String serviceUnitCode;

    @TableField("hospital_id")
    private Long hospitalId;

    @TableField("campus_id")
    private Long campusId;

    @TableField("name_raw")
    private String nameRaw;

    @TableField("name_norm")
    private String nameNorm;

    @TableField("unit_type")
    private String unitType;

    @TableField("outpatient_flag")
    private String outpatientFlag;

    @TableField("intro_text")
    private String introText;

    @TableField("reg_entry_url")
    private String regEntryUrl;

    @TableField("reg_entry_phone")
    private String regEntryPhone;

    @TableField("source_platform")
    private String sourcePlatform;

    @TableField("source_url")
    private String sourceUrl;

    @TableField("crawl_time")
    private LocalDateTime crawlTime;

    @TableField("is_active")
    private Integer isActive;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public Long getServiceUnitId() {
        return serviceUnitId;
    }

    public void setServiceUnitId(Long serviceUnitId) {
        this.serviceUnitId = serviceUnitId;
    }
    public String getServiceUnitCode() {
        return serviceUnitCode;
    }

    public void setServiceUnitCode(String serviceUnitCode) {
        this.serviceUnitCode = serviceUnitCode;
    }
    public Long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }
    public Long getCampusId() {
        return campusId;
    }

    public void setCampusId(Long campusId) {
        this.campusId = campusId;
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
    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }
    public String getOutpatientFlag() {
        return outpatientFlag;
    }

    public void setOutpatientFlag(String outpatientFlag) {
        this.outpatientFlag = outpatientFlag;
    }
    public String getIntroText() {
        return introText;
    }

    public void setIntroText(String introText) {
        this.introText = introText;
    }
    public String getRegEntryUrl() {
        return regEntryUrl;
    }

    public void setRegEntryUrl(String regEntryUrl) {
        this.regEntryUrl = regEntryUrl;
    }
    public String getRegEntryPhone() {
        return regEntryPhone;
    }

    public void setRegEntryPhone(String regEntryPhone) {
        this.regEntryPhone = regEntryPhone;
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
    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
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
