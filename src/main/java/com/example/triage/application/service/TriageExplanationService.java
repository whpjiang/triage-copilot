package com.example.triage.application.service;

import com.example.triage.domain.capability.CapabilityRecommendation;
import com.example.triage.domain.disease.DiseaseCandidate;
import com.example.triage.domain.hospital.DepartmentRecommendation;
import com.example.triage.domain.hospital.DoctorRecommendation;
import com.example.triage.domain.population.PopulationProfile;
import com.example.triage.infrastructure.ai.AiExplanationClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TriageExplanationService {

    private final AiExplanationClient aiExplanationClient;

    public TriageExplanationService(AiExplanationClient aiExplanationClient) {
        this.aiExplanationClient = aiExplanationClient;
    }

    public String explain(PopulationProfile profile,
                          List<String> pathwayTags,
                          List<DiseaseCandidate> diseases,
                          List<CapabilityRecommendation> capabilities,
                          List<DepartmentRecommendation> departments,
                          List<DoctorRecommendation> doctors) {
        String structured = """
                Structured triage completed:
                - Population profile: gender=%s, age=%s, ageGroup=%s, tags=%s
                - Pathway tags: %s
                - Candidate diseases: %s
                - Capability recommendations: %s
                - Department recommendations: %s
                - Doctor recommendations: %s
                The result is constrained by age, gender, special population tags, and pathway tags before mapping capabilities to local departments.
                Seek urgent in-person care if symptoms become rapidly worse or severe pain, persistent high fever, or altered consciousness appears.
                """.formatted(
                profile.gender(),
                profile.age(),
                profile.ageGroup().name().toLowerCase(),
                String.join(", ", profile.crowdTags()),
                pathwayTags.isEmpty() ? "general_pathway" : String.join(", ", pathwayTags),
                diseases.stream().map(d -> d.diseaseName() + "(" + round2(d.score()) + ")").collect(Collectors.joining("; ")),
                capabilities.stream().map(c -> c.capabilityName() + "(" + round2(c.score()) + ")").collect(Collectors.joining("; ")),
                departments.stream().map(d -> d.hospitalName() + "-" + d.departmentName()).collect(Collectors.joining("; ")),
                doctors.stream().map(d -> d.doctorName() + "-" + d.title()).collect(Collectors.joining("; "))
        );
        return aiExplanationClient.polishExplanation(structured);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
