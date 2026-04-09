# triage-copilot 升级说明

## 第一阶段目标

本阶段已将项目从“症状直出科室”的原型，升级为“疾病中枢 + 人群约束 + 医学能力目录 + 本地科室映射 + AI解释”的结构化骨架。

已完成内容：

- 新增 `com.example.triage` 分层结构，覆盖 `controller`、`application`、`domain`、`infrastructure`
- 新增结构化分诊接口 `POST /api/triage/assess`
- 新增基础数据接口
  - `POST /api/base-data/import`
  - `GET /api/base-data/check`
- 落地第一版核心表结构与初始化样例数据
- 实现疾病候选识别、人群过滤、医学能力召回、本地科室映射、解释生成主链路
- 保留旧 `POST /api/triage/analyze`，但已切换到新结构化链路做兼容

## 本次改造路径

采用了“新骨架并行落地，旧入口逐步兼容”的方式：

1. 保留原 Spring Boot 启动结构，扩大扫描范围到 `com.example`
2. 新增结构化分诊域模型和应用服务
3. 用 `JdbcTemplate` 快速落地第一阶段核心数据底座
4. 用初始化脚本提供首批复杂场景样例
5. 通过测试先保证主链路和导入接口可运行

## 已知限制

- 疾病候选识别当前以规则和关键词为主，尚未接入更强的 AI 辅助召回
- 基础数据导入目前支持 `CSV/XLSX`，但字段模板仍偏首版
- 导入后的“医学能力映射”待人工复核，目前通过 `import_review_item` 承载
- 医生推荐扩展位尚未落地
- 向量库、RAG、复杂 Agent 编排、多轮长期记忆暂未纳入本阶段

## 推荐后续提交拆分

- `feat: redesign domain model for disease and capability`
- `feat: add base data import and initialization`
- `feat: implement structured triage decision flow`
- `test: add triage and population constraint tests`
- `docs: add upgrade and decision flow documentation`
