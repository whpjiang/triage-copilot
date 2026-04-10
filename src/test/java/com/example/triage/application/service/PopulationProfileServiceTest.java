package com.example.triage.application.service;

import com.example.triage.application.dto.TriageAssessRequest;
import com.example.triage.domain.population.AgeGroup;
import com.example.triage.domain.population.PopulationProfile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PopulationProfileServiceTest {

    private final PopulationProfileService service = new PopulationProfileService();

    @Test
    void shouldResolveAgeGroups() {
        assertThat(service.resolveAgeGroup(11)).isEqualTo(AgeGroup.CHILD);
        assertThat(service.resolveAgeGroup(12)).isEqualTo(AgeGroup.ADOLESCENT);
        assertThat(service.resolveAgeGroup(18)).isEqualTo(AgeGroup.ADULT);
        assertThat(service.resolveAgeGroup(65)).isEqualTo(AgeGroup.ELDERLY);
    }

    @Test
    void shouldBuildCrowdTagsForTransplantAndElderly() {
        TriageAssessRequest request = new TriageAssessRequest();
        request.setAge(70);
        request.setGender("male");
        request.setSymptoms("移植术后复查");

        PopulationProfile profile = service.buildProfile(request);

        assertThat(profile.gender()).isEqualTo("male");
        assertThat(profile.ageGroup()).isEqualTo(AgeGroup.ELDERLY);
        assertThat(profile.crowdTags()).contains("elderly", "transplant_followup");
    }

    @Test
    void shouldBuildAdolescentCrowdTag() {
        TriageAssessRequest request = new TriageAssessRequest();
        request.setAge(15);
        request.setGender("female");
        request.setSymptoms("发热咳嗽");

        PopulationProfile profile = service.buildProfile(request);

        assertThat(profile.ageGroup()).isEqualTo(AgeGroup.ADOLESCENT);
        assertThat(profile.crowdTags()).contains("adolescent");
    }
}
