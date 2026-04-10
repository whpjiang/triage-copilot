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
        assertThat(response.getPathwayTags()).contains("child_fever_pathway");
        assertThat(response.getCapabilityRecommendations()).extracting(TriageAssessResponse.CapabilityRecommendationDto::getCapabilityCode)
                .contains("cap_pediatrics", "cap_pediatric_fever_clinic");
        assertThat(response.getDepartmentRecommendations()).extracting(TriageAssessResponse.DepartmentRecommendationDto::getDepartmentName)
                .contains("儿科门诊", "儿童发热门诊");
        assertThat(response.getDoctorRecommendations()).extracting(TriageAssessResponse.DoctorRecommendationDto::getDoctorName)
                .contains("张小宁");
    }

    @Test
    void shouldRecommendGeriatricsForMemoryDecline() {
        TriageAssessRequest request = new TriageAssessRequest();
        request.setSymptoms("老年记忆下降，头晕乏力，反应变慢");
        request.setAge(72);
        request.setGender("female");

        TriageAssessResponse response = orchestrator.assess(request);

        assertThat(response.getPathwayTags()).contains("elderly_multisymptom_pathway");
        assertThat(response.getCapabilityRecommendations()).extracting(TriageAssessResponse.CapabilityRecommendationDto::getCapabilityCode)
                .contains("cap_geriatrics", "cap_memory_clinic");
        assertThat(response.getDepartmentRecommendations()).extracting(TriageAssessResponse.DepartmentRecommendationDto::getDepartmentName)
                .contains("老年病科", "记忆障碍门诊");
        assertThat(response.getDoctorRecommendations()).extracting(TriageAssessResponse.DoctorRecommendationDto::getDoctorName)
                .contains("周忆安");
    }

    @Test
    void shouldApplyGenderFilterForFemalePelvicPain() {
        TriageAssessRequest request = new TriageAssessRequest();
        request.setSymptoms("女性下腹痛伴发热");
        request.setAge(30);
        request.setGender("female");

        TriageAssessResponse response = orchestrator.assess(request);

        assertThat(response.getPathwayTags()).contains("female_pelvic_pathway");
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

        assertThat(spineResponse.getPathwayTags()).contains("spine_pathway");
        assertThat(spineResponse.getCapabilityRecommendations()).extracting(TriageAssessResponse.CapabilityRecommendationDto::getCapabilityCode)
                .contains("cap_spine_surgery", "cap_spine_pain_clinic");
        assertThat(spineResponse.getDepartmentRecommendations()).extracting(TriageAssessResponse.DepartmentRecommendationDto::getDepartmentName)
                .contains("脊柱外科门诊", "脊柱疼痛专病门诊");
        assertThat(spineResponse.getDoctorRecommendations()).extracting(TriageAssessResponse.DoctorRecommendationDto::getDoctorName)
                .contains("李脊衡");

        TriageAssessRequest transplant = new TriageAssessRequest();
        transplant.setSymptoms("移植术后异常复查");
        transplant.setAge(50);
        transplant.setGender("male");
        TriageAssessResponse transplantResponse = orchestrator.assess(transplant);

        assertThat(transplantResponse.getPathwayTags()).contains("transplant_followup");
        assertThat(transplantResponse.getCapabilityRecommendations()).extracting(TriageAssessResponse.CapabilityRecommendationDto::getCapabilityCode)
                .contains("cap_transplant_followup");
        assertThat(transplantResponse.getDepartmentRecommendations()).extracting(TriageAssessResponse.DepartmentRecommendationDto::getDepartmentName)
                .contains("器官移植随访门诊");
        assertThat(transplantResponse.getDoctorRecommendations()).extracting(TriageAssessResponse.DoctorRecommendationDto::getDoctorName)
                .contains("王移宁");
    }

    @Test
    void shouldRefineMaleUrinaryPathwayToSpecialClinic() {
        TriageAssessRequest request = new TriageAssessRequest();
        request.setSymptoms("男性尿频、夜尿增多伴排尿困难");
        request.setAge(58);
        request.setGender("male");

        TriageAssessResponse response = orchestrator.assess(request);

        assertThat(response.getPathwayTags()).contains("male_urinary_pathway");
        assertThat(response.getCapabilityRecommendations()).extracting(TriageAssessResponse.CapabilityRecommendationDto::getCapabilityCode)
                .contains("cap_andrology", "cap_male_urinary_clinic");
        assertThat(response.getDepartmentRecommendations()).extracting(TriageAssessResponse.DepartmentRecommendationDto::getDepartmentName)
                .contains("男科门诊", "男性排尿异常门诊");
        assertThat(response.getDoctorRecommendations()).extracting(TriageAssessResponse.DoctorRecommendationDto::getDoctorName)
                .contains("陈泌安");
    }
}
