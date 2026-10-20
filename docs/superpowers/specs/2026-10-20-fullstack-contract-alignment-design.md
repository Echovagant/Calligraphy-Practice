# 在线书法练习平台 — 全栈契约对齐设计

日期：2026-10-20
状态：已获用户批准，待转实施计划
范围：`Calligraphy Practice/`（`Practice-Platform/` 前端 + `SpringBoot-Platform/` 后端）

---

## 1. 目标与验收定义

**目标**：把「前端 UI 完整、后端 HTTP 层空白」的项目，变成前后端真正联通的全栈应用。前端调用的每一个端点都真实存在、由后端实现、有契约测试锁定，页面上不再出现硬编码的假数据。

**验收定义**（全部满足才算完成）：

1. `mvn spring-boot:run` 能正常启动，Spring 上下文不报错。
2. `npm run build` 退出码 0。
3. 契约文件中的 53 个端点全部有实现，且有对应的契约测试覆盖。
4. 前端一致性脚本（§3.2）通过：前端方法集合与契约 operation 集合**双向零差异**。
5. 六个视图（Gallery / MyWorks / MyPractices / Community / Admin / Home）不再包含硬编码 mock 数组。
6. 前端 API 层无 mock 降级分支——后端出错时页面显示错误，不是假数据。

**非目标**（本期明确不做）：

- 智能书法评分、笔画比对、真书法字体渲染（§9 记为后续项）。
- 引入 Flyway / Liquibase 做 schema 版本化迁移。
- 把后端改写成 Node.js + Express（《项目计划书》2.2 节原本写的是 Express，但现有 16 个 JPA 实体、17 个 Repository、14 个 Service 实现已经落地，改写等于全部丢弃）。
- 引入 TypeScript 改造前端（当前 `src` 下 0 个 `.ts` 文件，`tsconfig.json` + `vue-tsc` 脚本是摆设）。
- 管理后台的用户管理 / 角色权限 / 通知 / 操作审计端点——前端没有对应调用，属额外需求。

---

## 2. 现状事实

全部结论来自文件读取与可复现命令，附证据。

### 2.1 硬阻断（必须先修，否则后续阶段无法验证）

| # | 事实 | 证据 |
|---|---|---|
| B1 | 前端生产构建失败 | `npm run build` 退出码 1；`[vite:vue] [vue/compiler-sfc] Identifier 'handleLike' has already been declared (330:6)` |
| B2 | B1 的根因：`handleLike` 重复声明 | `Practice-Platform/src/views/CommunityView.vue:344` 与 `:595` 各声明一次 `const handleLike` |
| B3 | 后端零生产 Controller | 全树仅 `test/SimpleApplication.java:15` 一个 `@RestController`（`GET /test`）；无 `controller` 包；`git log --diff-filter=A -- '*Controller*'` 无记录 |
| B4 | 后端启动即失败 | `AuthServiceImpl:42`、`AdminServiceImpl:34`、`UserServiceImpl:40` 注入 `PasswordEncoder`，全仓库无 `@Bean PasswordEncoder`；Spring Boot 3.2.5 不提供该自动配置 |
| B5 | 后端无安全过滤链 | 全仓库无 `SecurityFilterChain` / `@EnableWebSecurity` / `addInterceptors`；走 Boot 默认 `anyRequest().authenticated()` |
| B6 | 前端第二个 API 模块 import 崩溃 | `Practice-Platform/src/api/community.js:1` import `../utils/request`，该文件不存在（`src/utils/` 实际只有 `storage.js`/`index.js`/`constants.js`/`api.js`） |
| B7 | **后端完全无法编译** | `mvn -o compile` 实测失败，错误包括「未结束的字符串文字」「非法字符: '\ue178'」「需要 class、interface、enum 或 record」。根因是 §2.4 的编码污染吃掉了字符串的收尾引号、注释的换行与整条代码声明 |

### 2.2 契约自身矛盾（9 处）

前端契约内部互相打架，后端无法同时满足：

