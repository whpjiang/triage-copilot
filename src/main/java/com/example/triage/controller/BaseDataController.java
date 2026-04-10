package com.example.triage.controller;

import com.example.triage.application.dto.BaseDataCheckResponse;
import com.example.triage.application.dto.BaseDataImportResponse;
import com.example.triage.application.dto.BaseDataJobDetailResponse;
import com.example.triage.application.dto.BaseDataJobListResponse;
import com.example.triage.application.dto.BaseDataReviewResponse;
import com.example.triage.application.dto.BaseDataReviewResolveRequest;
import com.example.triage.application.dto.BaseDataReviewResolveResponse;
import com.example.triage.application.dto.BaseDataTemplateResponse;
import com.example.triage.application.service.BaseDataCheckService;
import com.example.triage.application.service.BaseDataImportService;
import com.example.triage.application.service.BaseDataJobService;
import com.example.triage.application.service.BaseDataReviewService;
import com.example.triage.application.service.BaseDataReviewResolveService;
import com.example.triage.application.service.BaseDataTemplateService;
import com.example.triagecopilot.common.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/base-data")
public class BaseDataController {

    private final BaseDataImportService baseDataImportService;
    private final BaseDataCheckService baseDataCheckService;
    private final BaseDataJobService baseDataJobService;
    private final BaseDataTemplateService baseDataTemplateService;
    private final BaseDataReviewService baseDataReviewService;
    private final BaseDataReviewResolveService baseDataReviewResolveService;

    public BaseDataController(BaseDataImportService baseDataImportService,
                              BaseDataCheckService baseDataCheckService,
                              BaseDataJobService baseDataJobService,
                              BaseDataTemplateService baseDataTemplateService,
                              BaseDataReviewService baseDataReviewService,
                              BaseDataReviewResolveService baseDataReviewResolveService) {
        this.baseDataImportService = baseDataImportService;
        this.baseDataCheckService = baseDataCheckService;
        this.baseDataJobService = baseDataJobService;
        this.baseDataTemplateService = baseDataTemplateService;
        this.baseDataReviewService = baseDataReviewService;
        this.baseDataReviewResolveService = baseDataReviewResolveService;
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

    @GetMapping("/jobs")
    public ApiResponse<BaseDataJobListResponse> listJobs(@RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 100));
        return ApiResponse.success(baseDataJobService.listJobs(normalizedLimit));
    }

    @GetMapping("/jobs/detail")
    public ApiResponse<BaseDataJobDetailResponse> getJobDetail(@RequestParam("jobId") Long jobId,
                                                               @RequestParam(value = "failureLimit", defaultValue = "20") Integer failureLimit) {
        int normalizedLimit = Math.max(1, Math.min(failureLimit, 100));
        return ApiResponse.success(baseDataJobService.getJobDetail(jobId, normalizedLimit));
    }

    @GetMapping("/template")
    public ApiResponse<BaseDataTemplateResponse> getTemplate(@RequestParam("datasetType") String datasetType) {
        return ApiResponse.success(baseDataTemplateService.getTemplate(datasetType));
    }

    @GetMapping("/reviews")
    public ApiResponse<BaseDataReviewResponse> listReviews(@RequestParam(value = "datasetType", required = false) String datasetType,
                                                           @RequestParam(value = "jobId", required = false) Long jobId,
                                                           @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 100));
        return ApiResponse.success(baseDataReviewService.listPendingReviews(datasetType, jobId, normalizedLimit));
    }

    @PostMapping("/reviews/resolve")
    public ApiResponse<BaseDataReviewResolveResponse> resolveReview(@RequestBody BaseDataReviewResolveRequest request) {
        return ApiResponse.success(baseDataReviewResolveService.resolve(request));
    }
}
