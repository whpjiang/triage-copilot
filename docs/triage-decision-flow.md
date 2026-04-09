# 分诊决策链路说明

## 主链路

当前 `POST /api/triage/assess` 采用以下顺序执行：

1. 用户输入症状、年龄、性别
2. `PopulationProfileService` 归一化用户画像
3. `DiseaseCandidateService` 基于疾病名、别名、症状关键词识别疾病候选
4. `DiseaseNormalizeService` 对性别和年龄约束做强过滤
5. `MedicalCapabilityService` 将疾病候选映射为标准医学能力并排序
6. 再次按人群约束过滤医学能力
7. `LocalDepartmentMappingService` 将医学能力映射到本地医院真实科室
8. `TriageExplanationService` 生成结构化解释文本

## AI 边界

当前版本中：

- 最终性别/年龄约束由结构化规则控制
- 疾病到能力、能力到本地科室由数据库关系控制
- AI 只用于可选的解释润色，不改变结构化决策结果

## 输出结构

接口返回四层信息：

- `populationProfile`
- `candidateDiseases`
- `capabilityRecommendations`
- `departmentRecommendations`
- `explanation`

这让调用方可以清楚看到：

- 候选疾病是什么
- 为什么会召回这些医学能力
- 最终落到哪个本地真实科室

## 复杂场景承载方式

### 儿童发热 / 咳嗽

- 通过疾病关键词命中
- 由年龄段和 `cap_pediatrics` 共同过滤
- 最终映射到 `儿科门诊`

### 老年记忆下降

- 疾病候选命中老年认知下降
- 召回 `cap_geriatrics`
- 最终落到 `老年病科`

### 腰腿痛 / 腰椎问题

- 疾病候选命中 `lumbar_disc_herniation`
- 优先召回 `cap_spine_surgery`
- 最终落到 `脊柱外科门诊`

### 移植术后复查

- 通过人群标签 `transplant_followup`
- 召回特殊路径能力 `cap_transplant_followup`
- 最终落到 `器官移植随访门诊`
