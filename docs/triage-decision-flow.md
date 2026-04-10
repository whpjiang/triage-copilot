# 分诊决策链路说明

## 主链路

当前 `POST /api/triage/assess` 采用以下顺序执行：

1. 用户输入症状、年龄、性别
2. `PopulationProfileService` 归一化用户画像
3. `PathwayTagService` 推断场景路径标签
4. `DiseaseCandidateService` 基于疾病名、别名、症状关键词做首轮疾病候选识别
5. `AiDiseaseRecallClient` 在规则候选之后做受白名单约束的补充召回
6. `DiseaseNormalizeService` 对性别、年龄、关键词等规则做归一化处理
7. `MedicalCapabilityService` 将疾病候选映射为标准医学能力并结合路径标签排序
8. 再次按人群约束过滤医学能力
9. `LocalDepartmentMappingService` 将医学能力映射到本地医院真实科室
10. `DoctorRecommendationService` 在本地科室基础上推荐医生候选
11. `TriageExplanationService` 生成结构化解释文本

## AI 边界

当前版本中：

- 最终年龄、性别、人群约束由结构化规则控制
- 疾病到能力、能力到本地科室、科室到医生由数据库关系控制
- 路径标签由本地规则推断
- AI 只承担两类职责
  - 对结构化结果做解释润色
  - 在疾病候选阶段做受白名单约束的补充召回

这意味着 AI 不会直接决定：

- 最终疾病编码
- 医学能力最终排序
- 本地科室最终落点
- 医生最终推荐落点

## 输出结构

接口返回以下层次信息：

- `populationProfile`
- `pathwayTags`
- `candidateDiseases`
- `capabilityRecommendations`
- `departmentRecommendations`
- `doctorRecommendations`
- `explanation`

这样调用方可以清楚看到：

- 候选疾病是什么
- 为什么会召回这些医学能力
- 哪些路径标签参与了排序
- 最终落到哪个本地真实科室
- 对应有哪些本地医生候选可作为扩展位返回

## 复杂场景承载方式

### 儿童发热 / 咳嗽

- 通过症状关键词或 AI 补召回命中儿科相关疾病
- 由年龄段和 `child_fever_pathway` 共同过滤
- 可召回 `cap_pediatrics` 与 `cap_pediatric_fever_clinic`
- 最终映射到 `儿科门诊` 或 `儿童发热门诊`

### 老年记忆下降

- 疾病候选命中老年认知下降相关疾病
- 可召回 `cap_geriatrics` 与 `cap_memory_clinic`
- 最终落到 `老年病科` 或 `记忆障碍门诊`

### 男性排尿异常

- 疾病候选命中前列腺相关疾病
- 路径标签 `male_urinary_pathway` 提升专病能力
- 最终可落到 `男科门诊` 或 `男性排尿异常门诊`

### 腰腿痛 / 脊柱问题

- 疾病候选命中 `lumbar_disc_herniation`
- 优先召回 `cap_spine_surgery` 与 `cap_spine_pain_clinic`
- 最终落到 `脊柱外科门诊` 或 `脊柱疼痛专病门诊`

### 移植术后复查

- 通过路径标签 `transplant_followup`
- 召回特殊路径能力 `cap_transplant_followup`
- 最终落到 `器官移植随访门诊`
