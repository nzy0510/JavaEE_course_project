## 项目简介

- 系统后台采用Spring Boot、MyBatis-Plus和MySQL实现，前端采用JSP、Bootstrap、jQuery和ECharts。
- 文档转换工具采用Microsoft MarkItDown，通过外部Python虚拟环境调用，不将工具源码或虚拟环境提交到项目仓库。
- AI问答部分基于DeepSeek Chat实现问题改写和答案生成，检索阶段不引入向量数据库，而是支持按知识分类限定范围，并使用MySQL LIKE与应用层打分完成Top3片段召回。

## 硬性规则

- 保护用户和其他 Agent 的未提交改动；不要回滚不是自己造成的改动。
- 不提交 `.env`、`application.yml`、密钥文件、私有部署文件、私有题库、临时导入包或本地视频产物。
- 不在日志或文档中暴露完整 API Key、access token、refresh token、密码或敏感请求头。
- 不使用 `git reset --hard`、`git checkout --` 等破坏性命令，除非用户明确要求。
- 完成 feature、bugfix、refactor、deployment 或用户可见代码变更后，按 `post-delivery-analysis` skill 自动输出交付后分析和下一步建议；不得自动执行下一步建议，必须等待用户明确指令。

## 基础工作流

- 如果发现用户已有未提交改动，必须区分“本次任务相关”和“用户/其他 Agent 的改动”，不要回滚或覆盖无关内容。
- 需求不清楚时，先追问关键问题；不要猜测实现。
- 如果存在多个方案，先推荐最适合当前项目结构的方案，并说明取舍。
- 优先小步修改，避免一次性大范围重构。

## 文档与交付

- 新增功能或用户可见行为变化时，用maintain-changelog skill维护更新日志文档。
- 功能交付后同步更新课程设计报告。

## 代码理解

- 当用户需要理解某段代码逻辑时，优先使用 `.codegraph/` 中的代码库知识图谱；如果图谱不足，再审查源码。
- agent也应优先采取`.codegraph/` 中的代码库知识图谱来理解项目架构。

## Git 规则

- 修改前必须确认分支和工作区状态。
- 可以在用户授权下自主 commit / push；最终 merge / release 前需要确认。
- 遇到 merge conflict 时，先说明冲突文件、冲突原因和建议方案，再处理。
- 提交前展示修改文件、commit message 和测试结果。
- commit message 使用 Conventional Commits，例如 `feat:`、`fix:`、`docs:`、`refactor:`、`test:`、`chore:`。
- 不使用 `git reset --hard`、`git checkout --` 等破坏性命令，除非用户明确要求。
- codex本地工作树创建失败时，优先查找原因，修复配置。