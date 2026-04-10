# triage-copilot

`triage-copilot` 是一个基于 Java + Spring Boot + Spring AI 的结构化分诊项目，当前重点已经从“原型架构重构”进入“武汉真实数据导入与治理”阶段。

当前主链路为：

用户输入 -> 人群标准化 -> 疾病候选识别 -> 医学能力召回 -> 本地科室映射 -> 医生扩展推荐 -> AI 解释生成

## 技术栈

- Java 25
- Spring Boot 3.5.9
- Spring AI 1.1.0
- MyBatis-Plus 3.5.7
- MySQL 8.x
- H2 test database

## 安全配置

仓库不包含真实数据库密码和 API Key。

运行前请设置环境变量：

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DASHSCOPE_API_KEY`

示例：

```powershell
$env:DB_URL="jdbc:mysql://127.0.0.1:3306/triage_copilot?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-password"
$env:DASHSCOPE_API_KEY="your-api-key"
mvn spring-boot:run
```

## 本地启动

1. 准备 MySQL 8 数据库。
2. 执行 [src/main/resources/sql/init.sql](src/main/resources/sql/init.sql) 初始化表结构和结构示例数据。
3. 设置环境变量。
4. 运行 `mvn spring-boot:run`。
5. 访问 `http://localhost:8080`。

测试环境会自动加载 `init.sql` 到 H2。

## 分诊接口

### `POST /api/triage/assess`

请求示例：

```json
{
  "symptoms": "腰腿痛伴右下肢麻木两周",
  "gender": "male",
  "age": 46,
  "city": "武汉"
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

## 基础数据接口

- `POST /api/base-data/import`
- `GET /api/base-data/check`
- `GET /api/base-data/template`
- `GET /api/base-data/jobs`
- `GET /api/base-data/jobs/detail`
- `GET /api/base-data/reviews`
- `POST /api/base-data/reviews/resolve`

## 武汉真实数据导入

当前支持：

- `datasetType=disease`
- `datasetType=department`
- `datasetType=wuhan_disease`
- `datasetType=wuhan_department`

武汉导入能力支持：

- 中文表头适配
- 医院和科室名称清洗
- 症状关键词拆分
- 年龄范围解析
- 性别规则归一
- 标准科室 hint 到 capability 映射
- 本地科室 capability 自动映射
- review 生成
- 导入任务统计

导入结果当前可返回：

- 成功数
- 失败数
- review 数
- auto-mapped 数
- review 类型分布
- 常见失败原因分布

## 高风险 AI 保护

AI 疾病补召回只做辅助，不主导最终决策。

命中高风险症状时会直接跳过 AI 补召回，并写入审计日志。当前重点覆盖：

- 胸痛、胸闷、窒息感、呼吸急促
- 昏厥、抽搐、偏瘫、言语不清
- 呕血、黑便、大出血、剧烈腹痛
- 高热不退、精神差、嗜睡、喂养困难
- 孕妇腹痛、阴道流血、胎动异常
- 突发头痛、一侧肢体无力、视物不清、意识障碍

## 年龄分层

当前统一规则：

- `child`: 0-11
- `adolescent`: 12-17
- `adult`: 18-64
- `elderly`: 65+

## 文档

- [docs/upgrade-plan.md](docs/upgrade-plan.md)
- [docs/data-model.md](docs/data-model.md)
- [docs/triage-decision-flow.md](docs/triage-decision-flow.md)
- [docs/wuhan-data-import.md](docs/wuhan-data-import.md)
- [docs/review-governance.md](docs/review-governance.md)
