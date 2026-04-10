package com.example.triage.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiseaseNormalizeServiceTest {

    private final DiseaseNormalizeService service = new DiseaseNormalizeService(new ObjectMapper());

    @Test
    void shouldNormalizeAliasJsonAndDelimitedText() {
        assertThat(service.parseList("[\"腰腿痛\",\"腰痛\"]")).containsExactly("腰腿痛", "腰痛");
        assertThat(service.parseList("发热, 咳嗽；流涕/咽痛\n鼻塞")).contains("发热", "咳嗽", "流涕", "咽痛", "鼻塞");
    }

    @Test
    void shouldFilterEmptyAndOverlongKeywords() {
        assertThat(service.normalizeKeywords("发热||咳嗽| |\n"
                + "这是一个明显过长的症状描述字段用于测试是否会被过滤掉因为它长度超过限制"))
                .contains("发热", "咳嗽")
                .doesNotContain("");
    }

    @Test
    void shouldApplyGenderAndAgeConstraints() {
        assertThat(service.matchesGenderAndAge("female_only", 14, 60, "female", 28)).isTrue();
        assertThat(service.matchesGenderAndAge("female_only", 14, 60, "male", 28)).isFalse();
        assertThat(service.matchesGenderAndAge("all", 60, 120, "male", 40)).isFalse();
    }
}
