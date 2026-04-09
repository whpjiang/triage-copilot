# 分诊决策链路说明

## 主链路

当前 `POST /api/triage/assess` 采用以下顺序执行：

1. 用户输入症状、年龄、性别
2. `PopulationProfileService` 归一化用户画像
3. `PathwayTagService` 推断场景路径标签
4. `DiseaseCandidateService` 基于疾病名、别名、症状关键词识别疾病候选
5. `DiseaseNormalizeService` 对性别和年龄约束做强过滤
6. `MedicalCapabilityService` 将疾病候选映射为标准医学能力并结合路径标签排序
7. 再次按人群约束过滤医学能力
8. `LocalDepartmentMappingService` 将医学能力映射到本地医院真实科室
9. `TriageExplanationService` 生成结构化解释文本

## AI 边界

当前版本中：

- 最终性别/年龄约束由结构化规则控制
- 疾病到能力、能力到本地科室由数据库关系控制
- 路径标签由本地规则推断
- AI 只用于可选的解释润色，不改变结构化决策结果

## 输出结构

接口返回以下层次信息：

- `populationProfile`
- `pathwayTags`
- `candidateDiseases`
- `capabilityRecommendations`
- `departmentRecommendations`
- `explanation`

这让调用方可以清楚看到：

- 候选疾病是什么
- 为什么会召回这些医学能力
- 哪些场景路径参与了排序
- 最终落到哪个本地真实科室

## 复杂场景承载方式

### 儿童发热 / 咳嗽

- 通过疾病关键词命中
- 由年龄段和 `child_fever_pathway` 共同过滤
- 可召回 `cap_pediatrics` 与 `cap_pediatric_fever_clinic`
- 最终映射到 `儿科门诊` 或 `儿童发热门诊`

### 老年记忆下降

- 疾病候选命中老年认知下降
- 召回 `cap_geriatrics` 与 `cap_memory_clinic`
- 最终落到 `老年病科` 或 `记忆障碍门诊`

### 男性排尿异常

- 疾病候选命中前列腺相关疾病
- 路径标签 `male_urinary_pathway` 提升专病能力
- 最终可落到 `男科门诊` 或 `男性排尿异常门诊`

### 腰腿痛 / 腰椎问题

- 疾病候选命中 `lumbar_disc_herniation`
- 优先召回 `cap_spine_surgery` 与 `cap_spine_pain_clinic`
- 最终落到 `脊柱外科门诊` 或 `脊柱疼痛专病门诊`

### 移植术后复查

- 通过路径标签 `transplant_followup`
- 召回特殊路径能力 `cap_transplant_followup`
- 最终落到 `器官移植随访门诊`
