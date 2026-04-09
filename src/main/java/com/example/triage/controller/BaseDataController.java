package com.example.triage.controller;

import com.example.triage.application.dto.BaseDataCheckResponse;
import com.example.triage.application.dto.BaseDataImportResponse;
import com.example.triage.application.service.BaseDataCheckService;
import com.example.triage.application.service.BaseDataImportService;
import com.example.triagecopilot.common.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/base-data")
public class BaseDataController {

    private final BaseDataImportService baseDataImportService;
    private final BaseDataCheckService baseDataCheckService;

    public BaseDataController(BaseDataImportService baseDataImportService, BaseDataCheckService baseDataCheckService) {
        this.baseDataImportService = baseDataImportService;
        this.baseDataCheckService = baseDataCheckService;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<BaseDataImportResponse> importBaseData(@RequestParam("datasetType") String datasetType,
                                                              @RequestParam("file") MultipartFile file) throws Exception {
        return ApiResponse.success(baseDataImportService.importData(datasetType, file));
    }

    @GetMapping("/check")
    public ApiResponse<BaseDataCheckResponse> checkBaseData() {
        return ApiResponse.success(baseDataCheckService.check());
    }
}
