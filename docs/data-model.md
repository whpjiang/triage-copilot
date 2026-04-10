# 数据模型说明

## 核心表

### `disease_master`

疾病标准主表，用于承载：

- 标准疾病编码
- 标准疾病名称
- 别名集合
- 症状关键词
- 性别规则
- 年龄边界
- 紧急程度
- 审核状态

### `disease_alias`

疾病别名表，用于承载俗称、同义词和导入时拆分出的别名。

### `medical_capability_catalog`

医学能力目录，不直接等价于本地医院科室。

当前支持：

- `STANDARD_DEPT`
- `SUBSPECIALTY`
- `SPECIAL_POPULATION`
- `SPECIAL_PATHWAY`

关键字段：

- `parent_code`
- `standard_dept_code`
- `gender_rule`
- `age_min`
- `age_max`
- `crowd_tags_json`
- `pathway_tags_json`

### `disease_capability_rel`

疾病到医学能力的结构化关系表。

用于表达：

- 主能力
- 次能力
- 优先级
- 备注

### `hospital`

本地医院主表。

### `hospital_department`

本地真实科室表，是最终推荐落点。

当前支持：

- 性别规则
- 最小年龄
- 最大年龄
- 人群标签

### `department_capability_rel`

本地科室到医学能力的映射表。

用于表达：

- 支持级别
- 权重
- 来源

来源可区分：

- `seed-example`
- `import-auto-rule`
- `wuhan-auto-rule`
- 后续人工确认来源

### `doctor_profile`

医生扩展位主表。

### `doctor_capability_rel`

医生到医学能力映射表。

## 导入治理相关表

### `import_job_record`

记录导入任务的状态、成功数、失败数、复核数和摘要信息。

### `import_failure_log`

记录单行导入失败原因，便于排查源数据问题。

### `import_review_item`

记录待人工复核项。

当前典型问题类型：

- `MISSING_SYMPTOM_KEYWORDS`
- `MISSING_STANDARD_DEPT_HINT`
- `WAIT_CAPABILITY_MAPPING`
- `AUTO_MAPPING_NEEDS_REVIEW`

## AI 审计表

### `ai_recall_audit_log`

用于记录 AI 疾病补召回行为。

当前记录内容包括：

- 症状文本
- 性别
- 年龄
- 年龄分层
- 可选疾病数量
- 规则命中的 disease code
- AI 建议的 disease code
- 状态
- 审计消息

当前状态包括：

- `SKIPPED_EMPTY`
- `SKIPPED_HIGH_RISK`
- `SKIPPED_NO_MODEL`
- `SUGGESTED`
- `FAILED`

## 年龄分层统一规则

当前全系统统一使用：

- `child`: 0-11
- `adolescent`: 12-17
- `adult`: 18-64
- `elderly`: 65+

这套边界会同时作用于：

- 用户画像
- 疾病
- 医学能力
- 本地科室
- 医生推荐

## 结构示例数据说明

初始化脚本中的医院、科室和医生数据已改为中性结构示例，用于：

- 跑通接口
- 验证复杂场景
- 支撑集成测试

真实城市数据建议统一通过导入接口接入。
