# triage-copilot

`triage-copilot` 已从“症状直接喂给大模型，让模型猜挂号科室”的原型，升级为“疾病中枢 + 人群约束 + 医学能力目录 + 本地科室映射 + AI 解释”的结构化分诊系统。

当前版本的目标是先把数据骨架、规则主链路和导入治理搭稳，再逐步增强 AI 辅助能力。

## 技术栈

- Java 25
- Spring Boot 3.5.9
- Spring AI 1.1.0
- MyBatis-Plus 3.5.7
- MySQL 8.x
- H2（测试环境）

## 安全配置

仓库不再包含真实数据库连接和 API Key。

应用通过环境变量读取敏感配置：

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DASHSCOPE_API_KEY`

PowerShell 示例：

```powershell
$env:DB_URL="jdbc:mysql://127.0.0.1:3306/triage_copilot?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-password"
$env:DASHSCOPE_API_KEY="your-api-key"
mvn spring-boot:run
```

## 本地启动

1. 准备 MySQL 8 数据库。
2. 执行 [`src/main/resources/sql/init.sql`](/d:/Workspace/LearnAI/triage-copilot/src/main/resources/sql/init.sql) 初始化表结构和结构示例数据。
3. 设置环境变量。
4. 运行 `mvn spring-boot:run`。
5. 访问 `http://localhost:8080`.

测试环境使用 H2，并在启动时自动加载 `init.sql`。

## 核心接口

### 结构化分诊

`POST /api/triage/assess`

请求示例：

```json
{
  "symptoms": "腰痛伴右下肢麻木两周",
  "gender": "male",
  "age": 46
}
```

返回结构包含：

- `populationProfile`
- `pathwayTags`
- `candidateDiseases`
- `capabilityRecommendations`
- `departmentRecommendations`
- `doctorRecommendations`
- `explanation`

### 基础数据导入

- `POST /api/base-data/import`
- `GET /api/base-data/check`
- `GET /api/base-data/template`
- `GET /api/base-data/jobs`
- `GET /api/base-data/jobs/detail`
- `GET /api/base-data/reviews`
- `POST /api/base-data/reviews/resolve`

## 导入能力

当前支持：

- 通用疾病导入：`datasetType=disease`
- 通用本地科室导入：`datasetType=department`
- 武汉本地科室适配导入：`datasetType=wuhan_department`
- 武汉疾病字段适配导入：`datasetType=wuhan_disease`

导入过程中会自动完成：

- 疾病别名清洗
- 症状关键词归一
- 年龄分层初判
- 部分本地科室到医学能力的自动映射
- 导入任务记录、失败日志和待人工复核项生成

## 年龄分层规则

当前统一规则为：

- `child`: 0-11
- `adolescent`: 12-17
- `adult`: 18-64
- `elderly`: 65+

这套边界会同步作用于：

- 用户画像标准化
- 疾病过滤
- 医学能力过滤
- 本地科室过滤
- 医生推荐过滤

## AI 使用边界

AI 目前只负责两类事情：

- 疾病候选补召回
- 结果解释文本生成

AI 不直接决定：

- 最终疾病标准编码
- 年龄和性别强约束
- 医学能力最终排序
- 本地科室最终落点
- 高风险场景唯一决策

针对 AI 补召回，当前还加了两层保护：

- 仅能从已入库且已通过年龄/性别过滤的疾病白名单中补充
- 高风险症状会跳过 AI 补召回并写入审计日志

## 结构示例数据

初始化脚本中的医院和医生数据现在只用于结构演示，不再使用“上海 demo”命名。

后续对接真实城市数据时，建议通过导入接口导入，不直接改 seed 数据。

## 文档

- [`docs/upgrade-plan.md`](/d:/Workspace/LearnAI/triage-copilot/docs/upgrade-plan.md)
- [`docs/data-model.md`](/d:/Workspace/LearnAI/triage-copilot/docs/data-model.md)
- [`docs/triage-decision-flow.md`](/d:/Workspace/LearnAI/triage-copilot/docs/triage-decision-flow.md)
