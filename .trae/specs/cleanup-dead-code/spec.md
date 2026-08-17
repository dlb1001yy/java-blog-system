# 废弃无引用文件清理（cleanup-dead-code）Spec

## Why
项目经过多轮迭代后积累了脚手架遗留文件、一次性脚本、历史规划文档和零引用代码。这些死代码增加仓库体积、干扰阅读、甚至存在风险（如 `_merge_docs.py` 重跑会覆盖文档新增章节）。经全量引用分析，识别出一批确认删除后不影响服务运行的文件。

## What Changes
- 删除 blog-admin / blog-frontend / blog-app 中零引用的组件、样式、静态资源与 API 封装文件
- 删除 blog-admin / blog-frontend 中未使用的 npm 依赖（sass、highlight.js）
- 删除 blog-backend 中一次性调试工具类 PasswordGenerator.java
- 删除历史遗留的一次性产物：`_merge_docs.py`、`.codebuddy/plans/`、`blog-backend/.trae/documents/`
- 同步更新受影响的 README 中对已删除文件的描述
- **不删除**任何有活跃引用的代码（详见"保留清单"）

## Impact
- Affected specs: 无（纯清理，无功能变更）
- Affected code:
  - blog-admin：src/components/、src/assets/、src/style.css、package.json
  - blog-frontend：src/api/、src/assets/、src/style.css、package.json
  - blog-app：根 config.js、components/
  - blog-backend：src/test/ 下 1 个文件
  - 根目录：_merge_docs.py、.codebuddy/plans/、blog-backend/.trae/documents/

---

## 分析结论：可废弃清单（删除后不影响服务）

### A. blog-admin（管理后台，6 个文件 + 2 个依赖）
| 文件 | 证据 |
|---|---|
| `src/components/HelloWorld.vue` | Vite 脚手架遗留，全项目零引用 |
| `src/components/Editor.vue` | 零引用；ArticleEdit.vue 自行实现编辑器（直接 import markdown-it） |
| `src/components/SvgIcon.vue` | 零引用 |
| `src/assets/vite.svg`、`src/assets/vue.svg`、`src/assets/hero.png` | 仅被待删除的 HelloWorld.vue 引用 |
| `src/style.css` | main.js 只引入 tokens.css 和 global.css，此文件未被引入；内容为脚手架默认样式 |
| package.json `sass`（devDep） | 项目内无任何 .scss / lang="scss" |
| package.json `highlight.js` | 唯一使用处是待删除的 Editor.vue |

### B. blog-frontend（前台，5 个文件 + 1 个依赖）
| 文件 | 证据 |
|---|---|
| `src/api/auth.js` | 零引用；stores/user.js 是纯 localStorage 实现，login/logout 封装无人调用 |
| `src/assets/vite.svg`、`src/assets/vue.svg`、`src/assets/hero.png` | 零代码引用，仅 README 文字提及 |
| `src/style.css` | main.js:11-12 只引入 global.css 和 variables.css，未被引入 |
| package.json `sass`（devDep） | 同 blog-admin，无 .scss 文件 |

### C. blog-app（uni-app 移动端，2 个文件）
| 文件 | 证据 |
|---|---|
| 根目录 `config.js` | 空文件（0 字节）且零引用；真正生效的是 common/config.js |
| `components/Loading.vue` | 零引用（grep 仅命中 uni.showLoading API 调用） |

### D. blog-backend（后端，1 个文件）
| 文件 | 证据 |
|---|---|
| `src/test/java/com/dlbyy/blog/PasswordGenerator.java` | 仅 14 行的 main() 一次性调试工具，打印 BCrypt 哈希；无 @Test 注解、零引用 |

### E. 根目录历史遗留（一次性产物）
| 文件/目录 | 证据 |
|---|---|
| `_merge_docs.py` | 一次性文档合并脚本；数据源硬编码为仓库外路径 `D:\学习资料\...`；重跑会用旧内容覆盖合集文档新增的十一/十二章节（有破坏风险） |
| `.codebuddy/plans/`（10 个 .md） | CodeBuddy 工具的历史规划文档，任务全部 completed，无任何引用 |
| `blog-backend/.trae/documents/`（9 个 .md） | 建库初期脚手架规划文档，任务早已完成，无任何引用 |

### F. 可选项（需用户决策，默认不动）
| 项 | 说明 |
|---|---|
| `wiki/`（4 个 .md） | Gitee Wiki 推送源，内容全部是根目录文档的**过期快照**（如部署手册 757 行 vs 根目录 1192 行）。若继续使用 Gitee Wiki 应刷新而非删除；若已放弃可整体废弃 |
| `blog-backend/src/main/resources/mapper/*.xml`（10 个） | 全部为空模板（仅 namespace + 注释，无任何 SQL）。删除不影响运行，但保留作扩展占位亦无害 |
| `.trae/documents/`（根目录，30 个 .md） | 历史修复/任务计划文档。注意 README.md:487-489 记载了该目录结构，删除需同步改 README |
| `.gitignore` 失效规则 | `/升级文件/`、`/blog-ui/blog/`、Gradle/JRebel 区块等指向不存在的路径，可顺手清理（低优先级） |

---

## 分析结论：必须保留清单（易误判项，均有活跃引用）
| 文件 | 引用证据 |
|---|---|
| JwtUtils.java | AuthController、JwtAuthenticationFilter、JwtTokenProvider 三处使用 |
| JwtTokenProvider.java | AuthController 使用（虽与 JwtUtils 冗余分层，但非死代码，属后续重构候选） |
| SchemaMapper.java | DataInitializer 启动迁移使用 |
| FileUtils.java | AdminFileController、MarkdownImportService、CoverImageGenerator 三处使用（注释自述"旧版"，但未迁移前不可删） |
| MarkdownImportService.java | POST /admin/articles/import-markdown 端点，blog-admin 前端在用 |
| es/ElasticsearchIndexInitializer.java | @Component + 功能开关 blog.search.enabled 控制，属休眠组件非死代码 |
| utils 全部其余类（RedisUtils/CookieUtils/CoverImageGenerator） | 均有生产引用 |
| entity 全部 12 个实体 + mapper 全部 12 个接口 | 逐一核对均有使用 |
| sql/01-create_sql.sql、02-article_evolution.sql | docker-compose.yml:34 挂载至 /docker-entrypoint-initdb.d/ 首次建库自动执行 |
| blog-admin/src/views/Redirect.vue | router 注册且 TagsView.vue:82 实际使用 |
| blog-admin/src/api/ 全部 12 个文件 | 均被 views/stores 引用 |
| blog-app/uni.promisify.adaptor.js | uni-app Vue3 编译标配，构建时自动注入 |
| blog-app/common/ 全部 6 个文件 | 内部引用链环环相扣（env→signing→request→api） |
| scripts/ 三个脚本、docker/daemon.json、elasticsearch/、monitoring/ | 均被 README/部署手册/docker-compose.yml 引用 |

## ADDED Requirements
### Requirement: 死代码清理
系统在删除本 spec A-E 节所列文件后 SHALL 保持全部功能不变：后端编译测试通过、两个 Web 前端构建通过、Docker 部署流程不受影响。

#### Scenario: 删除后构建验证
- **WHEN** 删除 A-E 节全部文件并移除对应依赖后
- **THEN** `mvn test` 通过、`npm run build`（blog-admin 与 blog-frontend）成功、docker-compose.yml 引用的路径全部完好