| # | 矛盾 | 位置 | 本设计裁决 |
|---|---|---|---|
| C1 | 端口：proxy 指向 8080，后端在 8083 | `vite.config.js:25-30` vs `application.properties:7` | proxy 改 `8083` |
| C2 | 前缀：`api.js` base 自带 `/api`，`community.js` 路径再带 `/api`，后端 context-path 为空 | `api.js:4` / `community.js` 全量 / `application.properties:9` | 后端 `@RequestMapping("/api")`，前端用相对 `/api` |
| C3 | 两套客户端风格：fetch 直接返回 body vs axios 风格 `{data}` | `api.js:144` vs `community.js:13` | 只保留 `api.js` 一套 |
| C4 | 解包三套并存 | `copybookStore.js:213` 用 `response.data.copybooks`；`practiceStore.js:195` 用 `response.map()`；`userStore.js:362` 用 `const {data}` | 统一：客户端返回 body，列表用 `response.content` |
| C5 | 3 个幽灵方法 | `practiceStore.js:84,250,287` 调 `copybookAPI.addPracticeRecord` / `getPracticeHistory` / `getPracticeStats`，`api.js` 中不存在 | 改调已存在的 `recordPractice` / `getUserStatistics`；缺的历史接口新增为 `GET /api/copybooks/practice/history` |
| C6 | `getUserWorks()` 无参调用 → 请求 `/works/user/undefined` | `practiceStore.js:268` | 前端取当前用户 id |
| C7 | `sendVerificationCode` 的 mock 返回 `code` 字段 | `api.js:453` | 后端在 SMTP 未配置时返回 `{success, message, code, devMode:true}`，code 为固定开发码 |
| C8 | `PracticeView` 存作品时图片丢失 | `PracticeView.vue:280-282` 传 `imageUrl`，`practiceStore.js:119-126` 只认 `File`/`imageDataUrl` | 前端改传 `imageDataUrl` |
| C9 | `fabric v6` API 错误：`canvas.sendToBack` 不存在（v6 为 `sendObjectToBack`），导致网格压在笔画之上 | `PracticeView.vue:235` | 改用 `sendObjectToBack` |

### 2.3 其他已确认缺陷

- `userStore.initUserFromStorage()`（`userStore.js:438`）全项目无调用点 → 刷新页面即掉登录态。
- `spring.sql.init.mode=always` + `recreate_schema.sql` 开头 `DROP TABLE` → **每次启动清空 users / works / comments / practice_records 四张表**。
- `practice_records.copybook_id` 为 `bigint`，而 `copybooks.id` 为 `char(36)` → 类型不匹配。
- 无 `Copybook` 实体，但 `copybooks` 表存在且有 3 行真实数据（2025-10-31 播种）。
- 三套 JWT 实现互不兼容：`security/JwtService`(HS512) / `service/impl/JwtServiceImpl`(HS256+Redis 黑名单) / `util/JwtTokenProvider`；密钥三处（`application.properties` / `constant.SecurityConstants.JWT_SECRET` / `config.JwtConfig` 的 `app.jwt.*`）。
- `JwtAuthenticationFilter` 为 `@Component`，order = `LOWEST_PRECEDENCE`，**晚于 security 过滤链**执行，填充的 SecurityContext 对鉴权判定无效。
- 两份完全相同的 `WebMvcConfigurer`（`config/WebConfig.java` 与 `config/WebMvcConfig.java`），`allowedOriginPatterns("*")` + `allowCredentials(true)`。
- 6 个 Service 接口无实现：`AuthenticationService`、`BackendUserService`、`PermissionService`、`PracticeRecordService`、`RoleService`、`UserActivityLogService`；4 个 Repository 从未被注入。
- `FileStorageServiceImpl` 约 60 个方法是一行桩（`return true;` / `List.of()`）；`JwtServiceImpl` 约 1/3 方法为空；`EmailServiceImpl` 1850 行中大部分是 HTML 模板字符串。
- `config/MailConfig` 硬编码 `smtp.example.com` 占位符，绕过 `spring.mail.*` 配置。
- `target/classes/*.class` 与根目录 `AnnotationCleanup.class` 被 git 跟踪；`app.log` 等日志文件在仓库里。
- 根目录 `FixEncoding.ps1` / `FixImports*.ps1` / `RemoveBom.ps1` 等一次性修复脚本与源码同仓。
- `stores/counter.js`（脚手架残留）、`stores/appStore.js`（全项目无人 import，内部全为假数据）。
- `utils/index.js` 与 `utils/storage.js` 各自导出一套 `getStorage/setStorage`（签名还不一致）；`userStore.js:8-36` 又内联第三套 sessionStorage 工具。
- `API_BASE_URL` 双份定义：`api.js:4` 与 `constants.js:8`。
- `Practice-Platform/index.html` 仍是 `<title>Vite App</title>`、`<html lang="">`。

### 2.4 编码污染（后端全面）

**后端源码存在系统性 UTF-8/GBK 乱码，前端完全干净。**

| 指标 | 数值 |
|---|---|
| 受损范围 | **仅 `SpringBoot-Platform/src/main/java`**。`src/main/resources`（含 `application.properties`、两个 `.sql`）与 `src/test` **实测完全干净** |
| 受损文件数 | **90** |
| 可逆 CJK 片段 | **751** 处 |
| 引号不配对的行 | **217** 行（字符串字面量丢失收尾引号） |
| 注释吞掉下一行代码 | **105** 行 |
| `U+FFFD`（不可逆丢失）出现次数 | **0** —— 变换可逆 |
| 前端受污染文件数 | **0** |

- 成因：源文件是 UTF-8，曾被按 GBK 解码后重新以 UTF-8 写回。这是**确定性字节变换**，因此可机械还原：`片段.encode('gbk').decode('utf-8')`。
- 影响范围包含**字符串字面量**，不只是注释。已确认的例子：
  - `dto/ApiResponse.java:21` —— 成功消息为 `鎿嶄綔鎴愬姛`，应为 `操作成功`。这是**每个 API 响应体都会带的字段**。
  - 多个 DTO 的 JSR-303 校验消息，如 `DiscussionUpdateDto` 的 `闀垮害涓嶈兘瓒呰繃` → `长度不能超过`、`FileUploadRequestDto` 的 `鏂囦欢绫诲瀷闀垮害涓嶈兘瓒呰繃` → `文件类型长度不能超过`。这些消息会**直接显示给前端用户**。
