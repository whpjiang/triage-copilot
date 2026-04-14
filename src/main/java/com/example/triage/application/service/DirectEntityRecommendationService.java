package com.example.triage.application.service;

import com.example.triage.application.dto.RecommendationCardDto;
import com.example.triage.application.dto.TriageChatRequest;
import com.example.triage.application.dto.TriageChatResponse;
import com.example.triage.domain.triage.ConversationStage;
import com.example.triage.domain.triage.IntentType;
import com.example.triage.domain.triage.RouteType;
import com.example.triage.domain.triage.SeverityLevel;
import com.example.triage.infrastructure.persistence.entity.HospitalEntity;
import com.example.triage.infrastructure.persistence.model.DepartmentSearchRecord;
import com.example.triage.infrastructure.persistence.model.DoctorRecord;
import com.example.triage.infrastructure.persistence.repository.DoctorDataRepository;
import com.example.triage.infrastructure.persistence.repository.HospitalDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DirectEntityRecommendationService {

    private final HospitalDataRepository hospitalDataRepository;
    private final DoctorDataRepository doctorDataRepository;

    public DirectEntityRecommendationService(HospitalDataRepository hospitalDataRepository,
                                             DoctorDataRepository doctorDataRepository) {
        this.hospitalDataRepository = hospitalDataRepository;
        this.doctorDataRepository = doctorDataRepository;
    }

    public TriageChatResponse recommend(String sessionId,
                                        IntentType intentType,
                                        TriageChatRequest request,
                                        String query) {
        return switch (intentType) {
            case DIRECT_HOSPITAL_QUERY -> recommendHospitals(sessionId, request, query);
            case DIRECT_DEPARTMENT_QUERY -> recommendDepartments(sessionId, request, query);
            case DIRECT_DOCTOR_QUERY -> recommendDoctors(sessionId, request, query);
            default -> buildEmptyResponse(sessionId, intentType, query);
        };
    }

    private TriageChatResponse recommendHospitals(String sessionId, TriageChatRequest request, String query) {
        List<HospitalEntity> hospitals = isEmergencyQuery(query)
                ? hospitalDataRepository.findEmergencyHospitals(request.getCity(), request.getArea())
                : hospitalDataRepository.searchHospitalsByQuery(request.getCity(), request.getArea(), query);
        if (hospitals.isEmpty() && StringUtils.hasText(request.getArea())) {
            hospitals = isEmergencyQuery(query)
                    ? hospitalDataRepository.findEmergencyHospitals(request.getCity(), null)
                    : hospitalDataRepository.searchHospitalsByQuery(request.getCity(), null, query);
        }
        List<DepartmentSearchRecord> departments = hospitalDataRepository.searchDepartmentsByQuery(request.getCity(), request.getArea(), query);
        if (departments.isEmpty() && StringUtils.hasText(request.getArea())) {
            departments = hospitalDataRepository.searchDepartmentsByQuery(request.getCity(), null, query);
        }
        List<RecommendationCardDto> cards = new ArrayList<>();
        hospitals.stream().limit(2).forEach(item -> cards.add(buildHospitalCard(item, isEmergencyQuery(query))));
        departments.stream().limit(2).forEach(item -> cards.add(buildDepartmentCard(item, "Hospital-linked department")));
        return buildDirectResponse(
                sessionId,
                IntentType.DIRECT_HOSPITAL_QUERY,
                query,
                hospitals.size(),
                departments.size(),
                cards,
                hospitals.isEmpty()
                        ? "I could not find a strong hospital match yet. Try adding a symptom, department, or district."
                        : "Here are hospitals that fit your direct hospital query."
        );
    }

    private TriageChatResponse recommendDepartments(String sessionId, TriageChatRequest request, String query) {
        List<DepartmentSearchRecord> departments = hospitalDataRepository.searchDepartmentsByQuery(request.getCity(), request.getArea(), query);
        if (departments.isEmpty() && StringUtils.hasText(request.getArea())) {
            departments = hospitalDataRepository.searchDepartmentsByQuery(request.getCity(), null, query);
        }
        List<RecommendationCardDto> cards = departments.stream()
                .limit(5)
                .map(item -> buildDepartmentCard(item, "Direct department recommendation"))
                .toList();
        return buildDirectResponse(
                sessionId,
                IntentType.DIRECT_DEPARTMENT_QUERY,
                query,
                0,
                departments.size(),
                cards,
                departments.isEmpty()
                        ? "I could not match a department yet. Try adding a symptom, body part, or target specialty."
                        : "Based on your question, these departments are the best direct match."
        );
    }

    private TriageChatResponse recommendDoctors(String sessionId, TriageChatRequest request, String query) {
        List<DoctorRecord> doctors = doctorDataRepository.searchDoctorsByQuery(request.getCity(), request.getArea(), query);
        if (doctors.isEmpty() && StringUtils.hasText(request.getArea())) {
            doctors = doctorDataRepository.searchDoctorsByQuery(request.getCity(), null, query);
        }
        List<RecommendationCardDto> cards = doctors.stream()
                .limit(5)
                .map(this::buildDoctorCard)
                .toList();
        return buildDirectResponse(
                sessionId,
                IntentType.DIRECT_DOCTOR_QUERY,
                query,
                0,
                0,
                cards,
                doctors.isEmpty()
                        ? "I could not match a doctor yet. Try adding the disease direction, department, or hospital."
                        : "These doctors best match your direct doctor query."
        );
    }

    private TriageChatResponse buildDirectResponse(String sessionId,
                                                   IntentType intentType,
                                                   String query,
                                                   int hospitalCount,
                                                   int departmentCount,
                                                   List<RecommendationCardDto> cards,
                                                   String replyText) {
        TriageChatResponse response = new TriageChatResponse();
        response.setSessionId(sessionId);
        response.setStage(ConversationStage.DIRECT_ENTITY.name());
        response.setReplyText(replyText);
        response.setFollowUpQuestion(null);
        response.setEmergency(false);
        response.setSeverityLevel(SeverityLevel.MEDIUM.name());
        response.setRouteType(RouteType.DIRECT_ENTITY.name());
        response.setCards(cards);
        Map<String, Object> trace = new LinkedHashMap<>();
        trace.put("intent", intentType.name());
        trace.put("query", query);
        trace.put("hospitalCount", hospitalCount);
        trace.put("departmentCount", departmentCount);
        trace.put("cardCount", cards.size());
        response.setDecisionTrace(trace);
        return response;
    }

    private TriageChatResponse buildEmptyResponse(String sessionId, IntentType intentType, String query) {
        return buildDirectResponse(
                sessionId,
                intentType,
                query,
                0,
                0,
                List.of(),
                "I understand the direct recommendation intent, but I still need a clearer target."
        );
    }

    private RecommendationCardDto buildHospitalCard(HospitalEntity hospital, boolean emergencyOnly) {
        RecommendationCardDto card = new RecommendationCardDto();
        card.setCardType("hospital");
        card.setTitle(hospital.hospitalName);
        card.setSubtitle("%s | %s".formatted(
                defaultText(hospital.districtName, "Unknown district"),
                defaultText(hospital.hospitalLevel, "Hospital")
        ));
        card.setDescription(emergencyOnly
                ? "This hospital has emergency capacity and should be prioritized for urgent in-person care."
                : "This hospital matches your direct hospital query.");
        card.setHospitalName(hospital.hospitalName);
        card.setTag(emergencyOnly ? "Emergency" : "Direct hospital");
        card.setScore(hospital.authorityScore);
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("districtName", hospital.districtName);
        extra.put("hospitalLevel", hospital.hospitalLevel);
        extra.put("authorityScore", hospital.authorityScore);
        extra.put("isEmergency", hospital.isEmergency);
        card.setExtra(extra);
        return card;
    }

    private RecommendationCardDto buildDepartmentCard(DepartmentSearchRecord department, String tag) {
        RecommendationCardDto card = new RecommendationCardDto();
        card.setCardType("department");
        card.setTitle("%s | %s".formatted(department.hospitalName(), department.departmentName()));
        card.setSubtitle(defaultText(department.parentDepartmentName(), "Department"));
        card.setDescription("This department is a direct match for your current question.");
        card.setHospitalName(department.hospitalName());
        card.setDepartmentName(department.departmentName());
        card.setTag(tag);
        card.setScore(department.authorityScore());
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("districtName", department.districtName());
        extra.put("authorityScore", department.authorityScore());
        extra.put("standardDeptCode", department.standardDeptCode());
        extra.put("subspecialtyCode", department.subspecialtyCode());
        extra.put("isEmergency", department.isEmergency());
        card.setExtra(extra);
        return card;
    }

    private RecommendationCardDto buildDoctorCard(DoctorRecord doctor) {
        RecommendationCardDto card = new RecommendationCardDto();
        card.setCardType("doctor");
        card.setTitle("%s | %s".formatted(doctor.hospitalName(), doctor.doctorName()));
        card.setSubtitle("%s | %s".formatted(
                defaultText(doctor.departmentName(), "Department"),
                defaultText(doctor.title(), "Doctor")
        ));
        card.setDescription(defaultText(doctor.specialtyText(), "Direct doctor recommendation"));
        card.setHospitalName(doctor.hospitalName());
        card.setDepartmentName(doctor.departmentName());
        card.setDoctorName(doctor.doctorName());
        card.setTag("Direct doctor");
        card.setScore(doctor.authorityScore());
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("districtName", doctor.districtName());
        extra.put("authorityScore", doctor.authorityScore());
        extra.put("academicTitleScore", doctor.academicTitleScore());
        extra.put("campusName", doctor.campusName());
        extra.put("isExpert", doctor.isExpert());
        card.setExtra(extra);
        return card;
    }

    private boolean isEmergencyQuery(String query) {
        if (!StringUtils.hasText(query)) {
            return false;
        }
        String normalized = query.toLowerCase(Locale.ROOT);
        return normalized.contains("emergency") || normalized.contains("急诊");
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
