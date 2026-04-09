package com.example.triagecopilot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("hospital")
public class Hospital {

    @TableId(value = "hospital_id", type = IdType.AUTO)
    private Long hospitalId;

    @TableField("hospital_code")
    private String hospitalCode;

    @TableField("name_raw")
    private String nameRaw;

    @TableField("name_norm")
    private String nameNorm;

    @TableField("hospital_level")
    private String hospitalLevel;

    @TableField("hospital_type")
    private String hospitalType;

    private String province;

    private String city;

    private String district;

    @TableField("address_norm")
    private String addressNorm;

    private BigDecimal lat;

    private BigDecimal lng;

    @TableField("has_emergency")
    private Integer hasEmergency;

    @TableField("has_fever_clinic")
    private Integer hasFeverClinic;

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

    public Long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }
    public String getHospitalCode() {
        return hospitalCode;
    }

    public void setHospitalCode(String hospitalCode) {
        this.hospitalCode = hospitalCode;
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
    public String getHospitalLevel() {
        return hospitalLevel;
    }

    public void setHospitalLevel(String hospitalLevel) {
        this.hospitalLevel = hospitalLevel;
    }
    public String getHospitalType() {
        return hospitalType;
    }

    public void setHospitalType(String hospitalType) {
        this.hospitalType = hospitalType;
    }
    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
    public String getAddressNorm() {
        return addressNorm;
    }

    public void setAddressNorm(String addressNorm) {
        this.addressNorm = addressNorm;
    }
    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }
    public BigDecimal getLng() {
        return lng;
    }

    public void setLng(BigDecimal lng) {
        this.lng = lng;
    }
    public Integer getHasEmergency() {
        return hasEmergency;
    }

    public void setHasEmergency(Integer hasEmergency) {
        this.hasEmergency = hasEmergency;
    }
    public Integer getHasFeverClinic() {
        return hasFeverClinic;
    }

    public void setHasFeverClinic(Integer hasFeverClinic) {
        this.hasFeverClinic = hasFeverClinic;
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
