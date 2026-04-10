# 分诊决策链路说明

## 主链路

`POST /api/triage/assess` 当前按以下顺序执行：

1. `PopulationProfileService`
   标准化性别、年龄和年龄段，并生成 `crowdTags`
2. `PathwayTagService`
   根据症状和人群识别路径标签
3. `DiseaseCandidateService`
   先用规则识别疾病候选，再尝试 AI 补召回
4. `MedicalCapabilityService`
   将疾病候选映射到标准医学能力，并按人群约束过滤
5. `LocalDepartmentMappingService`
   将标准医学能力映射到本地真实科室
6. `DoctorRecommendationService`
   在已命中的本地科室基础上推荐医生扩展位
7. `TriageExplanationService`
   生成结果解释

## 年龄与人群约束

当前统一规则：

- `child`: 0-11
- `adolescent`: 12-17
- `adult`: 18-64
- `elderly`: 65+

约束会应用在：

- 疾病候选过滤
- 医学能力过滤
- 本地科室过滤
- 医生过滤

## AI 的边界

AI 只负责：

- 症状理解后的补充召回
- 结果解释

AI 不负责：

- 最终疾病编码确定
- 性别和年龄强过滤
- 医学能力最终排序
- 本地科室最终落点
- 高风险场景唯一决策

## AI 补召回保护

### 白名单保护

AI 只能从“已入库且已通过性别/年龄过滤”的疾病集合中补充 disease code。

### 高风险保护

遇到以下高风险症状时，直接跳过 AI 补召回：

- 胸痛
- 呼吸困难
- 昏迷
- 意识不清
- 抽搐
- 大出血
- 偏瘫
- 言语不清

### 审计日志

每次 AI 补召回都会写入 `ai_recall_audit_log`，用于后续追踪：

- 是否跳过
- 为什么跳过
- 规则候选有哪些
- AI 最终补了什么

## 导入到分诊的衔接

### 基础数据导入

导入链路会把疾病、医院和科室写入基础表，并尝试自动映射医学能力。

### 自动映射

当前首版规则会自动识别常见科室，例如：

- 儿科
- 老年病科
- 妇科
- 男科
- 骨科
- 脊柱外科
- 脊柱疼痛门诊
- 移植随访门诊

### 待人工复核

以下情况会生成 `import_review_item`：

- 缺少症状关键词
- 缺少标准医学能力线索
- 本地科室完全无法自动映射
- 自动映射了多个能力，需要人工确认

## 典型场景

### 儿童发热

- 画像进入 `child`
- 路径命中 `child_fever_pathway`
- 召回 `cap_pediatrics` 和 `cap_pediatric_fever_clinic`
- 最终落到 `儿科门诊` 或 `儿童发热门诊`

### 老年记忆下降

- 画像进入 `elderly`
- 路径命中 `elderly_multisymptom_pathway`
- 召回 `cap_geriatrics` 和 `cap_memory_clinic`
- 最终落到 `老年病科` 或 `记忆障碍门诊`

### 腰腿痛

- 路径命中 `spine_pathway`
- 优先召回 `cap_spine_surgery` 和 `cap_spine_pain_clinic`
- 最终落到 `脊柱外科门诊` 或 `脊柱疼痛专病门诊`

### 移植术后复查

- 路径命中 `transplant_followup`
- 召回 `cap_transplant_followup`
- 最终落到 `器官移植随访门诊`
