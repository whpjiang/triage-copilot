package com.example.triage.integration;

import com.example.triagecopilot.TriageCopilotApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;

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
    void shouldImportWuhanDiseaseWorkbookSheetAndAutoMapHint() throws Exception {
        MockMultipartFile file = workbookFile("wuhan-disease.xlsx", workbook -> {
            createSheet(workbook, "README", new String[]{"说明"}, new String[]{"placeholder"});
            createSheet(workbook, "Import_Disease",
                    new String[]{"rt_disease_id", "disease_name", "disease_code", "aliases", "symptom_keywords", "gender_rule", "age_min", "age_max", "age_group", "urgency_level", "standard_dept_hint"},
                    new String[]{"1", "女性下腹痛", "female_lower_abdominal_pain", "盆腔炎/下腹痛", "下腹痛,白带异常,发热", "female", "14", "60", "adult", "medium", "妇科"});
        });

        mockMvc.perform(multipart("/api/base-data/import")
                        .file(file)
                        .param("datasetType", "wuhan_disease")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failureCount").value(0))
                .andExpect(jsonPath("$.data.reviewCount").value(0));

        mockMvc.perform(get("/api/base-data/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.diseaseCapabilityMappedCount").isNumber());
    }

    @Test
    void shouldCreateDiseaseCapabilityReviewsForMissingAndAmbiguousHints() throws Exception {
        String csv = """
                disease_name,disease_code,aliases,symptom_keywords,gender_rule,age_min,age_max,standard_dept_hint
                疾病A,disease_a,别名A,发热,all,0,17,
                疾病B,disease_b,别名B,腰痛,all,18,80,骨科/脊柱外科
                """;
        MockMultipartFile file = new MockMultipartFile("file", "disease.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/base-data/import")
                        .file(file)
                        .param("datasetType", "wuhan_disease")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewTypeDistribution.MISSING_STANDARD_DEPT_HINT").value(1))
                .andExpect(jsonPath("$.data.reviewTypeDistribution.DISEASE_CAPABILITY_MULTI_MATCH").value(1));
    }

    @Test
    void shouldImportWuhanDepartmentWorkbookSheetAndExposeReviewEvidence() throws Exception {
        MockMultipartFile file = workbookFile("wuhan-department.xlsx", workbook -> {
            createSheet(workbook, "README", new String[]{"说明"}, new String[]{"placeholder"});
            createSheet(workbook, "Summary", new String[]{"说明"}, new String[]{"placeholder"});
            createSheet(workbook, "Import_Department",
                    new String[]{"rt_hospital_id", "hospital_name", "city", "rt_dept_id", "department_name", "parent_department_name", "department_intro", "service_scope", "gender_rule", "age_min", "age_max", "crowd_tags"},
                    new String[]{"10008", "武汉大学人民医院", "武汉", "20001", "脊柱疼痛门诊", "骨科", "聚焦脊柱退变", "腰腿痛,腰椎间盘突出", "", "", "", ""},
                    new String[]{"10009", "医院_10009", "武汉", "20002", "综合评估门诊", "内科", "疑似待确认医院", "慢病管理", "", "", "", ""});
        });

        String importResponse = mockMvc.perform(multipart("/api/base-data/import")
                        .file(file)
                        .param("datasetType", "wuhan_department")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(2))
                .andExpect(jsonPath("$.data.autoMappedCount").value(3))
                .andExpect(jsonPath("$.data.reviewTypeDistribution.AUTO_MAPPING_NEEDS_REVIEW").value(1))
                .andExpect(jsonPath("$.data.reviewTypeDistribution.HOSPITAL_NAME_NEEDS_CONFIRM").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long jobId = objectMapper.readTree(importResponse).path("data").path("jobId").asLong();

        mockMvc.perform(get("/api/base-data/reviews")
                        .param("jobId", String.valueOf(jobId))
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].issueType").isString())
                .andExpect(jsonPath("$.data.items[*].matchedCapabilityCodes").isArray())
                .andExpect(jsonPath("$.data.items[*].reviewEvidence").exists());

        mockMvc.perform(get("/api/base-data/jobs/detail")
                        .param("jobId", String.valueOf(jobId))
                        .param("failureLimit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.job.autoMappedCount").value(3))
                .andExpect(jsonPath("$.data.reviewTypeDistribution.HOSPITAL_NAME_NEEDS_CONFIRM").value(1))
                .andExpect(jsonPath("$.data.recentReviews[0].reviewId").isNumber());
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
                        .param("limit", "10"))
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
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[*].id", not(hasItem((int) reviewId))));
    }

    private MockMultipartFile workbookFile(String filename, WorkbookConsumer consumer) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            consumer.accept(workbook);
            workbook.write(outputStream);
            return new MockMultipartFile("file", filename, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", outputStream.toByteArray());
        }
    }

    private void createSheet(XSSFWorkbook workbook, String name, String[] headers, String[]... rows) {
        XSSFSheet sheet = workbook.createSheet(name);
        var headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
            var row = sheet.createRow(rowIndex + 1);
            String[] values = rows[rowIndex];
            for (int cellIndex = 0; cellIndex < values.length; cellIndex++) {
                row.createCell(cellIndex).setCellValue(values[cellIndex]);
            }
        }
    }

    @FunctionalInterface
    private interface WorkbookConsumer {
        void accept(XSSFWorkbook workbook) throws Exception;
    }
}
