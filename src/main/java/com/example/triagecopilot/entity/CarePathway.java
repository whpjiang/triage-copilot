package com.example.triagecopilot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("care_pathway")
public class CarePathway {

    @TableId(value = "pathway_id", type = IdType.AUTO)
    private Long pathwayId;

    @TableField("crowd_code")
    private String crowdCode;

    private String stage;

    private String title;

    @TableField("recommended_standard_codes")
    private String recommendedStandardCodes;

    @TableField("recommended_functional_codes")
    private String recommendedFunctionalCodes;

    @TableField("must_have_crowd_codes")
    private String mustHaveCrowdCodes;

    @TableField("prep_list")
    private String prepList;

    @TableField("red_flags")
    private String redFlags;

    @TableField("is_active")
    private Integer isActive;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public Long getPathwayId() {
        return pathwayId;
    }

    public void setPathwayId(Long pathwayId) {
        this.pathwayId = pathwayId;
    }
    public String getCrowdCode() {
        return crowdCode;
    }

    public void setCrowdCode(String crowdCode) {
        this.crowdCode = crowdCode;
    }
    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getRecommendedStandardCodes() {
        return recommendedStandardCodes;
    }

    public void setRecommendedStandardCodes(String recommendedStandardCodes) {
        this.recommendedStandardCodes = recommendedStandardCodes;
    }
    public String getRecommendedFunctionalCodes() {
        return recommendedFunctionalCodes;
    }

    public void setRecommendedFunctionalCodes(String recommendedFunctionalCodes) {
        this.recommendedFunctionalCodes = recommendedFunctionalCodes;
    }
    public String getMustHaveCrowdCodes() {
        return mustHaveCrowdCodes;
    }

    public void setMustHaveCrowdCodes(String mustHaveCrowdCodes) {
        this.mustHaveCrowdCodes = mustHaveCrowdCodes;
    }
    public String getPrepList() {
        return prepList;
    }

    public void setPrepList(String prepList) {
        this.prepList = prepList;
    }
    public String getRedFlags() {
        return redFlags;
    }

    public void setRedFlags(String redFlags) {
        this.redFlags = redFlags;
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
