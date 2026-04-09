# Triage Copilot

基于 Java + Spring Boot + Spring AI + MyBatis-Plus 的分诊辅助项目。

## 技术栈

- Java 25
- Spring Boot 3.5.9
- Spring AI 1.1.0
- Spring AI Alibaba 1.1.0.0-RC2
- MyBatis-Plus 3.5.7
- MySQL 8.x
- DashScope `qwen-max`

## 安全配置

仓库已改为不包含任何真实数据库连接和 API Key。

应用通过环境变量读取敏感配置：

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DASHSCOPE_API_KEY`

可参考根目录下的 `.env.example` 作为本地配置模板，但不要把真实值提交到 GitHub。

PowerShell 示例：

```powershell
$env:DB_URL="jdbc:mysql://127.0.0.1:3306/triage_copilot?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-password"
$env:DASHSCOPE_API_KEY="your-api-key"
mvn spring-boot:run
```

## 本地运行

1. 准备 MySQL 8 数据库。
2. 执行 `src/main/resources/sql/init.sql` 初始化脚本。
3. 设置上述环境变量。
4. 运行 `mvn spring-boot:run`。
5. 访问 `http://localhost:8080/chat`。

## 提醒

如果这些密钥之前已经推送到公开 GitHub 历史里，仅仅修改当前文件还不够。
请尽快轮换你原来的数据库密码和 DashScope API Key，并在必要时清理 Git 提交历史。
