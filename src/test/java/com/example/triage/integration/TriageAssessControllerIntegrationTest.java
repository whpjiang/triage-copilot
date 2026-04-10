package com.example.triage.integration;

import com.example.triagecopilot.TriageCopilotApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TriageCopilotApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TriageAssessControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAssessViaApi() throws Exception {
        mockMvc.perform(post("/api/triage/assess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "symptoms": "腰腿痛伴右下肢麻木两周",
                                  "gender": "male",
                                  "age": 46,
                                  "city": "示例城市"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.populationProfile.ageGroup").value("adult"))
                .andExpect(jsonPath("$.data.candidateDiseases[0].diseaseCode").isString())
                .andExpect(jsonPath("$.data.capabilityRecommendations[0].capabilityCode").isString())
                .andExpect(jsonPath("$.data.departmentRecommendations[0].departmentName").isString());
    }
}
