package com.example.triage.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiseaseNormalizeServiceTest {

    private final DiseaseNormalizeService service = new DiseaseNormalizeService(new ObjectMapper());

    @Test
    void shouldNormalizeAliasJsonAndDelimitedText() {
        assertThat(service.parseList("[\"腰腿痛\",\"腰痛\"]")).containsExactly("腰腿痛", "腰痛");
        assertThat(service.parseList("发热, 咳嗽；流涕")).contains("发热", "咳嗽", "流涕");
    }

    @Test
    void shouldApplyGenderAndAgeConstraints() {
        assertThat(service.matchesGenderAndAge("female_only", 14, 60, "female", 28)).isTrue();
        assertThat(service.matchesGenderAndAge("female_only", 14, 60, "male", 28)).isFalse();
        assertThat(service.matchesGenderAndAge("all", 60, 120, "male", 40)).isFalse();
    }
}