- 检测必须带**双向 CJK 合法性守卫**：裸用往返变换会对 `职位`、`状态` 这类正常中文产生假阳性（`职位` 的 GBK 字节恰好是合法 UTF-8，往返后得到 `ְλ`）。守卫条件为「往返成功 且 往返前后都只含 CJK 与常见标点」，可把假阳性降到 0。
- `config/JwtConfig.java:14` 是乱码造成的**真实代码缺陷**：`private String header = "Authorization";` 被吞进了上一行的 `//` 注释里，该字段实际不存在。

### 2.5 环境事实（已实测）

| 项 | 状态 |
|---|---|
| MySQL 8.0 @ 3306（root/123456） | 运行中；`calligraphy_practice` 库存在，**19 张表** |
| Redis @ 6379 | 运行中 |
| JDK | Temurin 21.0.9（项目 target 17，兼容） |
| Maven | 3.9.6；`~/.m2` 已缓存 14374 个文件；**`settings.xml` 的 `<mirrors>` 整段为注释，无镜像** |
| Node | v24.16.0；`Practice-Platform/node_modules` 已存在（266 个包） |
| 端口 5173 | **被另一个项目占用**（「城市智慧管理平台」，Vue+TS，加载 `/src/main.ts`）→ 本项目 dev server 用 **5174** |
| 端口 8083 / 8080 | 空闲，后端未运行 |

数据库现状明细：

- 19 张表：`users` `frontend_users` `backend_users` `admin_users` `roles` `permissions` `role_permissions` `admin_user_permissions` `works` `comments` `discussions` `discussion_replies` `copybooks` `practice_records` `file_reviews` `file_metadata` `notifications` `system_config` `user_activity_logs`。
- 仅 `copybooks` 有数据（3 行）；其余全为 0 行。
- `users` 表同时含 `full_name`/`active`（来自 `recreate_schema.sql`）与 `real_name`/`status`（来自 JPA 实体）——两套历史 schema 的残留列并存。
- `dual_user_schema.sql`（11.5KB，完整双用户设计）**从未被加载**：`application.properties:36` 指向的是 `recreate_schema.sql`。

---

## 3. 架构决策

### 3.1 数据流与三条全局规则

```
View → Store → utils/api.js（唯一客户端，相对路径 /api）
      → vite dev proxy  /api → http://localhost:8083
      → Spring Boot Controller → Service → Repository → MySQL / Redis / 本地磁盘
```

**规则一 · 单一客户端**：前端只保留 `Practice-Platform/src/utils/api.js` 一个 HTTP 客户端。`src/api/community.js` 保留其 14 个端点定义，但改为复用 `api.js` 导出的 `get/post/put/del/upload`，删除对不存在的 `../utils/request` 的 import。

**规则二 · 前缀归一**：后端所有 Controller 统一 `@RequestMapping("/api")`；前端 `API_BASE_URL` 改为相对常量 `'/api'`（不再用绝对 URL 绕过 proxy）；`community.js` 路径中重复的 `/api` 前缀去掉。

**规则三 · 响应格式**：

| 情况 | 响应体 |
|---|---|
| 成功·单资源 | 裸资源 JSON，如 `{"id":1,"title":"楷书基础练习",...}` |
| 成功·列表 | `{"content":[...],"page":0,"size":20,"total":0,"totalPages":0}` |
| 成功·无内容 | 204 No Content |
| 失败 | `{"code":"<ERROR_CODE>","message":"<可读消息>"}` + 语义化 HTTP 状态码 |

状态码语义：`400` 参数非法 / `401` 未认证或 token 失效 / `403` 越权 / `404` 资源不存在 / `409` 冲突（用户名重复等）/ `422` 业务校验失败 / `429` 请求过于频繁 / `500` 服务端错误。

**错误响应体**：新建 `dto/ApiError.java`，字段为 `code`（字符串错误码，如 `AUTH_FAILED`、`USER_EXISTS`、`VALIDATION_ERROR`）与 `message`（可读消息）。由 `exception/GlobalExceptionHandler.java`（新建）统一产出，覆盖 `BusinessException`、`ResourceNotFoundException`、`AuthenticationException`、`UnauthorizedException`、`UserAlreadyExistsException`、`MethodArgumentNotValidException`。

**`dto/ApiResponse.java` 的处置**：该类当前**全项目零引用**（只有自引用），且其成功消息就是乱码 `鎿嶄綔鎴愬姛`。它同时携带 `success`/`data` 字段，与 §3.1 规则三的「成功返回裸资源」冲突。按规则三删除 `ApiResponse`，错误路径改用新的 `ApiError`。

