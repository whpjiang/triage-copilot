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
        assertThat(service.resolveAgeGroup(8)).isEqualTo(AgeGroup.CHILD);
        assertThat(service.resolveAgeGroup(15)).isEqualTo(AgeGroup.ADOLESCENT);
        assertThat(service.resolveAgeGroup(46)).isEqualTo(AgeGroup.ADULT);
        assertThat(service.resolveAgeGroup(72)).isEqualTo(AgeGroup.ELDERLY);
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
}
