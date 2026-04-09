package com.example.triagecopilot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("hospital_campus")
public class HospitalCampus {

    @TableId(value = "campus_id", type = IdType.AUTO)
    private Long campusId;

    @TableField("hospital_id")
    private Long hospitalId;

    @TableField("campus_code")
    private String campusCode;

    @TableField("campus_name_raw")
    private String campusNameRaw;

    @TableField("campus_name_norm")
    private String campusNameNorm;

    @TableField("address_norm")
    private String addressNorm;

    private BigDecimal lat;

    private BigDecimal lng;

    @TableField("reg_entry_url")
    private String regEntryUrl;

    @TableField("reg_entry_phone")
    private String regEntryPhone;

    @TableField("is_active")
    private Integer isActive;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public Long getCampusId() {
        return campusId;
    }

    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }
    public Long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }
    public String getCampusCode() {
        return campusCode;
    }

    public void setCampusCode(String campusCode) {
        this.campusCode = campusCode;
    }
    public String getCampusNameRaw() {
        return campusNameRaw;
    }

    public void setCampusNameRaw(String campusNameRaw) {
        this.campusNameRaw = campusNameRaw;
    }
    public String getCampusNameNorm() {
        return campusNameNorm;
    }

    public void setCampusNameNorm(String campusNameNorm) {
        this.campusNameNorm = campusNameNorm;
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
