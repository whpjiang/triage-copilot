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
    void shouldImportDiseaseCsvAndExposeCheckSummary() throws Exception {
        String csv = """
                disease_name,aliases,symptom_keywords,gender_rule,age_min,age_max,age_group,urgency_level,standard_dept_hint
                test_disease,test_alias,headache|dizziness,all,18,65,adult,medium,neurology
                """;
        MockMultipartFile file = new MockMultipartFile("file", "disease.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/base-data/import")
                        .file(file)
                        .param("datasetType", "disease")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failureCount").value(0));

        mockMvc.perform(get("/api/base-data/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.diseaseCount").isNumber())
                .andExpect(jsonPath("$.data.capabilityCount").isNumber())
                .andExpect(jsonPath("$.data.pendingReviewCount").isNumber());
    }

    @Test
    void shouldExposeTemplateAndPendingReviews() throws Exception {
        String csv = """
                hospital_name,department_name,city,parent_department_name,department_intro,service_scope
                demo_hospital,import_review_clinic,shanghai,internal,import_intro,import_scope
                """;
        MockMultipartFile file = new MockMultipartFile("file", "department.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/base-data/import")
                        .file(file)
                        .param("datasetType", "department")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewCount").value(1));

        mockMvc.perform(get("/api/base-data/template").param("datasetType", "department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.datasetType").value("department"))
                .andExpect(jsonPath("$.data.requiredFields[0]").value("hospital_name"))
                .andExpect(jsonPath("$.data.csvTemplate").isString());

        mockMvc.perform(get("/api/base-data/reviews")
                        .param("datasetType", "department")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pendingCount").isNumber())
                .andExpect(jsonPath("$.data.items[0].datasetType").value("department"))
                .andExpect(jsonPath("$.data.items[0].issueType").value("WAIT_CAPABILITY_MAPPING"));
    }

    @Test
    void shouldResolvePendingReviewItem() throws Exception {
        String csv = """
                hospital_name,department_name,city,parent_department_name,department_intro,service_scope
                demo_hospital,resolve_review_clinic,shanghai,internal,import_intro,import_scope
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
                disease_name,aliases,symptom_keywords,gender_rule,age_min,age_max,age_group,urgency_level,standard_dept_hint
                valid_disease,valid_alias,headache|dizziness,all,18,65,adult,medium,neurology
                ,missing_name,nausea,all,18,65,adult,low,internal
                """;
        MockMultipartFile file = new MockMultipartFile("file", "job-detail.csv", "text/csv", csv.getBytes());

        String importResponse = mockMvc.perform(multipart("/api/base-data/import")
                        .file(file)
                        .param("datasetType", "disease")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failureCount").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long jobId = objectMapper.readTree(importResponse).path("data").path("jobId").asLong();

        mockMvc.perform(get("/api/base-data/jobs").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobs[0].jobId").isNumber())
                .andExpect(jsonPath("$.data.jobs[0].datasetType").isString());

        mockMvc.perform(get("/api/base-data/jobs/detail")
                        .param("jobId", String.valueOf(jobId))
                        .param("failureLimit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.job.jobId").value(jobId))
                .andExpect(jsonPath("$.data.job.failureCount").value(1))
                .andExpect(jsonPath("$.data.failures[0].rowNumber").isNumber())
                .andExpect(jsonPath("$.data.failures[0].errorMessage").value("missing field: disease_name"));
    }
}