### 3.2 契约层

**权威文件**：

- `docs/api-contract.md` —— 人读的端点规格。
- `Practice-Platform/openapi/contract.yaml` —— 机器读的 OpenAPI 3.1 规格。

**单一真相源规则**：`contract.yaml` 是唯一权威。`api.js` 与 `community.js` 中的每个方法必须一一映射到其中一条 operation id；**新增端点先改 `contract.yaml`，再写后端实现，最后接前端**。这条规则直接针对 C5 暴露的幽灵方法问题。

**契约测试两层**：

1. **后端契约测试**（`SpringBoot-Platform/src/test/java/.../contract/`）：`@SpringBootTest` + `MockMvc`，每个 operation 一个测试方法，断言 method / path / 状态码 / 响应字段集 / 错误码。列表端点断言分页字段齐全；错误路径断言 `{code,message}` 结构。
2. **前端一致性脚本**（`Practice-Platform/scripts/check-contract.mjs`）：解析 `api.js` 与 `community.js` 的全部方法，与 `contract.yaml` 的 operation id **双向比对**——任何一侧多出或缺失都判定失败并列出差异。该脚本纳入 `npm run build` 前置。

**变异测试抽查**：对 auth 与 works 两个域的关键实现做变异——故意改坏（如把 `BCryptPasswordEncoder` 换成 `NoOpPasswordEncoder`、把 `reviewStatus` 判定写反），确认对应契约测试**确实失败**，然后还原并用 `git diff` 证明工作区干净。

**路径冲突风险点**：`GET /api/copybooks/popular` 与 `GET /api/copybooks/{copybookId}` 同级。Spring 字面量路径优先于模板路径，行为正确，但必须在契约测试中显式断言二者互不吞掉，否则改路由时无声退化。

---

## 4. 后端改造

### 4.1 启动阻断修复

| 文件 | 动作 |
|---|---|
| `config/SecurityConfig.java`（新建） | 声明 `SecurityFilterChain`（`STATELESS`、关闭 formLogin/httpBasic/CSRF）、`@Bean BCryptPasswordEncoder`、放行规则：`/api/frontend/auth/register`、`/api/frontend/auth/login`、`/api/backend/auth/login`、`/api/auth/verification-code`、`/uploads/**`、`/swagger-ui/**`、`/v3/api-docs/**`、`/actuator/health`；其余需认证。角色规则：`/api/admin/**` 需 `ROLE_ADMIN` 或 `ROLE_SUPER_ADMIN` |
| `security/JwtAuthenticationFilter.java` | 去掉 `@Component`（避免被当作普通 servlet filter 注册），改由 SecurityFilterChain `addFilterBefore(..., UsernamePasswordAuthenticationFilter.class)` 装配 |
| `interceptor/JwtAuthenticationInterceptor.java`、`security/JwtTokenValidationInterceptor.java` | 删除（职责归 filter）；`constant/SecurityConstants.java` 随之删除 |
| `config/MailConfig.java` | 去掉硬编码的 `smtp.example.com` 占位符，改为读 `spring.mail.*`；未配置 SMTP 时验证码接口走开发模式固定码 |

### 4.2 正确性收敛

**JWT 统一为一套**：以 `service/impl/JwtServiceImpl.java`（HS256 + Redis 黑名单，支持 refresh / revoke）为唯一实现。

- 删除 `security/JwtService.java`（与上面那套 HS512 实现互不兼容）。
- 删除 `util/JwtTokenProvider.java`（仅被两个待删的拦截器使用）。
- `AdminServiceImpl` 依赖的 6 个管理员方法**必须实现**，不能删：`generateAdminToken`、`generateAdminRefreshToken`、`validateAdminToken`、`getUsernameFromAdminToken`、`revokeAdminToken`、`getAdminJwtExpiration`（调用点见 `AdminServiceImpl.java:69,70,87,114,115,132,145,149,158,159,179,187,189`）。这些是 `/api/backend/auth/*` 端点的基础，实现时统一走 `JwtServiceImpl` 的同一套签发/校验逻辑，管理员 token 通过 claim 区分主体类型。
- 密钥**只保留 `application.properties` 的 `jwt.secret`** 一处。
- `config/JwtProperties.java`（`@ConfigurationProperties(prefix = "jwt")`，键名与 properties 对得上）**保留并继续使用**。
- `config/JwtConfig.java`（`@ConfigurationProperties(prefix = "app.jwt")`）**删除** —— properties 里没有任何 `app.jwt.*`，其全部字段永远取默认值 `secret = "mySecretKey"`，是一份完全失效的第三份密钥来源。

**CORS 去重**：删除 `config/WebMvcConfig.java`（与 `config/WebConfig.java` 内容完全相同）。`config/WebConfig.java` 的 `allowedOriginPatterns("*")` + `allowCredentials(true)` 改为 `allowedOrigins("http://localhost:5173", "http://localhost:5174")`。

**数据库策略（保留现有库 + 停用 DROP）**：

