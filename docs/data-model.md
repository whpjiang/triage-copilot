# 数据模型说明

## 核心表

### `disease_master`

疾病主数据中心。

关键字段：

- `disease_code`: 结构化疾病编码
- `disease_name`: 标准疾病名
- `aliases_json`: 疾病别名集合
- `symptom_keywords`: 症状关键词集合
- `gender_rule` / `age_min` / `age_max`: 性别年龄约束
- `urgency_level`: 紧急程度
- `review_status`: 复核状态

### `disease_alias`

承载疾病别名、俗称、症状别名。

### `medical_capability_catalog`

标准医学能力目录，不直接等于本地医院科室。

可承载：

- 标准科室
- 亚专科
- 专病能力
- 特殊人群能力
- 特殊路径能力

关键字段：

- `capability_code`
- `capability_type`
- `standard_dept_code`
- `gender_rule` / `age_min` / `age_max`
- `crowd_tags_json`
- `pathway_tags_json`

### `disease_capability_rel`

疾病到医学能力的结构化映射表。

关键字段：

- `disease_code`
- `capability_code`
- `rel_type`
- `priority_score`
- `crowd_constraint`

### `hospital`

本地医院主表。

### `hospital_department`

本地真实科室表，是结构化分诊链路中的最终落点。

关键字段：

- `hospital_id`
- `department_name`
- `parent_department_name`
- `department_intro`
- `service_scope`
- `gender_rule` / `age_min` / `age_max`
- `crowd_tags_json`

### `department_capability_rel`

本地科室到医学能力的映射表。

关键字段：

- `department_id`
- `capability_code`
- `support_level`
- `weight`
- `source`

## 导入与复核表

### `import_job_record`

记录导入任务状态、成功数、失败数、复核数。

### `import_failure_log`

记录导入失败行和错误信息。

### `import_review_item`

记录待人工补充或复核的数据项。

## 当前样例能力覆盖

初始化样例已经显式覆盖：

- 儿科
- 儿童发热门诊
- 老年病科
- 记忆障碍门诊
- 妇科
- 男科
- 男性排尿异常门诊
- 脊柱外科
- 脊柱疼痛专病门诊
- 器官移植随访路径
