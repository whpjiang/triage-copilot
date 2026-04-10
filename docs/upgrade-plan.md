# triage-copilot 升级说明

## 第一阶段目标

第一阶段已经把项目从“症状直出科室”的原型升级成“疾病中枢 + 人群约束 + 医学能力目录 + 本地科室映射 + AI解释”的结构化骨架。

已完成内容：

- 新增 `com.example.triage` 分层结构，覆盖 `controller`、`application`、`domain`、`infrastructure`
- 新增结构化分诊接口 `POST /api/triage/assess`
- 新增基础数据接口
  - `POST /api/base-data/import`
  - `GET /api/base-data/check`
- 落地第一版核心表结构与初始化样例数据
- 实现疾病候选识别、人群过滤、医学能力召回、本地科室映射、解释生成主链路
- 保留旧 `POST /api/triage/analyze`，但已切换到新结构化链路做兼容

## 第二阶段增强

当前已继续补充第二阶段的 P1 能力：

- 新增路径标签推断，显式支持：
  - `child_fever_pathway`
  - `elderly_multisymptom_pathway`
  - `female_pelvic_pathway`
  - `male_urinary_pathway`
  - `spine_pathway`
  - `transplant_followup`
- 细化能力目录，补充：
  - 儿童发热门诊
  - 记忆障碍门诊
  - 男性排尿异常门诊
  - 脊柱疼痛专病门诊
- 将能力召回从“疾病关系 + 人群约束”扩展为“疾病关系 + 人群约束 + 路径标签加权”
- 响应结构新增 `pathwayTags`，使结果更可解释

## 第三阶段增强

当前已继续补充第三阶段的医生推荐扩展位：

- 在本地科室推荐之后新增医生候选排序
- 新增 `doctor_profile` 与 `doctor_capability_rel` 样例数据
- 响应结构新增 `doctorRecommendations`
- 医生推荐遵循以下原则：
  - 不单独绕开结构化分诊链路
  - 必须依附已命中的本地科室和医学能力
  - 继续受年龄、性别和特殊人群约束
  - 只作为扩展推荐位，不替代科室推荐

## 改造路径

采用了“新骨架并行落地，旧入口逐步兼容”的方式：

1. 保留原 Spring Boot 启动结构，扩大扫描范围到 `com.example`
2. 新增结构化分诊域模型和应用服务
3. 用 `JdbcTemplate` 快速落地核心数据底座
4. 用初始化脚本提供复杂场景样例
5. 通过测试先保证主链路和导入接口可运行

## 已知限制

- 疾病候选识别当前仍以规则和关键词为主，尚未接入更强的 AI 辅助召回
- 基础数据导入目前支持 `CSV/XLSX`，但字段模板仍偏首版
- 导入后的“医学能力映射”仍保留人工复核位，通过 `import_review_item` 承载
- 医生推荐扩展位尚未落地
- 向量库、RAG、复杂 Agent 编排、多轮长期记忆暂未纳入当前阶段