- `application.properties`：`spring.sql.init.mode` 由 `always` 改为 `never`（停掉每次启动 DROP 四张表的元凶），保留 `spring.jpa.hibernate.ddl-auto=update` 兜底。
- `db/01-align-copybook-id.sql`（新建，手工执行一次并记入文档）：`ALTER TABLE practice_records MODIFY copybook_id VARCHAR(36)`，对齐 `copybooks.id`。
- `model/Copybook.java`（新建）：映射已有 `copybooks` 表（字段 `id char(36)` / `title` / `description` / `image_url` / `difficulty` enum / `category` / `character_count` / `usage_count` / `is_active` / `created_at` / `updated_at`），3 行既有数据不受影响。
- `model/PracticeRecord.java`：字段 `copybookId` 的类型由 `Long` 改为 `String`，与 `copybooks.id` 的 `char(36)` 一致（对应 §2.3 记录的类型不匹配）。
- 不做清库重建；`users` 表的历史残留列（`full_name`/`active`）保持原样——它们无害，删除属于超出范围的破坏性操作。

**无实现 Service 接口的处置**：本期只实现 `PracticeRecordServiceImpl`（练习记录与统计，对应新增的 `GET /api/copybooks/practice/history`）。其余 5 个：

- `AuthenticationService` 与 `AuthService` 职责重复 → 删除接口。
- `BackendUserService` 与 `AdminService` 职责重复 → 删除接口，后端管理员统一走 `AdminService`。
- `PermissionService` / `RoleService` / `UserActivityLogService` —— 前端无对应端点 → 删除接口，对应 Repository 一并删除。

原则：**不为了「看起来完整」实现没人调的东西**。

**`FileStorageServiceImpl` 瘦身**：只保留实际用到的上传 / 下载 / 删除 / 缩略图四个能力，其余约 60 个一行桩方法删除。调用方必须能从返回值判断「真成功」与「未实现」。

**`EmailServiceImpl` 瘦身**：1850 行中 20 余个只 `log.info` 不发信的方法删除；保留验证码与审核通知两类真实模板。

**`JwtServiceImpl` 补全**：约 1/3 空实现的方法（设备/IP/自定义 claim/审计）删除或实现到可用的最小形态。

### 4.3 环境与仓库卫生

- `~/.m2/settings.xml` 配置阿里云 Maven 镜像（`<mirrorOf>central</mirrorOf>`），当前 `<mirrors>` 整段为注释状态。
- 新增根 `.gitignore`：`SpringBoot-Platform/target/`、`SpringBoot-Platform/*.log`、`SpringBoot-Platform/app.log*`、`*.class`。
- 从 git 索引移除已被跟踪的 `target/classes/**/*.class` 与根目录 `AnnotationCleanup.class`（`git rm --cached`，保留工作区文件）。
- 清理根目录一次性修复脚本：`FixChineseComments.ps1`、`FixEncoding.ps1`、`FixImports.ps1`、`FixImportsBasic.ps1`、`FixImportsSimple.ps1`、`RemoveBom.ps1`；以及 `SpringBoot-Platform/src/main/java/com/calligraphy/platform/util/` 下无 package 声明、会就地批量改写 `src/main/java` 的 `AnnotationCleanup.java` / `BomRemover.java` / `EncodingFixer.java`；以及 `AnnotationCleanup.java` 根目录副本与 `AnnotationCleanup.class`。
- `SpringBoot-Platform/src/main/java/com/calligraphy/platform/test/` 下的 `TestClass.java` 与 `SimpleApplication.java`（第二个 `@SpringBootApplication` + `/test` 控制器）删除；包根的 `Test.java` 删除。
- `src/main/resources/templates/admin/*.html`（8 个 Thymeleaf 页）当前无任何控制器映射 → 删除。
- `application.properties` 的 `logging.level.com.calligraphy.platform.controller=INFO` 指向不存在的包 → 移除。

### 4.4 编码修复

在 §4.1～4.3 之前先行完成，因为乱码会污染后续所有新增代码的中文字面量与注释。

**判定与还原规则**：对每个 CJK 字符片段 `s`，当且仅当以下三条同时成立时判定为乱码并还原为 `s.encode('gbk').decode('utf-8')`：

1. 往返成功（`encode('gbk')` 与 `decode('utf-8')` 均不抛异常）；
2. 往返结果与原串不同；
3. **往返前后都只含 CJK 与常见标点**（守卫条件）。

第 3 条是必需的：裸用往返变换会把 `职位`、`状态` 这类正常中文误判——它们的 GBK 字节恰好构成合法 UTF-8 序列，往返后得到 `ְλ`、`״̬` 这类非中文字符。守卫把假阳性降到 0（47 个文件 / 477 处代码级乱码，无一误判）。

**执行要求**：

