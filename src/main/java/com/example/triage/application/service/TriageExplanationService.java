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
                已按结构化链路完成初步分诊：
                - 人群画像：性别=%s，年龄=%s，年龄段=%s，标签=%s
                - 场景路径：%s
                - 疾病候选：%s
                - 医学能力：%s
                - 本地科室：%s
                - 医生推荐：%s
                以上结果先经过年龄、性别、特殊人群和路径标签约束，再将标准医学能力映射到本地真实科室。
                如出现进行性加重、高热不退、意识异常或剧烈疼痛，应及时线下急诊就医。
                """.formatted(
                profile.gender(),
                profile.age(),
                profile.ageGroup().name().toLowerCase(),
                String.join("、", profile.crowdTags()),
                pathwayTags.isEmpty() ? "general_pathway" : String.join("、", pathwayTags),
                diseases.stream().map(d -> d.diseaseName() + "(" + round2(d.score()) + ")").collect(Collectors.joining("；")),
                capabilities.stream().map(c -> c.capabilityName() + "(" + round2(c.score()) + ")").collect(Collectors.joining("；")),
                departments.stream().map(d -> d.hospitalName() + "-" + d.departmentName()).collect(Collectors.joining("；")),
                doctors.stream().map(d -> d.doctorName() + "-" + d.title()).collect(Collectors.joining("；"))
        );
        return aiExplanationClient.polishExplanation(structured);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
