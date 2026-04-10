package com.example.triage.integration;

import com.example.triagecopilot.TriageCopilotApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TriageCopilotApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BaseDataControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldImportWuhanDiseaseWithChineseHeaders() throws Exception {
        String csv = """
                疾病名称,别名,症状关键词,适用性别,年龄范围,紧急程度,建议科室
                女性下腹痛,盆腔炎|盆腔感染,下腹痛|白带异常|发热,女,14-60岁,medium,妇科
                """;
        MockMultipartFile file = new MockMultipartFile("file", "wuhan-disease.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/base-data/import")
                        .file(file)
                        .param("datasetType", "wuhan_disease")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failureCount").value(0))
                .andExpect(jsonPath("$.data.reviewCount").value(0));
    }

    @Test
    void shouldExposeWuhanTemplateAndCreatePendingReviewForUnmappedDepartment() throws Exception {
        String csv = """
                医院名称,科室名称,所属城市,父级科室,科室简介,诊疗范围
                武汉样例医院,综合评估门诊,武汉,内科,需要人工识别的结构化导入,复杂慢病管理
                """;
        MockMultipartFile file = new MockMultipartFile("file", "wuhan-department.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/base-data/import")
                        .file(file)
                        .param("datasetType", "wuhan_department")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewCount").value(1))
                .andExpect(jsonPath("$.data.reviewTypeDistribution.WAIT_CAPABILITY_MAPPING").value(1));

        mockMvc.perform(get("/api/base-data/template").param("datasetType", "wuhan_department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.datasetType").value("wuhan_department"))
                .andExpect(jsonPath("$.data.requiredFields[0]").value("医院名称"))
                .andExpect(jsonPath("$.data.csvTemplate").isString());

        mockMvc.perform(get("/api/base-data/reviews")
                        .param("datasetType", "department")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pendingCount").isNumber())
                .andExpect(jsonPath("$.data.issueTypeDistribution.WAIT_CAPABILITY_MAPPING").isNumber())
                .andExpect(jsonPath("$.data.items[0].issueType").value("WAIT_CAPABILITY_MAPPING"));
    }

    @Test
    void shouldAutoMapWuhanDepartmentImportAndExposeStats() throws Exception {
        String csv = """
                医院名称,科室名称,所属城市,父级科室,科室简介,诊疗范围
                武汉样例医院,妇科门诊,武汉,妇产科,女性专科门诊,女性下腹痛|盆腔炎
                """;
        MockMultipartFile file = new MockMultipartFile("file", "wuhan-auto-map.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/base-data/import")
                        .file(file)
                        .param("datasetType", "wuhan_department")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.autoMappedCount").value(1))
                .andExpect(jsonPath("$.data.reviewCount").value(0));

        mockMvc.perform(get("/api/base-data/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.relationCount").isNumber());
    }

    @Test
    void shouldResolvePendingReviewItem() throws Exception {
        String csv = """
                hospital_name,department_name,city,parent_department_name,department_intro,service_scope
                demo_hospital,resolve_review_clinic,example_city,internal,import_intro,import_scope
                """;
        MockMultipartFile file = new MockMultipartFile("file", "department.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/base-data/import")
                        .file(file)
                        .param("datasetType", "department")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        String reviewResponse = mockMvc.perform(get("/api/base-data/reviews")
                        .param("datasetType", "department")
                        .param("limit", "1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(reviewResponse);
        long reviewId = jsonNode.path("data").path("items").path(0).path("id").asLong();

        mockMvc.perform(post("/api/base-data/reviews/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reviewId": %s, "resolutionNote": "mapped manually"}
                                """.formatted(reviewId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewId").value(reviewId))
                .andExpect(jsonPath("$.data.resolved").value(true))
                .andExpect(jsonPath("$.data.pendingCount").isNumber());

        mockMvc.perform(get("/api/base-data/reviews")
                        .param("datasetType", "department")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[*].id", not(hasItem((int) reviewId))));
    }

    @Test
    void shouldExposeImportJobListAndFailureDetails() throws Exception {
        String csv = """
                疾病名称,别名,症状关键词,适用性别,年龄范围,紧急程度,建议科室
                valid_disease,valid_alias,headache|dizziness,all,18-65岁,medium,neurology
                ,missing_name,nausea,all,18-65岁,low,internal
                """;
        MockMultipartFile file = new MockMultipartFile("file", "job-detail.csv", "text/csv", csv.getBytes());

        String importResponse = mockMvc.perform(multipart("/api/base-data/import")
                        .file(file)
                        .param("datasetType", "wuhan_disease")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failureCount").value(1))
                .andExpect(jsonPath("$.data.commonIssueDistribution").isMap())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long jobId = objectMapper.readTree(importResponse).path("data").path("jobId").asLong();

        mockMvc.perform(get("/api/base-data/jobs").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobs[0].jobId").isNumber())
                .andExpect(jsonPath("$.data.jobs[0].autoMappedCount").isNumber());

        mockMvc.perform(get("/api/base-data/jobs/detail")
                        .param("jobId", String.valueOf(jobId))
                        .param("failureLimit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.job.jobId").value(jobId))
                .andExpect(jsonPath("$.data.job.failureCount").value(1))
                .andExpect(jsonPath("$.data.commonIssueDistribution").isMap())
                .andExpect(jsonPath("$.data.reviewTypeDistribution.MISSING_DISEASE_NAME").value(1))
                .andExpect(jsonPath("$.data.failures[0].rowNumber").isNumber());
    }
}
