# 武汉数据导入说明

## 目标

本轮导入能力用于把武汉真实疾病数据和武汉本地医院科室数据接入到当前结构化分诊底座中，并在导入阶段完成首版治理。

## 支持的数据集类型

- `wuhan_disease`
- `wuhan_department`

## 支持的武汉疾病列名

当前优先兼容以下列名：

- `疾病名称`
- `标准病种`
- `病种名称`
- `诊断名称`
- `疾病编码`
- `病种编码`
- `别名`
- `疾病别名`
- `同义词`
- `症状关键词`
- `常见症状`
- `症状`
- `适用性别`
- `性别规则`
- `年龄范围`
- `最小年龄`
- `最大年龄`
- `紧急程度`
- `建议科室`
- `标准科室`

## 支持的武汉本地科室列名

当前优先兼容以下列名：

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
- `简介`
- `intro_text`
- `诊疗范围`
- `服务范围`
- `specialty_text`
- `适用性别`
- `性别规则`
- `年龄范围`
- `最小年龄`
- `最大年龄`
- `人群标签`

同时兼容部分来自 `schema_whdz.json` 的字段命名，例如：

- `name_raw`
- `name_norm`
- `service_unit_name`

## 导入治理规则

### 疾病治理

- 清洗疾病名称
- 生成或归一 `disease_code`
- 拆分别名
- 拆分症状关键词
- 归一性别规则
- 解析年龄范围
- 通过标准科室 hint 映射到 capability

### 科室治理

- 清洗医院名称
- 清洗科室名称
- 提取父级科室
- 科室简介入库
- 诊疗范围入库
- 默认城市补成 `武汉`
- 使用 capability 别名字典进行自动映射

## 自动映射规则

当前字典已优先支持以下能力：

- 儿科
- 儿童发热门诊
- 老年病科
- 记忆门诊 / 记忆障碍门诊 / 认知门诊
- 妇科
- 男科
- 排尿异常门诊 / 前列腺门诊
- 骨科
- 脊柱外科 / 脊柱门诊
- 脊柱疼痛门诊 / 腰腿痛门诊
- 器官移植随访门诊 / 移植门诊 / 移植复查门诊

## review 生成规则

### 疾病侧

以下情况会生成 review：

- `MISSING_DISEASE_NAME`
- `MISSING_SYMPTOM_KEYWORDS`
- `UNRECOGNIZED_AGE_RANGE`
- `UNRECOGNIZED_GENDER_RULE`
- `MISSING_STANDARD_DEPT_HINT`
- `UNMAPPED_STANDARD_DEPT_HINT`
- `AMBIGUOUS_STANDARD_DEPT_HINT`

### 科室侧

以下情况会生成 review：

- `WAIT_CAPABILITY_MAPPING`
- `AUTO_MAPPING_NEEDS_REVIEW`
- `PARENT_DEPARTMENT_CONFLICT`
- `SERVICE_SCOPE_CONFLICT`
- `UNRECOGNIZED_AGE_RANGE`

## 导入结果统计

当前导入任务支持输出：

- 成功数
- 失败数
- review 数
- auto-mapped 数
- review 类型分布
- 常见失败原因分布

可通过以下接口查看：

- `GET /api/base-data/jobs`
- `GET /api/base-data/jobs/detail?jobId=...`

## 常见失败原因

- 缺失疾病名称
- 缺失医院名称
- 缺失科室名称
- Excel 或 CSV 表头不在适配范围内
- 年龄范围格式异常

## 当前限制

- 武汉真实源文件尚未直接随仓库提交，需要在本地通过导入接口接入
- 自动映射仅是首版规则，不等价于最终人工确认结果
- 疾病 hint 到 capability 仍依赖首版字典，后续可继续扩充
