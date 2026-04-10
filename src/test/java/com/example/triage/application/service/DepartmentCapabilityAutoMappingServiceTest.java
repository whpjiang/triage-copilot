package com.example.triage.application.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentCapabilityAutoMappingServiceTest {

    private final DepartmentCapabilityAutoMappingService service = new DepartmentCapabilityAutoMappingService();

    @Test
    void shouldSuggestSpineCapabilities() {
        var suggestions = service.suggest("脊柱疼痛门诊", "骨科", "腰腿痛|腰椎间盘突出", "聚焦脊柱退变");

        assertThat(suggestions).extracting(DepartmentCapabilityAutoMappingService.CapabilityMappingSuggestion::capabilityCode)
                .contains("cap_orthopedics", "cap_spine_surgery", "cap_spine_pain_clinic");
    }

    @Test
    void shouldSuggestGynecologyCapability() {
        var suggestions = service.suggest("妇科门诊", "妇产科", "女性下腹痛|盆腔炎", "女性专科门诊");

        assertThat(suggestions).extracting(DepartmentCapabilityAutoMappingService.CapabilityMappingSuggestion::capabilityCode)
                .containsExactly("cap_gynecology");
    }
}
