# 文档知识库管理系统 + AI RAG 问答

一个面向 JavaEE 课程实践的文档型知识库与 AI 问答系统。系统支持用户注册登录、上传 PDF/Word/Markdown 等文档、调用 MarkItDown 转换为 Markdown、自动切分文档片段、管理文档状态，以及基于文档切片召回的 DeepSeek Chat 问答。

## 功能概览

- 用户认证：支持注册、登录、退出，使用 Session 保存登录状态，登录页带图形验证码。
- 文档导入：支持上传 PDF、DOC、DOCX、PPT、PPTX、XLS、XLSX、HTML、MD、TXT。
- 知识分类：根据文件名关键词识别“大模型、Java基础、Spring、MySQL”等知识分类。
- MarkItDown 转换：后台调用外部 Python 虚拟环境中的 `markitdown` 工具，将文件转换为 Markdown。
- 文档切分：对 Markdown 做 Unicode 归一化、噪声清理，再按标题/问题行和长度切分为知识片段。
- 文档管理：支持按文件名和状态查询文档、按关键词查询活动切片、查看切片、归档、恢复和永久删除；归档文档不参与 AI 检索。
- AI 问答：先让大模型改写用户问题，再通过 MySQL LIKE + 应用层打分召回 top3 切片，最后注入 Prompt 生成回答。
- 数据统计：首页展示知识库概览和最近文档，详细统计页展示题库分类及活动切片占比饼图、各知识分类切片数量柱形图。

## 技术栈

- Java 17
- Spring Boot 3.2.4
- JSP + Bootstrap 5 + jQuery + ECharts（仅详细统计页使用）
- MyBatis-Plus
- MySQL 8
- LangChain4j + DeepSeek OpenAI 兼容接口
- Microsoft MarkItDown（外部 Python 环境）
- Maven

## 目录结构

```text
src/main/java/com/rjgc/nzy
├── common        # 通用响应结构
├── config        # MVC、拦截器、MyBatis-Plus、AI、MarkItDown 配置
├── controller    # 页面和接口控制器
├── dto           # 请求 DTO
├── entity        # User、KnowledgeDocument、KnowledgeChunk
├── mapper        # MyBatis-Plus Mapper
└── service       # 用户、验证码、文档转换、切片、检索、AI 问答

src/main/resources
├── application.yml
└── schema.sql

src/main/webapp/WEB-INF/jsp
├── knowledge-add.jsp   # 文档上传
├── knowledge-list.jsp  # 文档管理和切片查看
├── ai-qa.jsp           # AI 问答
└── stats.jsp           # 数据统计
```

## 环境要求

本地需要准备：

- JDK 17
- Maven 3.8+
- MySQL 8+
- Python 虚拟环境中已安装 MarkItDown
- DeepSeek API Key（如果需要真实 AI 改写和回答）

默认数据库名为 `knowledge_base_db`。`application.yml` 已开启 `createDatabaseIfNotExist=true`，数据库账号需要有建库和建表权限。

## 隐私配置

不要把数据库密码、API Key 或本机私有路径写死到仓库。项目通过环境变量读取敏感配置：

| 变量名 | 说明 | 默认值 |
| --- | --- | --- |
| `DB_URL` | MySQL JDBC 地址 | `jdbc:mysql://localhost:3306/knowledge_base_db?...` |
| `DB_USERNAME` | MySQL 用户名 | `root` |
| `DB_PASSWORD` | MySQL 密码 | 空 |
| `DEEPSEEK_API_KEY` | DeepSeek API Key | 空 |
| `MARKITDOWN_PYTHON` | MarkItDown 所在 Python 解释器 | `E:/Develop/tools/.venv/Scripts/python.exe` |
| `MARKITDOWN_UPLOAD_DIR` | 上传临时目录 | `target/markitdown-uploads` |

可以参考 `.env.example` 准备本机配置。`.env` 和 `.env.*` 已被 `.gitignore` 排除，不要提交真实密钥文件。

Windows PowerShell 临时配置示例：

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的MySQL密码"
$env:DEEPSEEK_API_KEY="你的DeepSeek API Key"
$env:MARKITDOWN_PYTHON="E:\Develop\tools\.venv\Scripts\python.exe"
```

## MarkItDown 准备

本项目不把 MarkItDown 源码或 `.venv` 提交到仓库，而是通过外部工具路径调用。当前开发机可使用：

```powershell
E:\Develop\tools\.venv\Scripts\python.exe -m markitdown --help
```

后台导入时会保存上传文件到受控临时目录，再执行：

```powershell
$env:MARKITDOWN_PYTHON -m markitdown <uploaded-file>
```

为安全起见，系统只处理用户上传到受控目录的文件，不接受任意本地路径或 URL 作为转换输入。

## 启动项目

1. 启动 MySQL。
2. 配置数据库和 MarkItDown 环境变量。
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

## 数据库模型

系统保留 `user` 表用于登录认证，知识库改为两张文档型表：

- `knowledge_document`：记录原始文件名、扩展名、文件大小、知识分类、导入状态、切片数量、错误信息和时间戳。
- `knowledge_chunk`：记录文档切片，包含文档 ID、切片序号、标题路径、正文内容、内容哈希、估算 token 和状态。

旧版 `knowledge_atom` 表会在 `schema.sql` 中被删除，不再作为运行时知识库。

## 文档切分策略

1. MarkItDown 先将上传文件转换为 Markdown。
2. 后端根据文件名关键词识别知识分类，例如 AI 大模型题库会归入“大模型”。
3. 后端执行 Unicode NFKC 归一化，清理常见页眉页脚和广告行。
4. 优先按 Markdown 标题或问题行切分。
5. 超长片段按目标长度继续滑窗切分，保留少量 overlap，避免上下文断裂。

默认配置：

- `chunk-target-chars`: `1000`
- `chunk-overlap-chars`: `150`
- `max-markdown-chars`: `300000`
- `timeout-seconds`: `60`

## AI 问答说明

AI 问答流程：

1. 用户提交问题。
2. 如果 DeepSeek 可用，先将问题改写为 2-3 个适合检索的查询；不可用则直接使用原问题。
3. 后端用 MySQL LIKE 检索 `knowledge_chunk` 的标题和正文，并在 Java 层计算匹配分。
4. 取 top3 切片和所属知识分类注入 Prompt，要求模型只基于召回资料回答。
5. 如果 `DEEPSEEK_API_KEY` 未配置，接口返回配置提示，并附带当前检索到的切片内容。

## 常用验证

```powershell
mvn test
mvn package
```

当前测试覆盖：

- Markdown 清理、问题边界识别和长文档切分。
- 文档导入时转换、切分、切片入库和状态更新。
- 文档知识分类识别和分类统计。
- 文档管理页的文件查询、切片查询和文档删除。
- 文档切片检索中文内容返回和打分。
- AI 未配置时的降级响应。
- 用户验证码校验。

## 注意事项

- 不要提交 `.env`、真实数据库密码、真实 API Key。
- 不要把 MarkItDown `.venv` 搬入仓库，使用 `MARKITDOWN_PYTHON` 指向本机或服务器的 Python 解释器。
- 如果使用代理访问 DeepSeek，确认 Java 进程也能通过代理访问 `api.deepseek.com`。
- 归档文档不会删除数据，只会将文档和切片状态改为 `ARCHIVED`，并从 AI 检索中排除；已归档文档可在管理页执行永久删除。
