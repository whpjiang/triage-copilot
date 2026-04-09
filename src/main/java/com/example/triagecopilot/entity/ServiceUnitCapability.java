package com.example.triagecopilot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("service_unit_capability")
public class ServiceUnitCapability {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("service_unit_id")
    private Long serviceUnitId;

    @TableField("capability_code")
    private String capabilityCode;

    @TableField("capability_name")
    private String capabilityName;

    @TableField("support_level")
    private String supportLevel;

    private BigDecimal confidence;

    private String source;

    @TableField("created_at")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getServiceUnitId() {
        return serviceUnitId;
    }

    public void setServiceUnitId(Long serviceUnitId) {
        this.serviceUnitId = serviceUnitId;
    }
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
    public String getSupportLevel() {
        return supportLevel;
    }

    public void setSupportLevel(String supportLevel) {
        this.supportLevel = supportLevel;
    }
    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
