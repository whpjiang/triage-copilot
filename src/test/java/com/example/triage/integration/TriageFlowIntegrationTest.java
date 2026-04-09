package com.example.triage.integration;

import com.example.triage.application.dto.TriageAssessRequest;
import com.example.triage.application.dto.TriageAssessResponse;
import com.example.triage.application.orchestrator.TriageDecisionOrchestrator;
import com.example.triagecopilot.TriageCopilotApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TriageCopilotApplication.class)
@ActiveProfiles("test")
class TriageFlowIntegrationTest {

    @Autowired
    private TriageDecisionOrchestrator orchestrator;

    @Test
    void shouldRecommendPediatricsForChildCough() {
        TriageAssessRequest request = new TriageAssessRequest();
        request.setSymptoms("儿童咳嗽发热两天");
        request.setAge(8);
        request.setGender("male");

        TriageAssessResponse response = orchestrator.assess(request);

        assertThat(response.getPopulationProfile().getAgeGroup()).isEqualTo("child");
        assertThat(response.getCapabilityRecommendations()).extracting(TriageAssessResponse.CapabilityRecommendationDto::getCapabilityCode)
                .contains("cap_pediatrics");
        assertThat(response.getDepartmentRecommendations()).extracting(TriageAssessResponse.DepartmentRecommendationDto::getDepartmentName)
                .contains("儿科门诊");
    }

    @Test
    void shouldRecommendGeriatricsForMemoryDecline() {
        TriageAssessRequest request = new TriageAssessRequest();
        request.setSymptoms("老年记忆下降，反应变慢");
        request.setAge(72);
        request.setGender("female");

        TriageAssessResponse response = orchestrator.assess(request);

        assertThat(response.getCapabilityRecommendations()).extracting(TriageAssessResponse.CapabilityRecommendationDto::getCapabilityCode)
                .contains("cap_geriatrics");
        assertThat(response.getDepartmentRecommendations()).extracting(TriageAssessResponse.DepartmentRecommendationDto::getDepartmentName)
                .contains("老年病科");
    }

    @Test
    void shouldApplyGenderFilterForFemalePelvicPain() {
        TriageAssessRequest request = new TriageAssessRequest();
        request.setSymptoms("女性下腹痛伴发热");
        request.setAge(30);
        request.setGender("female");

        TriageAssessResponse response = orchestrator.assess(request);

        assertThat(response.getCapabilityRecommendations()).extracting(TriageAssessResponse.CapabilityRecommendationDto::getCapabilityCode)
                .contains("cap_gynecology")
                .doesNotContain("cap_andrology");
        assertThat(response.getDepartmentRecommendations()).extracting(TriageAssessResponse.DepartmentRecommendationDto::getDepartmentName)
                .contains("妇科门诊")
                .doesNotContain("男科门诊");
    }

    @Test
    void shouldRecommendSpineAndTransplantPathways() {
        TriageAssessRequest spine = new TriageAssessRequest();
        spine.setSymptoms("腰腿痛伴右下肢麻木两周");
        spine.setAge(46);
        spine.setGender("male");
        TriageAssessResponse spineResponse = orchestrator.assess(spine);
        assertThat(spineResponse.getCapabilityRecommendations()).extracting(TriageAssessResponse.CapabilityRecommendationDto::getCapabilityCode)
                .contains("cap_spine_surgery");
        assertThat(spineResponse.getDepartmentRecommendations()).extracting(TriageAssessResponse.DepartmentRecommendationDto::getDepartmentName)
                .contains("脊柱外科门诊");

        TriageAssessRequest transplant = new TriageAssessRequest();
        transplant.setSymptoms("移植术后异常复查");
        transplant.setAge(50);
        transplant.setGender("male");
        TriageAssessResponse transplantResponse = orchestrator.assess(transplant);
        assertThat(transplantResponse.getCapabilityRecommendations()).extracting(TriageAssessResponse.CapabilityRecommendationDto::getCapabilityCode)
                .contains("cap_transplant_followup");
        assertThat(transplantResponse.getDepartmentRecommendations()).extracting(TriageAssessResponse.DepartmentRecommendationDto::getDepartmentName)
                .contains("器官移植随访门诊");
    }
}
