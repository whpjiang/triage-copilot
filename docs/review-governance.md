# review 治理说明

## 目的

review 机制用于承接“可以自动导入但不能自动确认正确性”的数据项，避免为了提高导入通过率而强行落库成唯一正确结果。

## review 类型说明

### 疾病类

- `MISSING_DISEASE_NAME`
  该行缺少疾病名称，通常需要修原始源文件后重导
- `MISSING_SYMPTOM_KEYWORDS`
  疾病已入库，但需要补症状关键词
- `UNRECOGNIZED_AGE_RANGE`
  年龄范围无法可靠解析
- `UNRECOGNIZED_GENDER_RULE`
  性别规则无法可靠识别
- `MISSING_STANDARD_DEPT_HINT`
  缺少标准科室或医学能力线索
- `UNMAPPED_STANDARD_DEPT_HINT`
  有 hint 但没能映射到能力
- `AMBIGUOUS_STANDARD_DEPT_HINT`
  一个 hint 命中了多个能力

### 科室类

- `WAIT_CAPABILITY_MAPPING`
  当前规则完全无法自动映射
- `AUTO_MAPPING_NEEDS_REVIEW`
  自动命中了多个能力，需要人工确认
- `PARENT_DEPARTMENT_CONFLICT`
  父级科室与当前科室推断能力冲突
- `SERVICE_SCOPE_CONFLICT`
  科室名称与诊疗范围推断能力冲突
- `UNRECOGNIZED_AGE_RANGE`
  科室年龄范围不清晰

## 人工复核建议流程

1. 先按 `jobId` 查看单次导入任务详情
2. 看 `reviewTypeDistribution` 判断本次主要问题集中在哪一类
3. 对疾病 review 优先处理：
   缺失疾病名称、年龄范围异常、性别规则异常
4. 对科室 review 优先处理：
   无法映射、多个能力同时命中、父级冲突
5. 修源数据后重导，或通过确认接口关闭已明确项

## 哪些 review 后续可以自动关闭

以下类型后续适合在规则增强后自动关闭：

- `MISSING_SYMPTOM_KEYWORDS`
- `UNMAPPED_STANDARD_DEPT_HINT`
- `WAIT_CAPABILITY_MAPPING`

## 哪些 review 必须人工确认

以下类型不建议直接自动关闭：

- `AMBIGUOUS_STANDARD_DEPT_HINT`
- `AUTO_MAPPING_NEEDS_REVIEW`
- `PARENT_DEPARTMENT_CONFLICT`
- `SERVICE_SCOPE_CONFLICT`
- `UNRECOGNIZED_GENDER_RULE`
- `UNRECOGNIZED_AGE_RANGE`

## 当前接口

- `GET /api/base-data/reviews`
- `POST /api/base-data/reviews/resolve`

## 治理原则

- 不为了提升导入通过率而跳过 review
- 不把无法判断的映射硬写成唯一正确答案
- 不让 AI 替代年龄、性别和能力映射规则
- 所有高风险和高歧义场景优先保守处理
