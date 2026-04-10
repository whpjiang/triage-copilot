package com.example.triage.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentCapabilityAutoMappingServiceTest {

    private final DepartmentCapabilityAutoMappingService service =
            new DepartmentCapabilityAutoMappingService(
                    new CapabilityAliasDictionaryService(new DiseaseNormalizeService(new ObjectMapper()))
            );

    @Test
    void shouldEvaluateSpineDepartmentWithReview() {
        var evaluation = service.evaluate("脊柱疼痛门诊", "骨科", "腰腿痛/腰椎间盘突出", "聚焦脊柱退变");

        assertThat(evaluation.suggestions())
                .extracting(DepartmentCapabilityAutoMappingService.CapabilityMappingSuggestion::capabilityCode)
                .contains("cap_orthopedics", "cap_spine_surgery", "cap_spine_pain_clinic");
        assertThat(evaluation.reviewIssues()).contains("AUTO_MAPPING_NEEDS_REVIEW");
        assertThat(evaluation.evidenceSummary()).isNotEmpty();
    }

    @Test
    void shouldEvaluateSingleGynecologyCapability() {
        var evaluation = service.evaluate("妇科门诊", "妇产科", "女性下腹痛|盆腔炎", "女性专科门诊");

        assertThat(evaluation.suggestions())
                .extracting(DepartmentCapabilityAutoMappingService.CapabilityMappingSuggestion::capabilityCode)
                .containsExactly("cap_gynecology");
        assertThat(evaluation.reviewIssues()).isEmpty();
    }
}