- 写一次性脚本 `SpringBoot-Platform/scripts/fix_mojibake.py`，对 `src/main/java/**/*.java`、`src/main/resources/**/*.sql` 逐文件处理，**只改字符串字面量与注释，不动标识符与控制流**（按 §2.4 的判定，代码级乱码均出现在字符串字面量中）。
- 修复后必须 `git diff` 逐文件人工复核，确认没有把正常中文改坏。
- 单独修复 `config/JwtConfig.java:14` —— `private String header = "Authorization";` 被 `//` 注释吞掉，该字段当前不存在。乱码修复后该字段恢复，但由于 `JwtConfig` 整体删除（§4.2），此条仅作为「乱码确实吃掉过代码」的证据记录。
- `EmailServiceImpl.java`（23 处）与各 DTO 的校验消息（见 §2.4）是本次修复的重点——它们直接影响用户可见文本。
- 修复完成后重跑检测脚本，**剩余乱码数必须为 0**。

---

## 5. 前端收敛

| 文件 | 动作 |
|---|---|
| `src/utils/api.js` | `API_BASE_URL` 改为从 `constants.js` 导入（消除 `api.js:4` 与 `constants.js:8` 的双份定义），`constants.js:8` 的兜底值由 `http://localhost:8083/api` 改为相对路径 `'/api'`，保留 `import.meta.env.VITE_APP_BASE_API` 覆盖能力；`request()` 统一直接返回 body（取消 `{data}` 二次解包）；**删除 5 处 mock 降级**（`register` / `login` / `adminLogin` / `sendVerificationCode` / `createWork`） |
| `src/api/community.js` | 删除 `../utils/request` import，改用 `api.js` 的 `get/post/put/del`；路径去掉重复的 `/api` 前缀 |
| `src/stores/practiceStore.js` | 修 3 个幽灵方法调用（C5）；`getUserWorks()` 取当前用户 id（C6）；`saveWork` 接受 `imageDataUrl`（C8） |
| `src/stores/copybookStore.js` | 解包统一为 `response.content`（C4） |
| `src/stores/userStore.js` | `const {data} = ...` → 直接用返回值（C4）；`main.js` 调用 `initUserFromStorage()` |
| `src/stores/appStore.js` | 删除（无任何 import，内部全为假数据） |
| `src/stores/counter.js` | 删除（脚手架残留） |
| `src/utils/index.js` / `src/utils/storage.js` | 合并为一套 `getStorage/setStorage/removeStorage`（统一签名），删除 `userStore.js` 内联的第三套 |
| `src/views/CommunityView.vue` | **修构建阻断**（B1/B2）：合并两处 `handleLike`，保留异步版 |
| `src/views/PracticeView.vue` | `sendToBack` → `sendObjectToBack`（C9）；存作品改传 `imageDataUrl`（C8） |
| `src/main.js` | 调用 `userStore.initUserFromStorage()` |
| `vite.config.js` | proxy 目标 `8080` → `8083`；dev server 端口 → `5174` |
| `index.html` | `<title>` 改为「在线书法练习平台」，`<html lang="zh-CN">` |

---

## 6. 视图接线

把写死的假数据换成真实 API——这是「全量对齐」真正落地的一步，否则端点实现完用户看到的仍是假数据。

| 视图 | 当前状态 | 目标 |
|---|---|---|
| `GalleryView.vue` | `works` 硬编码数组（`:243-244`），`onMounted` 只 `console.log`（`:441-443`） | 接 `GET /api/works` + 分页 + 筛选 |
| `MyWorksView.vue` | `myWorks` 硬编码（`:176`），`loadMyWorks` 只打日志（`:322-324`） | 接 `GET /api/works/user/{currentUserId}` |
| `MyPracticesView.vue` | mock 记录，日历热力图用 `Math.random()`（`:438-446`） | 接 `GET /api/copybooks/practice/history`，热力图由真实日期聚合 |
| `CommunityView.vue` | discussions/activities/groups/announcements/activeUsers/popularTags 六组硬编码 | 接 `src/api/community.js` 的 14 个 discussion 端点；评论走 `/api/community/comments` |
| `AdminView.vue` | users/works/comments 三组硬编码，仅 `fileReviewsAPI.createReview` 是真实调用（`:734`、`:753`） | 接 file-reviews 全部 7 个端点 + 作品/用户列表 |
| `HomeView.vue` | 统计卡片为静态 | 接 `GET /api/stats/overview`（端点 53），数据来自 `works` / `practice_records` 聚合 |

---

## 7. 端点清单（53 个）

`contract.yaml` 与契约测试以此为准。

**认证（10）**

| # | Method | Path |
|---|---|---|
| 1 | POST | `/api/frontend/auth/register` |
| 2 | POST | `/api/frontend/auth/login` |
| 3 | POST | `/api/backend/auth/login` |
| 4 | GET | `/api/frontend/auth/profile` |
| 5 | GET | `/api/backend/auth/profile` |
| 6 | POST | `/api/frontend/auth/refresh` |
| 7 | POST | `/api/backend/auth/refresh` |
| 8 | POST | `/api/frontend/auth/logout` |
| 9 | POST | `/api/backend/auth/logout` |
| 10 | POST | `/api/auth/verification-code` |

**用户（4）**

