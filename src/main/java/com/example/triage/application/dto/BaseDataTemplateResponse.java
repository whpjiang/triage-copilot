package com.example.triage.application.dto;

import java.util.ArrayList;
import java.util.List;

public class BaseDataTemplateResponse {

    private String datasetType;
    private String fileName;
    private List<String> requiredFields = new ArrayList<>();
    private List<String> optionalFields = new ArrayList<>();
    private String csvTemplate;

    public String getDatasetType() {
        return datasetType;
    }

    public void setDatasetType(String datasetType) {
        this.datasetType = datasetType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getRequiredFields() {
        return requiredFields;
    }

    public void setRequiredFields(List<String> requiredFields) {
        this.requiredFields = requiredFields;
    }

    public List<String> getOptionalFields() {
        return optionalFields;
    }

    public void setOptionalFields(List<String> optionalFields) {
        this.optionalFields = optionalFields;
    }

    public String getCsvTemplate() {
        return csvTemplate;
    }

    public void setCsvTemplate(String csvTemplate) {
        this.csvTemplate = csvTemplate;
    }
}
