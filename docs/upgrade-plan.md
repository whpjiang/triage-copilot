# triage-copilot 升级说明

## 第一阶段目标

第一阶段已经把项目从“症状直接输出科室”的原型，升级成“疾病中枢 + 人群约束 + 医学能力目录 + 本地科室映射 + AI 解释”的结构化骨架。

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

第二阶段补充了复杂场景路径和亚专科能力：

- 新增路径标签推断，显式支持
  - `child_fever_pathway`
  - `elderly_multisymptom_pathway`
  - `female_pelvic_pathway`
  - `male_urinary_pathway`
  - `spine_pathway`
  - `transplant_followup`
- 细化能力目录，补充
  - 儿童发热门诊
  - 记忆障碍门诊
  - 男性排尿异常门诊
  - 脊柱疼痛专病门诊
- 能力召回从“疾病关系 + 人群约束”扩展为“疾病关系 + 人群约束 + 路径标签加权”
- 响应结构新增 `pathwayTags`

## 第三阶段增强

第三阶段补充了医生推荐扩展位：

- 在本地科室推荐之后新增医生候选排序
- 新增 `doctor_profile` 与 `doctor_capability_rel` 样例数据
- 响应结构新增 `doctorRecommendations`
- 医生推荐遵循以下原则
  - 不绕开结构化分诊链路
  - 必须依附已命中的本地科室和医学能力
  - 继续受年龄、性别和特殊人群约束
  - 只作为扩展推荐位，不替代科室推荐

## 第四阶段增强

第四阶段开始补充 AI 辅助疾病候选补召回：

- 在 `DiseaseCandidateService` 的规则识别之后新增 AI 补充召回
- AI 只允许从已入库且已通过年龄/性别过滤的疾病集合中挑选候选
- AI 返回结果必须再次经过疾病编码白名单校验
- 规则命中结果仍然优先，AI 只做弱加权补充
- AI 不直接决定最终疾病标准化编码，也不改变后续能力、科室、医生映射逻辑

## 第五阶段增强

第五阶段补充导入模板和人工复核流程：

- 新增 `GET /api/base-data/template`，返回疾病或本地科室导入模板
- 新增 `GET /api/base-data/reviews`，返回待人工处理的复核清单
- 导入接口继续写入 `import_review_item`，并可按 `datasetType`、`jobId` 查看待处理项
- 让“导入数据 -> 自动入库 -> 人工补映射”这条链路更完整

## 第六阶段增强

第六阶段补充人工复核状态流转：

- 新增 `POST /api/base-data/reviews/resolve`，支持关闭待复核项
- 关闭复核项后，`GET /api/base-data/reviews` 与 `GET /api/base-data/check` 的待处理数量会同步减少
- 让导入后的人工处理流程形成“生成待复核项 -> 查看 -> 关闭”的闭环

## 第七阶段增强

第七阶段补充导入任务追踪：

- 新增 `GET /api/base-data/jobs`，查看最近导入任务列表
- 新增 `GET /api/base-data/jobs/detail`，查看单次导入的成功数、失败数、待复核数和失败日志
- 让“导入执行 -> 失败排查 -> 复核处理”具备完整追踪入口

## 改造路径

采用了“新骨架并行落地，旧入口逐步兼容”的方式：

1. 保留原 Spring Boot 启动结构，扩大扫描范围到 `com.example`
2. 新增结构化分诊领域模型和应用服务
3. 用 `JdbcTemplate` 快速落地核心数据底座
4. 用初始化脚本提供复杂场景样例
5. 通过测试先保证主链路和导入接口可运行
6. 在稳定骨架上逐步补充路径标签、医生推荐、AI 辅助召回

## 已知限制

- 当前 AI 补召回仍是轻量版，主要用于复杂表述的候选补充，不替代规则识别
- 基础数据导入目前支持 `CSV/XLSX`，但字段模板仍偏首版
- 导入后的“医学能力映射”仍保留人工复核位，通过 `import_review_item` 承载
- 向量库、RAG、复杂 Agent 编排、多轮长期记忆暂未纳入当前阶段