| # | Method | Path |
|---|---|---|
| 11 | PUT | `/api/users/profile` |
| 12 | POST | `/api/users/avatar`（multipart） |
| 13 | PUT | `/api/users/password` |
| 14 | GET | `/api/users/{userId}` |

**作品（7）**

| # | Method | Path |
|---|---|---|
| 15 | POST | `/api/works`（multipart） |
| 16 | GET | `/api/works` |
| 17 | GET | `/api/works/user/{userId}` |
| 18 | GET | `/api/works/{workId}` |
| 19 | PUT | `/api/works/{workId}` |
| 20 | POST | `/api/works/{workId}/image`（multipart） |
| 21 | DELETE | `/api/works/{workId}` |

**字帖与练习（6）**

| # | Method | Path | 说明 |
|---|---|---|---|
| 22 | GET | `/api/copybooks` | |
| 23 | GET | `/api/copybooks/popular` | 与 #24 同级，注意路径冲突 |
| 24 | GET | `/api/copybooks/{copybookId}` | |
| 25 | POST | `/api/copybooks/{copybookId}/practice` | |
| 26 | GET | `/api/copybooks/statistics/user` | |
| 27 | GET | `/api/copybooks/practice/history` | **新增**，替换幽灵方法 `getPracticeHistory` |

**评论（4）**

| # | Method | Path |
|---|---|---|
| 28 | POST | `/api/community/comments` |
| 29 | GET | `/api/community/comments/work/{workId}` |
| 30 | DELETE | `/api/community/comments/{commentId}` |
| 31 | GET | `/api/community/comments/user/{userId}` |

**管理端审核（7）**

| # | Method | Path |
|---|---|---|
| 32 | POST | `/api/admin/file-reviews` |
| 33 | PUT | `/api/admin/file-reviews/{reviewId}` |
| 34 | GET | `/api/admin/file-reviews/{reviewId}` |
| 35 | GET | `/api/admin/file-reviews/work/{workId}` |
| 36 | GET | `/api/admin/file-reviews/my-reviews` |
| 37 | DELETE | `/api/admin/file-reviews/{reviewId}` |
| 38 | POST | `/api/admin/file-reviews/batch` |

**讨论（14）**

| # | Method | Path |
|---|---|---|
| 39 | GET | `/api/discussions` |
| 40 | GET | `/api/discussions/hot` |
| 41 | GET | `/api/discussions/featured` |
| 42 | GET | `/api/discussions/pinned` |
| 43 | GET | `/api/discussions/search` |
| 44 | GET | `/api/discussions/{id}` |
| 45 | POST | `/api/discussions` |
| 46 | PUT | `/api/discussions/{id}` |
| 47 | DELETE | `/api/discussions/{id}` |
| 48 | POST | `/api/discussions/{id}/like` |
| 49 | GET | `/api/discussions/{discussionId}/replies` |
| 50 | POST | `/api/discussions/{discussionId}/replies` |
| 51 | PUT | `/api/discussions/replies/{replyId}` |
| 52 | DELETE | `/api/discussions/replies/{replyId}` |

**统计（1）**

| # | Method | Path | 说明 |
|---|---|---|---|
| 53 | GET | `/api/stats/overview` | 首页概览统计：作品总数、用户总数、练习记录总数、热门字帖 |

---

## 8. 执行顺序与阶段验收

每阶段独立可验收、可中断。阶段 0 是硬门槛——不修它，后续每一阶段都无法验证。

| 阶段 | 内容 | 验收命令 / 标准 |
|---|---|---|
| **0 地基** | §4.4 编码修复；§4.1 启动阻断全部修复；**§4.2 中的 JWT 统一 / CORS 去重 / DB 策略 / MailConfig**；§5 中构建阻断修复（B1/B2） | 后端 `mvn compile` 通过并能正常启动（无 `NoSuchBeanDefinitionException`）；乱码检测脚本剩余 0 处；`npm run build` 退出码 0 |

> **阶段 0 范围为何包含 §4.2 的部分条目**（2026-10-20 补充）：编写实施计划时实测发现 B7——后端当前**根本无法编译**。编译能通过是「能启动」的前提，而它进一步强制要求：① `security.JwtService` 与 `service.JwtService` 两个 `@Service` 同名 bean 必须合并（否则 `JwtAuthenticationFilter` 注入时抛 `NoUniqueBeanDefinitionException`）；② `spring.sql.init.mode=always` 的 DROP 行为必须停用（否则每次启动清库，测试无法复现）。CORS 去重与 MailConfig 不属于启动阻断，但与 §4.1 的 `SecurityConfig` 同属一个提交单元，一并处理更省事。
| **1 契约定稿** | `contract.yaml` + `api-contract.md`；`scripts/check-contract.mjs` 双向比对脚本 | 一致性脚本通过（此阶段允许报差异清单，差异就是待实现清单） |
| **2 auth 域** | 端点 1–10 | 登录 → 带 `Authorization: Bearer` → refresh → logout 全链路通；契约测试 10 项绿 |
| **3 user + works 域** | 端点 11–21、53 | 上传作品 → 画廊可见 → 图片能取回；契约测试绿 |
| **4 copybooks 域** | 端点 22–27 | 字帖列表/详情/记录练习/统计/历史；契约测试绿；`copybooks` 既有 3 行数据可读 |
| **5 comments + discussions 域** | 端点 28–31、39–52 | 发帖/回复/点赞/评论；契约测试绿 |
| **6 file-reviews 域** | 端点 32–38 | 管理员审核作品，审核状态回写 `works.review_status`；契约测试绿 |
| **7 前端接线 + 删 mock** | §5、§6 全部 | 六个视图无硬编码 mock 数组；`src/utils/api.js` 无 mock 降级分支 |
| **8 全量验证** | 变异测试抽查 + 真实后端冒烟 | **53** 项契约测试全绿；一致性脚本零差异；前后端联调冒烟通过 |

