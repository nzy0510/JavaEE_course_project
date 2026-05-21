# 简易知识库管理系统 + AI 知识问答

一个面向 JavaEE 课程实践的知识库维护与 AI 知识问答系统。系统支持用户注册登录、知识原子维护、JSON 批量导入、归档恢复、统计看板，以及基于知识库检索结果的 DeepSeek Chat 问答。

## 功能概览

- 用户认证：支持注册、登录、退出，使用 Session 保存登录状态。
- 知识原子维护：支持单条新增、编辑、查询、归档和恢复。
- 批量导入：支持上传 JSON 文件导入知识原子。
- 数据统计：首页和统计页展示知识总量、分类分布、难度分布和状态占比。
- AI 知识问答：先从知识库检索相关知识原子，再调用 DeepSeek Chat 生成中文回答。
- 启动初始化：首次启动时从 `src/main/resources/knowledge_base/` 加载预置知识数据。

## 技术栈

- Java 17
- Spring Boot 3.2.4
- JSP + Bootstrap 5 + jQuery + ECharts
- MyBatis-Plus
- MySQL 8
- LangChain4j + DeepSeek OpenAI 兼容接口
- Maven

## 目录结构

```text
src/main/java/com/rjgc/nzy
├── common        # 通用响应结构
├── config        # MVC、拦截器、MyBatis-Plus、AI 配置
├── controller    # 页面和接口控制器
├── dto           # 请求 DTO
├── entity        # 数据库实体
├── mapper        # MyBatis-Plus Mapper
└── service       # 用户、知识库、AI 问答业务逻辑

src/main/resources
├── application.yml
├── schema.sql
└── knowledge_base/

src/main/webapp/WEB-INF/jsp
```

## 环境要求

本地需要准备：

- JDK 17
- Maven 3.8+
- MySQL 8+
- DeepSeek API Key（如果需要真实 AI 回答）

默认数据库名为 `knowledge_base_db`。`application.yml` 已开启 `createDatabaseIfNotExist=true`，数据库账号需要有建库和建表权限。

## 隐私配置

不要把数据库密码和 API Key 写死到仓库。项目通过环境变量读取敏感配置：

| 变量名 | 说明 | 默认值 |
| --- | --- | --- |
| `DB_URL` | MySQL JDBC 地址 | `jdbc:mysql://localhost:3306/knowledge_base_db?...` |
| `DB_USERNAME` | MySQL 用户名 | `root` |
| `DB_PASSWORD` | MySQL 密码 | 空 |
| `DEEPSEEK_API_KEY` | DeepSeek API Key | 空 |

可以参考 `.env.example` 准备本机配置。`.env` 和 `.env.*` 已被 `.gitignore` 排除，不要提交真实密钥文件。

Windows PowerShell 临时配置示例：

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的MySQL密码"
$env:DEEPSEEK_API_KEY="你的DeepSeek API Key"
```

Windows 用户级永久配置示例：

```powershell
setx DB_USERNAME "root"
setx DB_PASSWORD "你的MySQL密码"
setx DEEPSEEK_API_KEY "你的DeepSeek API Key"
```

设置永久环境变量后，需要重新打开终端或重启 IDE 才能读取到新值。

## 启动项目

1. 启动 MySQL。
2. 配置环境变量。
3. 在项目根目录执行：

```powershell
mvn spring-boot:run
```

启动成功后访问：

```text
http://localhost:8080/login
```

## 打包运行

项目打包为可执行 WAR：

```powershell
mvn clean package
java -jar target/knowledge-base-system-1.0.0-SNAPSHOT.war
```

## 批量导入 JSON 格式

批量导入支持三种顶层结构。

数组格式：

```json
[
  {
    "subject": "什么是依赖注入？",
    "category": "Spring",
    "difficulty": "中等",
    "tags": ["Spring", "DI"],
    "principles": "依赖注入由容器负责创建和注入对象依赖。",
    "pitfalls": "不要把依赖注入理解为对象自己主动创建依赖。"
  }
]
```

单个对象格式：

```json
{
  "subject": "什么是 IoC？",
  "category": "Spring",
  "difficulty": "中等",
  "tags": ["Spring", "IoC"],
  "principles": "控制反转将对象创建和依赖管理交给容器。",
  "pitfalls": ""
}
```

包裹数组格式：

```json
{
  "atoms": [
    {
      "subject": "Spring Bean 生命周期",
      "category": "Spring",
      "principles": "Bean 生命周期包括实例化、属性填充、初始化和销毁。"
    }
  ]
}
```

`tags` 既可以写成 JSON 数组，也可以写成字符串形式的 JSON 数组，例如 `"[\"Spring\",\"DI\"]"`。

## AI 问答说明

AI 问答流程：

1. 根据用户问题从 ACTIVE 状态的知识原子中做关键词检索。
2. 拼接检索上下文。
3. 调用 DeepSeek Chat 接口生成回答。

如果 `DEEPSEEK_API_KEY` 未配置，或当前网络无法访问 DeepSeek API，接口会返回明确提示，并附带已检索到的知识库内容，避免页面长时间卡住或直接 500。

## 常用验证

```powershell
mvn test
mvn package
```

当前测试覆盖：

- 知识原子必填校验和标签 JSON 校验。
- AI 未配置或调用失败时的兜底响应。
- 批量导入数组、单对象和包裹对象格式。

## 注意事项

- 不要提交 `.env`、真实数据库密码、真实 API Key。
- 如果使用代理访问 DeepSeek，确认 Java 进程也能通过代理访问 `api.deepseek.com`。
- 归档知识原子不会删除数据，只会将状态改为 `ARCHIVED`，并从默认搜索和 AI 检索中排除。
