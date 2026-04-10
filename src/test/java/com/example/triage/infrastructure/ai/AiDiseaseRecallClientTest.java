package com.example.triage.infrastructure.ai;

import com.example.triage.application.service.DiseaseNormalizeService;
import com.example.triage.domain.population.AgeGroup;
import com.example.triage.domain.population.PopulationProfile;
import com.example.triage.infrastructure.persistence.model.DiseaseRecord;
import com.example.triage.infrastructure.persistence.repository.AiRecallAuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiDiseaseRecallClientTest {

    @SuppressWarnings("unchecked")
    private final ObjectProvider<ChatModel> provider = mock(ObjectProvider.class);
    private final AiRecallAuditRepository auditRepository = mock(AiRecallAuditRepository.class);
    private final AiDiseaseRecallClient client = new AiDiseaseRecallClient(
            provider,
            new ObjectMapper(),
            new DiseaseNormalizeService(new ObjectMapper()),
            auditRepository
    );

    @Test
    void shouldSkipHighRiskSymptoms() {
        PopulationProfile profile = new PopulationProfile("male", 45, AgeGroup.ADULT, List.of("adult"));
        List<DiseaseRecord> eligibleDiseases = List.of(new DiseaseRecord(
                "lumbar_disc_herniation",
                "腰椎间盘突出",
                "[]",
                "[\"腰痛\"]",
                "all",
                16,
                80,
                "adult",
                "medium",
                "approved"
        ));

        List<String> result = client.suggestDiseaseCodes("胸痛伴呼吸困难", profile, eligibleDiseases, List.of());

        assertThat(result).isEmpty();
        verify(auditRepository).save(eq("胸痛伴呼吸困难"), eq("male"), eq(45), eq("adult"), eq(1), any(), any(), eq("SKIPPED_HIGH_RISK"), any());
    }

    @Test
    void shouldSkipWhenModelUnavailable() {
        PopulationProfile profile = new PopulationProfile("female", 32, AgeGroup.ADULT, List.of("adult"));
        when(provider.getIfAvailable()).thenReturn(null);

        List<String> result = client.suggestDiseaseCodes(
                "腰痛伴下肢麻木",
                profile,
                List.of(new DiseaseRecord(
                        "lumbar_disc_herniation",
                        "腰椎间盘突出",
                        "[]",
                        "[\"腰痛\"]",
                        "all",
                        16,
                        80,
                        "adult",
                        "medium",
                        "approved"
                )),
                List.of()
        );

        assertThat(result).isEmpty();
        verify(auditRepository).save(eq("腰痛伴下肢麻木"), eq("female"), eq(32), eq("adult"), eq(1), any(), any(), eq("SKIPPED_NO_MODEL"), any());
    }

    @Test
    void shouldParseJsonResponse() {
        List<String> result = client.parseDiseaseCodes("[\"lumbar_disc_herniation\",\"lumbar_disc_herniation\",\"post_transplant_followup\"]");

        assertThat(result).containsExactly("lumbar_disc_herniation", "post_transplant_followup");
    }
}