### 8.1 实施计划的拆分方式

本 spec 的范围（53 个端点 + 6 个视图接线 + 后端地基改造）**超出单份实施计划的合理容量**。按 brainstorming 流程，实施计划按阶段拆分为 8 份，每份一份计划、前置阶段完成后再启动下一份：

| 计划 | 覆盖阶段 | 依赖 |
|---|---|---|
| P0 | 阶段 0（编码修复 + 启动阻断 + 构建阻断） | 无 |
| P1 | 阶段 1（契约定稿 + 一致性脚本） | P0 |
| P2 | 阶段 2（auth 域） | P1 |
| P3 | 阶段 3–4（user / works / copybooks 域） | P2 |
| P4 | 阶段 5–6（comments / discussions / file-reviews 域） | P3 |
| P5 | 阶段 7（前端接线 + 删 mock） | P4 |
| P6 | 阶段 8（变异测试 + 全量冒烟） | P5 |

P0 完成后即已具备「后端能启动 + 前端能构建」的可用基线；P2 完成后具备第一条端到端真实链路。

### 变异测试抽查清单（阶段 8）

| 被改坏的东西 | 期望失败的测试 |
|---|---|
| `BCryptPasswordEncoder` → `NoOpPasswordEncoder` | auth 域注册/登录用例 |
| `works.reviewStatus` 判定写反 | file-reviews 用例 |
| `copybooks` 列表过滤条件写反 | copybooks 域用例 |
| `GlobalExceptionHandler` 不再返回 `{code,message}` | 错误路径用例 |

---

## 9. 风险与缓解

| 风险 | 影响 | 缓解 |
|---|---|---|
| 工作量超出预期（53 端点 + 6 视图接线） | 中 | 阶段化，每阶段可停；阶段 0–2 结束即有一条完整可用链路 |
| 前端大改（解包统一 + mock 删除）引入回归 | 中 | 每个 store 改动都配契约测试；`git diff` 逐阶段审查 |
| 现有数据库结构与实体不完全对应 | 中 | 保留库不重建（已裁决）；`ddl-auto=update` 兜底；一次性修正脚本记入 `db/` |
| Maven 依赖拉取慢 | 低 | 配阿里云镜像；`.m2` 已缓存 14374 个文件 |
| 端口 5173 被占 | 低 | dev server 改 5174，CORS 白名单同步更新 |
| 契约测试写成「空断言」——绿了但没在测东西 | 高 | 阶段 8 变异测试抽查，逐项证明测试能失败 |
| 后续功能（评分、真字体）被提前拉进来 | 中 | 明确列为非目标；本期不做 |

### 后续项（本期不做，记录备查）

- **智能书法评分与笔画比对**：需引入汉字笔画数据源（如 hanzi-writer / makemeahanzi 一类）。当前仓库**没有任何汉字或笔画数据**，字帖的 5 个字体选项（楷/行/隶/篆/草）全部渲染为系统 `SimSun`，`PracticeView` 的参考字是写死的 `['永','和','书','法','文','明','德','信']` 且点击后不在画布上绘制字形轮廓。
- **真实书法字体**：需引入字体文件与 `@font-face`，或改用矢量字形渲染。
- **后端可观测性**：目前只有 `app.log` 文本日志，无结构化日志与链路追踪。

---

## 10. 变更记录

- 2026-10-20 初稿，经用户确认：方案 = 契约单一真相源 + 契约测试锁定；响应格式 = 成功裸资源 + 错误 `{code,message}`；视图接线 = 全接；数据库 = 保留现有库 + 停用 DROP。
- 2026-10-20 编写实施计划时实测发现 **B7：后端完全无法编译**（`mvn -o compile` 失败）。据此：
  - §2.1 新增 B7；§2.4 补充「乱码吃掉收尾引号 / 换行 / 整条代码声明」这一事实；
  - §8 阶段 0 的范围相应扩展，纳入 §4.2 的 JWT 统一、CORS 去重、DB 策略、MailConfig（理由见表下说明）。
  - 对应的实施计划：`docs/superpowers/plans/2026-10-20-p0-foundation-repair.md`。
