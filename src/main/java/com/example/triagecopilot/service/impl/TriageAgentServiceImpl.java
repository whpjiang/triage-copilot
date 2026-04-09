package com.example.triagecopilot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.triagecopilot.dto.TriageAnalyzeRequest;
import com.example.triagecopilot.dto.TriageAnalyzeResponse;
import com.example.triagecopilot.entity.DictFunctionalClinic;
import com.example.triagecopilot.entity.DictStandardDept;
import com.example.triagecopilot.entity.DictTagAlias;
import com.example.triagecopilot.entity.Doctor;
import com.example.triagecopilot.entity.Hospital;
import com.example.triagecopilot.entity.ServiceUnit;
import com.example.triagecopilot.entity.ServiceUnitTagMap;
import com.example.triagecopilot.mapper.DictFunctionalClinicMapper;
import com.example.triagecopilot.mapper.DictStandardDeptMapper;
import com.example.triagecopilot.mapper.DictTagAliasMapper;
import com.example.triagecopilot.mapper.DoctorMapper;
import com.example.triagecopilot.mapper.HospitalMapper;
import com.example.triagecopilot.mapper.ServiceUnitMapper;
import com.example.triagecopilot.mapper.ServiceUnitTagMapMapper;
import com.example.triagecopilot.service.TriageAgentService;
import com.example.triagecopilot.tools.TriageTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TriageAgentServiceImpl implements TriageAgentService {

    private static final int CHILD_AGE = 18;
    private static final List<String> GYNE = List.of("\u5987", "\u4ea7", "gyne", "obstetric");
    private static final List<String> ANDRO = List.of("\u7537\u79d1", "andrology");
    private static final List<String> PED = List.of("\u513f", "\u513f\u7ae5", "pediatric", "paediatric");
    private static final List<String> ACUTE_COMMON = List.of("\u5934\u75db", "\u53d1\u70e7", "\u53d1\u70ed", "headache", "fever");
    private static final List<String> UNRELATED_FOR_ACUTE = List.of("\u9ebb\u9189", "\u75e4\u75ae", "\u7f8e\u5bb9", "\u6574\u5f62", "\u4f53\u68c0");
    private static final Pattern MALE = Pattern.compile("\\b(male|man|boy)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern FEMALE = Pattern.compile("\\b(female|woman|girl)\\b", Pattern.CASE_INSENSITIVE);

    private final ObjectProvider<ChatModel> chatModelProvider;
    private final TriageTools triageTools;
    private final DictTagAliasMapper aliasMapper;
    private final DictStandardDeptMapper deptMapper;
    private final DictFunctionalClinicMapper clinicMapper;
    private final ServiceUnitTagMapMapper tagMapMapper;
    private final ServiceUnitMapper unitMapper;
    private final HospitalMapper hospitalMapper;
    private final DoctorMapper doctorMapper;

    public TriageAgentServiceImpl(ObjectProvider<ChatModel> chatModelProvider, TriageTools triageTools, DictTagAliasMapper aliasMapper, DictStandardDeptMapper deptMapper, DictFunctionalClinicMapper clinicMapper, ServiceUnitTagMapMapper tagMapMapper, ServiceUnitMapper unitMapper, HospitalMapper hospitalMapper, DoctorMapper doctorMapper) {
        this.chatModelProvider = chatModelProvider;
        this.triageTools = triageTools;
        this.aliasMapper = aliasMapper;
        this.deptMapper = deptMapper;
        this.clinicMapper = clinicMapper;
        this.tagMapMapper = tagMapMapper;
        this.unitMapper = unitMapper;
        this.hospitalMapper = hospitalMapper;
        this.doctorMapper = doctorMapper;
    }

    @Override
    public TriageAnalyzeResponse analyze(TriageAnalyzeRequest req) {
        Integer age = req.getAge();
        String gender = normalizeGender(req.getGender());
        if (age == null || !StringUtils.hasText(gender)) {
            TriageAnalyzeResponse r = new TriageAnalyzeResponse();
            r.setRecommendedDepartment("-");
            r.setRecommendedFunctionalClinic("-");
            r.setUrgencyLevel("MEDIUM");
            r.setUrgencyReason("Need age and gender.");
            r.setAdvice("Please provide age and gender first.");
            r.setDisclaimer("This suggestion cannot replace clinical diagnosis.");
            return r;
        }

        String text = norm(req.getSymptoms() + " " + nvl(req.getSpecialCondition()));
        List<String> keys = Arrays.stream(text.split("[,.;\\uFF0C\\u3002\\uFF1B\\u3001\\s]+")).filter(x -> x.length() > 1).distinct().toList();
        List<DictStandardDept> depts = deptMapper.selectList(new LambdaQueryWrapper<DictStandardDept>().eq(DictStandardDept::getIsActive, 1));
        List<DictFunctionalClinic> clinics = clinicMapper.selectList(new LambdaQueryWrapper<DictFunctionalClinic>().eq(DictFunctionalClinic::getIsActive, 1));
        List<DictTagAlias> aliases = aliasMapper.selectList(new LambdaQueryWrapper<DictTagAlias>().eq(DictTagAlias::getIsActive, 1));
        List<DictStandardDept> cDepts = depts.stream().filter(d -> allowDept(d.getName(), gender)).toList();
        List<DictFunctionalClinic> cClinics = clinics.stream().filter(c -> allowDept(c.getName(), gender)).toList();
        if (containsAny(text, ACUTE_COMMON)) {
            cDepts = cDepts.stream().filter(d -> !containsAny(norm(d.getName()), UNRELATED_FOR_ACUTE)).toList();
            cClinics = cClinics.stream().filter(c -> !containsAny(norm(c.getName()), UNRELATED_FOR_ACUTE)).toList();
        }
        if (isChild(age)) {
            List<DictStandardDept> p = cDepts.stream().filter(d -> containsAny(norm(d.getName()), PED)).toList();
            if (!p.isEmpty()) cDepts = p;
            List<DictFunctionalClinic> pc = cClinics.stream().filter(d -> containsAny(norm(d.getName()), PED)).toList();
            if (!pc.isEmpty()) cClinics = pc;
        }
        Map<String, Double> deptScore = scoreTags("standard", keys, text, aliases, cDepts, null);
        Map<String, Double> clinicScore = scoreTags("functional", keys, text, aliases, null, cClinics);
        DictStandardDept topDept = pickDept(deptScore, cDepts.isEmpty() ? depts : cDepts);
        DictFunctionalClinic topClinic = pickClinic(clinicScore, cClinics.isEmpty() ? clinics : cClinics);
        topDept = applyHardDeptOverride(topDept, cDepts.isEmpty() ? depts : cDepts, age, text);
        topClinic = applyHardClinicOverride(topClinic, cClinics.isEmpty() ? clinics : cClinics, age, text);

        TriageAnalyzeResponse res = new TriageAnalyzeResponse();
        res.setRecommendedDepartment(topDept == null ? "General Internal Medicine" : topDept.getName());
        res.setRecommendedFunctionalClinic(topClinic == null ? "General Clinic" : topClinic.getName());
        res.setUrgencyLevel("MEDIUM");
        res.setUrgencyReason("Outpatient evaluation is recommended.");
        res.setDisclaimer("This suggestion cannot replace clinical diagnosis.");

        Ranking rank = rank(keys, deptScore, clinicScore, req.getCity(), age, gender);
        res.setHospitalOptions(rank.hospitals);
        res.setDoctorOptions(rank.doctors);
        TriageAnalyzeResponse.DebugInfo dbg = new TriageAnalyzeResponse.DebugInfo();
        dbg.setMatchedKeywords(keys);
        dbg.setStandardDepartmentScores(toItems(remap(deptScore, depts)));
        dbg.setFunctionalClinicScores(toItems(remap(clinicScore, clinics)));
        dbg.setHospitalScores(toItems(rank.hospitalScore));
        dbg.setDoctorScores(toItems(rank.doctorScore));
        res.setDebug(dbg);
        res.setAdvice(advice(req, res));
        return res;
    }

    private Ranking rank(List<String> keys, Map<String, Double> deptScore, Map<String, Double> clinicScore, String city, Integer age, String gender) {
        Ranking r = new Ranking();
        List<Hospital> hospitals = hospitalMapper.selectList(new LambdaQueryWrapper<Hospital>().eq(Hospital::getIsActive, 1).like(StringUtils.hasText(city), Hospital::getCity, city));
        if (hospitals.isEmpty()) return r;
        Set<Long> hids = hospitals.stream().map(Hospital::getHospitalId).collect(Collectors.toSet());
        Map<Long, Hospital> hMap = hospitals.stream().collect(Collectors.toMap(Hospital::getHospitalId, x -> x, (a, b) -> a));
        List<ServiceUnit> units = unitMapper.selectList(new LambdaQueryWrapper<ServiceUnit>().eq(ServiceUnit::getIsActive, 1).in(ServiceUnit::getHospitalId, hids));
        if (units.isEmpty()) return r;
        Set<Long> uids = units.stream().map(ServiceUnit::getServiceUnitId).collect(Collectors.toSet());
        Map<Long, Double> uScore = new HashMap<>();
        List<ServiceUnitTagMap> maps = tagMapMapper.selectList(new LambdaQueryWrapper<ServiceUnitTagMap>().in(ServiceUnitTagMap::getServiceUnitId, uids).in(ServiceUnitTagMap::getTagType, Arrays.asList("standard", "functional")));
        for (ServiceUnitTagMap m : maps) {
            double base = "standard".equalsIgnoreCase(nvl(m.getTagType())) ? deptScore.getOrDefault(m.getTagCode(), 0.0) : clinicScore.getOrDefault(m.getTagCode(), 0.0);
            if (base <= 0) continue;
            double w = m.getWeight() == null ? 1.0 : m.getWeight().doubleValue();
            uScore.merge(m.getServiceUnitId(), base * w, Double::sum);
        }
        for (ServiceUnit u : units) {
            String n = norm(nvl(u.getNameNorm()) + " " + nvl(u.getNameRaw()));
            if (!allowDept(n, gender)) continue;
            for (String k : keys) if (n.contains(k)) uScore.merge(u.getServiceUnitId(), 0.5, Double::sum);
            if (isChild(age) && containsAny(n, PED)) uScore.merge(u.getServiceUnitId(), 1.0, Double::sum);
        }
        Map<Long, Double> hScore = new HashMap<>();
        for (ServiceUnit u : units) {
            Double s = uScore.get(u.getServiceUnitId());
            if (s == null) continue;
            hScore.merge(u.getHospitalId(), s, Math::max);
        }
        List<Long> topH = hScore.entrySet().stream().sorted(Map.Entry.<Long, Double>comparingByValue().reversed()).limit(5).map(Map.Entry::getKey).toList();
        for (Long id : topH) {
            Hospital h = hMap.get(id);
            if (h == null) continue;
            TriageAnalyzeResponse.TriageOption o = new TriageAnalyzeResponse.TriageOption();
            o.setId(String.valueOf(id));
            o.setName(StringUtils.hasText(h.getNameNorm()) ? h.getNameNorm() : h.getNameRaw());
            o.setSubtitle(nvl(h.getCity()) + " " + nvl(h.getHospitalLevel()));
            o.setExtra("Emergency: " + (h.getHasEmergency() != null && h.getHasEmergency() == 1 ? "yes" : "no"));
            r.hospitals.add(o);
            r.hospitalScore.put(o.getName(), round2(hScore.getOrDefault(id, 0.0)));
        }
        List<Doctor> doctors = doctorMapper.selectList(new LambdaQueryWrapper<Doctor>().eq(Doctor::getIsActive, 1).in(!topH.isEmpty(), Doctor::getHospitalId, topH));
        Map<Doctor, Double> dScore = new LinkedHashMap<>();
        for (Doctor d : doctors) {
            if (!allowDoctor(d, age, gender)) continue;
            double s = uScore.getOrDefault(d.getServiceUnitId(), 0.0);
            String profile = norm(nvl(d.getSpecialtyText()) + " " + nvl(d.getTitle()) + " " + nvl(d.getNameNorm()));
            for (String k : keys) if (profile.contains(k)) s += 0.7;
            if (s <= 0) continue;
            dScore.put(d, s);
        }
        dScore.entrySet().stream().sorted(Map.Entry.<Doctor, Double>comparingByValue().reversed()).limit(8).forEach(e -> {
            Doctor d = e.getKey();
            TriageAnalyzeResponse.TriageOption o = new TriageAnalyzeResponse.TriageOption();
            o.setId(String.valueOf(d.getDoctorId()));
            o.setName(StringUtils.hasText(d.getNameNorm()) ? d.getNameNorm() : d.getNameRaw());
            o.setSubtitle(nvl(d.getTitle()));
            o.setExtra(nvl(d.getSpecialtyText()));
            r.doctors.add(o);
            r.doctorScore.put(o.getName(), round2(e.getValue()));
        });
        return r;
    }

    private Map<String, Double> scoreTags(String type, List<String> keys, String text, List<DictTagAlias> aliases, List<DictStandardDept> depts, List<DictFunctionalClinic> clinics) {
        Map<String, Double> score = new HashMap<>();
        for (DictTagAlias a : aliases) {
            if (!type.equalsIgnoreCase(nvl(a.getTagType()))) continue;
            String alias = norm(a.getAliasText());
            if (StringUtils.hasText(alias) && text.contains(alias)) score.merge(a.getTagCode(), 3.0, Double::sum);
        }
        if (depts != null) for (DictStandardDept d : depts) { String n = norm(d.getName()); for (String k : keys) if (n.contains(k)) score.merge(d.getCode(), 1.2, Double::sum); }
        if (clinics != null) for (DictFunctionalClinic c : clinics) { String n = norm(c.getName()); for (String k : keys) if (n.contains(k)) score.merge(c.getCode(), 1.0, Double::sum); }
        return score;
    }

    private DictStandardDept pickDept(Map<String, Double> score, List<DictStandardDept> depts) {
        if (depts.isEmpty()) return null;
        if (score.isEmpty()) return depts.get(0);
        Map<String, DictStandardDept> m = depts.stream().collect(Collectors.toMap(DictStandardDept::getCode, x -> x, (a, b) -> a));
        return score.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed()).map(e -> m.get(e.getKey())).filter(x -> x != null).findFirst().orElse(depts.get(0));
    }

    private DictFunctionalClinic pickClinic(Map<String, Double> score, List<DictFunctionalClinic> clinics) {
        if (clinics.isEmpty()) return null;
        if (score.isEmpty()) return clinics.get(0);
        Map<String, DictFunctionalClinic> m = clinics.stream().collect(Collectors.toMap(DictFunctionalClinic::getCode, x -> x, (a, b) -> a));
        return score.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed()).map(e -> m.get(e.getKey())).filter(x -> x != null).findFirst().orElse(clinics.get(0));
    }

    private DictStandardDept applyHardDeptOverride(DictStandardDept current, List<DictStandardDept> candidates, Integer age, String text) {
        if (candidates.isEmpty()) {
            return current;
        }
        if (isChild(age)) {
            DictStandardDept pediatric = findDeptByKeywords(candidates, PED);
            if (pediatric != null) {
                return pediatric;
            }
        }
        if (containsAny(text, ACUTE_COMMON)) {
            DictStandardDept feverRelated = findDeptByKeywords(candidates, List.of("\u53d1\u70ed", "\u5185\u79d1", "\u611f\u67d3", "\u6025\u8bca"));
            if (feverRelated != null) {
                return feverRelated;
            }
        }
        return current;
    }

    private DictFunctionalClinic applyHardClinicOverride(DictFunctionalClinic current, List<DictFunctionalClinic> candidates, Integer age, String text) {
        if (candidates.isEmpty()) {
            return current;
        }
        if (isChild(age)) {
            DictFunctionalClinic pediatric = findClinicByKeywords(candidates, PED);
            if (pediatric != null) {
                return pediatric;
            }
        }
        if (containsAny(text, ACUTE_COMMON)) {
            DictFunctionalClinic fever = findClinicByKeywords(candidates, List.of("\u53d1\u70ed", "\u666e\u901a", "\u513f\u79d1", "\u5185\u79d1"));
            if (fever != null) {
                return fever;
            }
        }
        return current;
    }

    private DictStandardDept findDeptByKeywords(List<DictStandardDept> list, List<String> keywords) {
        for (DictStandardDept d : list) {
            if (containsAny(norm(d.getName()), keywords)) {
                return d;
            }
        }
        return null;
    }

    private DictFunctionalClinic findClinicByKeywords(List<DictFunctionalClinic> list, List<String> keywords) {
        for (DictFunctionalClinic c : list) {
            if (containsAny(norm(c.getName()), keywords)) {
                return c;
            }
        }
        return null;
    }

    private boolean allowDept(String text, String gender) {
        String n = norm(text);
        if ("male".equals(gender) && containsAny(n, GYNE)) return false;
        return !"female".equals(gender) || !containsAny(n, ANDRO);
    }

    private boolean allowDoctor(Doctor d, Integer age, String gender) {
        if (!allowDept(nvl(d.getSpecialtyText()) + " " + nvl(d.getTitle()), gender)) return false;
        if (!isChild(age)) return true;
        String c = norm(d.getCrowdLimit());
        return !StringUtils.hasText(c) || "unknown".equals(c) || "unlimited".equals(c) || "child".equals(c);
    }

    private boolean isChild(Integer age) { return age != null && age < CHILD_AGE; }
    private String norm(String s) { return StringUtils.hasText(s) ? s.toLowerCase(Locale.ROOT).trim() : ""; }
    private String nvl(String s) { return s == null ? "" : s; }
    private boolean containsAny(String text, List<String> words) { for (String w : words) if (text.contains(norm(w))) return true; return false; }

    private String normalizeGender(String raw) {
        if (!StringUtils.hasText(raw)) return null;
        String g = norm(raw);
        if (g.contains("\u5973") || FEMALE.matcher(g).find()) return "female";
        if (g.contains("\u7537") || MALE.matcher(g).find()) return "male";
        return null;
    }

    private String advice(TriageAnalyzeRequest req, TriageAnalyzeResponse res) {
        ChatModel chatModel = chatModelProvider.getIfAvailable();
        if (chatModel == null) return "Preliminary recommendation generated from database ranking.";
        String prompt = "Symptoms: " + req.getSymptoms() + "; Age: " + req.getAge() + "; Gender: " + req.getGender() + "; Department: " + res.getRecommendedDepartment() + "; Clinic: " + res.getRecommendedFunctionalClinic() + "; Hospitals: " + res.getHospitalOptions().stream().map(TriageAnalyzeResponse.TriageOption::getName).toList() + "; Doctors: " + res.getDoctorOptions().stream().map(TriageAnalyzeResponse.TriageOption::getName).toList();
        try {
            return ChatClient.builder(chatModel).build().prompt().system("Provide concise triage guidance. Do not change recommended resources.").user(prompt).tools(triageTools).call().content();
        } catch (Exception ex) {
            return "Please complete outpatient evaluation according to recommended department.";
        }
    }

    private Map<String, Double> remap(Map<String, Double> score, List<?> dict) {
        Map<String, String> nameMap = new HashMap<>();
        for (Object x : dict) {
            if (x instanceof DictStandardDept d) nameMap.put(d.getCode(), d.getName());
            if (x instanceof DictFunctionalClinic c) nameMap.put(c.getCode(), c.getName());
        }
        Map<String, Double> out = new LinkedHashMap<>();
        score.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed()).forEach(e -> out.put(nameMap.getOrDefault(e.getKey(), e.getKey()), e.getValue()));
        return out;
    }

    private List<TriageAnalyzeResponse.ScoreItem> toItems(Map<String, Double> score) {
        return score.entrySet().stream().limit(10).map(e -> {
            TriageAnalyzeResponse.ScoreItem i = new TriageAnalyzeResponse.ScoreItem();
            i.setId(e.getKey());
            i.setName(e.getKey());
            i.setScore(round2(e.getValue()));
            return i;
        }).toList();
    }

    private double round2(Double v) { return v == null ? 0.0 : Math.round(v * 100.0) / 100.0; }

    private static class Ranking {
        private final List<TriageAnalyzeResponse.TriageOption> hospitals = new ArrayList<>();
        private final List<TriageAnalyzeResponse.TriageOption> doctors = new ArrayList<>();
        private final Map<String, Double> hospitalScore = new LinkedHashMap<>();
        private final Map<String, Double> doctorScore = new LinkedHashMap<>();
    }
}
