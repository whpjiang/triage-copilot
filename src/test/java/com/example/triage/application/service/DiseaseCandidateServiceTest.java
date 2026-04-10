package com.example.triage.application.service;

import com.example.triage.domain.disease.DiseaseCandidate;
import com.example.triage.domain.population.AgeGroup;
import com.example.triage.domain.population.PopulationProfile;
import com.example.triage.infrastructure.ai.AiDiseaseRecallClient;
import com.example.triage.infrastructure.persistence.model.DiseaseRecord;
import com.example.triage.infrastructure.persistence.repository.DiseaseDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DiseaseCandidateServiceTest {

    private final DiseaseDataRepository diseaseDataRepository = mock(DiseaseDataRepository.class);
    private final AiDiseaseRecallClient aiDiseaseRecallClient = mock(AiDiseaseRecallClient.class);
    private final DiseaseNormalizeService diseaseNormalizeService = new DiseaseNormalizeService(new ObjectMapper());
    private final DiseaseCandidateService service =
            new DiseaseCandidateService(diseaseDataRepository, diseaseNormalizeService, aiDiseaseRecallClient);

    @Test
    void shouldSupplementCandidateByAiRecallWhenRulesMiss() {
        DiseaseRecord spineDisease = new DiseaseRecord(
                "lumbar_disc_herniation",
                "腰椎间盘突出",
                "[\"腰突\"]",
                "[\"腰痛\",\"下肢麻木\"]",
                "all",
                16,
                80,
                "adult",
                "medium",
                "approved"
        );
        when(diseaseDataRepository.findApprovedDiseases()).thenReturn(List.of(spineDisease));
        when(aiDiseaseRecallClient.suggestDiseaseCodes(eq("腰腿发麻，怀疑腰椎问题"), any(), anyList(), anyList()))
                .thenReturn(List.of("lumbar_disc_herniation"));

        PopulationProfile profile = new PopulationProfile("male", 42, AgeGroup.ADULT, List.of());
        List<DiseaseCandidate> candidates = service.identifyCandidates("腰腿发麻，怀疑腰椎问题", profile);

        assertThat(candidates).hasSize(1);
        assertThat(candidates.getFirst().diseaseCode()).isEqualTo("lumbar_disc_herniation");
        assertThat(candidates.getFirst().matchedKeywords()).contains("ai_supplement");
    }

    @Test
    void shouldIgnoreAiSuggestionThatFailsPopulationConstraint() {
        DiseaseRecord gyneDisease = new DiseaseRecord(
                "pelvic_inflammatory_disease",
                "盆腔炎",
                "[\"下腹痛\"]",
                "[\"下腹痛\",\"发热\"]",
                "female_only",
                14,
                60,
                "adult",
                "medium",
                "approved"
        );
        when(diseaseDataRepository.findApprovedDiseases()).thenReturn(List.of(gyneDisease));
        when(aiDiseaseRecallClient.suggestDiseaseCodes(eq("下腹疼痛不适"), any(), anyList(), anyList()))
                .thenReturn(List.of("pelvic_inflammatory_disease"));

        PopulationProfile profile = new PopulationProfile("male", 30, AgeGroup.ADULT, List.of());
        List<DiseaseCandidate> candidates = service.identifyCandidates("下腹疼痛不适", profile);

        assertThat(candidates).isEmpty();
    }
}
