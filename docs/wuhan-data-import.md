# 武汉数据导入说明

## 目标

本轮导入能力用于把武汉真实疾病数据和武汉本地医院科室数据接入当前结构化分诊底座，并在导入阶段完成首版治理。

## 支持的 sheet

针对 `src/main/resources/data/wuhan_triage_base_data_pack.xlsx`，当前支持：

- `Import_Disease`
- `Import_Department`

当调用：

- `datasetType=wuhan_disease` 时，默认读取 `Import_Disease`
- `datasetType=wuhan_department` 时，默认读取 `Import_Department`

CSV 导入仍然保留原有能力。

## 支持的字段名

### 疾病导入

核心字段：

- `disease_name`
- `disease_code`
- `aliases`
- `symptom_keywords`
- `gender_rule`
- `age_min`
- `age_max`
- `age_group`
- `urgency_level`
- `standard_dept_hint`

兼容的中文别名包括：

- `疾病名称`
- `标准病种`
- `病种名称`
- `诊断名称`
- `疾病编码`
- `病种编码`
- `别名`
- `疾病别名`
- `同义词`
- `俗称`
- `症状关键词`
- `关键词`
- `常见症状`
- `症状`
- `主诉关键词`
- `性别规则`
- `适用性别`
- `年龄范围`
- `最小年龄`
- `最大年龄`
- `年龄分层`
- `紧急程度`
- `标准科室`
- `医学能力线索`
- `建议科室`
- `推荐科室`

### 科室导入

核心字段：

- `hospital_name`
- `city`
- `department_name`
- `parent_department_name`
- `department_intro`
- `service_scope`
- `gender_rule`
- `age_min`
- `age_max`
- `crowd_tags`

兼容的中文别名包括：

- `医院名称`
- `医院`
- `医疗机构名称`
- `科室名称`
- `科室`
- `门诊名称`
- `所属城市`
- `城市`
- `父级科室`
- `上级科室`
- `一级科室`
- `科室简介`
- `科室介绍`
- `诊疗范围`
- `服务范围`
- `擅长方向`
- `适用性别`
- `性别规则`
- `最小年龄`
- `最大年龄`
- `年龄范围`
- `人群标签`

## 推荐导入顺序

1. 导入 `datasetType=wuhan_disease`
2. 导入 `datasetType=wuhan_department`
3. 查看 `GET /api/base-data/jobs`
4. 查看 `GET /api/base-data/jobs/detail?jobId=...`
5. 查看 `GET /api/base-data/reviews`
6. 对确认完成的 review 调用 `POST /api/base-data/reviews/resolve`

## 自动映射规则

### 疾病 -> 医学能力

当 `standard_dept_hint` 存在时，系统会通过 `CapabilityAliasDictionaryService` 自动尝试映射：

- `妇科` -> `cap_gynecology`
- `男科` / `泌尿男科` -> `cap_andrology`
- `前列腺门诊` / `排尿门诊` -> `cap_male_urinary_clinic`
- `老年病科` / `老年医学科` -> `cap_geriatrics`
- `记忆门诊` / `认知门诊` -> `cap_memory_clinic`
- `骨科` -> `cap_orthopedics`
- `脊柱外科` / `腰椎门诊` / `脊柱专病门诊` -> `cap_spine_surgery`
- `腰腿痛门诊` / `脊柱疼痛门诊` -> `cap_spine_pain_clinic`
- `移植门诊` / `移植复查` / `移植术后门诊` -> `cap_transplant_followup`

处理结果：

- 命中 1 个能力：自动写入 `disease_capability_rel`
- 命中多个能力：生成 `DISEASE_CAPABILITY_MULTI_MATCH`
- 无法映射：生成 `DISEASE_CAPABILITY_UNMAPPED`
- 缺少 hint：生成 `MISSING_STANDARD_DEPT_HINT`

### 科室 -> 医学能力

科室自动映射会综合以下来源：

- `department_name`
- `parent_department_name`
- `service_scope`
- `department_intro`

当前重点覆盖：

- 儿科 / 儿童发热门诊
- 老年病科 / 老年医学科 / 记忆门诊 / 认知障碍门诊
- 妇科
- 男科 / 泌尿男科 / 前列腺专病门诊 / 排尿门诊
- 骨科 / 脊柱外科 / 腰腿痛门诊 / 椎间盘门诊
- 移植门诊 / 移植复查 / 排异随访 / 移植术后门诊

## review 类型说明

常见 review 类型包括：

- `MISSING_DISEASE_NAME`
- `MISSING_SYMPTOM_KEYWORDS`
- `UNRECOGNIZED_GENDER_RULE`
- `UNRECOGNIZED_AGE_RANGE`
- `MISSING_STANDARD_DEPT_HINT`
- `DISEASE_CAPABILITY_UNMAPPED`
- `DISEASE_CAPABILITY_MULTI_MATCH`
- `WAIT_CAPABILITY_MAPPING`
- `AUTO_MAPPING_NEEDS_REVIEW`
- `PARENT_DEPARTMENT_CONFLICT`
- `SERVICE_SCOPE_CONFLICT`
- `HOSPITAL_NAME_NEEDS_CONFIRM`

其中：

- `AUTO_MAPPING_NEEDS_REVIEW` 表示自动命中多个能力
- `PARENT_DEPARTMENT_CONFLICT` 表示父级科室与当前科室推断冲突
- `SERVICE_SCOPE_CONFLICT` 表示科室名与诊疗范围推断冲突
- `HOSPITAL_NAME_NEEDS_CONFIRM` 表示医院名疑似占位符或待确认

## 导入后如何校验

### 任务统计

调用：

- `GET /api/base-data/jobs`
- `GET /api/base-data/jobs/detail?jobId=...`

可查看：

- 成功数
- 失败数
- review 数
- 自动映射数
- review 类型分布
- failure 示例
- recent review 示例

### 基础质量校验

调用 `GET /api/base-data/check` 可查看：

- 疾病总数
- 疾病别名总数
- 有症状关键词的疾病数
- 已建立疾病 -> 能力关系的疾病数
- 医院数
- 科室数
- 已自动映射能力的科室数
- 待复核总数
- 待复核疾病数
- 待复核科室数

## 常见失败原因

- 缺失 `disease_name`
- 缺失 `hospital_name`
- 缺失 `department_name`
- Excel sheet 不匹配 `datasetType`
- 年龄范围无法识别
- `standard_dept_hint` 无法映射
- 科室自动映射命中多个能力但未完成人工确认
