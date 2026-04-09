package com.example.triage.integration;

import com.example.triagecopilot.TriageCopilotApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TriageCopilotApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BaseDataControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldImportDiseaseCsvAndExposeCheckSummary() throws Exception {
        String csv = """
                disease_name,aliases,symptom_keywords,gender_rule,age_min,age_max,age_group,urgency_level,standard_dept_hint
                测试疾病,测试别名,头痛|头晕,all,18,65,adult,medium,神经内科
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
}
