# API 契约（人读版）

> **机器可读版本是 [`Practice-Platform/openapi/contract.yaml`](../Practice-Platform/openapi/contract.yaml)，它是唯一权威。**
> 本文件是它的人类可读版本。两边不一致时以 `contract.yaml` 为准；
> 发现不一致请改 `contract.yaml`，然后跑 `npm run contract:check`。

## 0. 怎么用

| 事情 | 命令 |
|---|---|
| 校验契约与代码是否还对得上 | `cd Practice-Platform && npm run contract:check` |
| 严格模式（有任何差异就失败，P6 用） | `npm run contract:check:strict` |
| 构建前端（自动先跑上面的校验） | `npm run build` |
| 看差异明细 | `Practice-Platform/openapi/contract-diff.md`（每次检查自动重生成） |

检查器做三方比对：

```
前端实际调用点  (src/utils/api.js, src/api/community.js)
       ↕
契约声明        (openapi/contract.yaml)
       ↕
后端已实现      (SpringBoot-Platform/**/*Controller.java)
```

结论分三类：

- **违规 (violation)** —— 契约与代码「对不上」且无人计划过（前端调了契约里没有的端点、路径参数名不一致、后端实现了契约外的端点……）。**一律让构建失败。**
- **缺口 (gap)** —— 契约声明了但代码里还没有。**这就是待实现清单**，本身不算错误。
- **警告 (warning)** —— 已知缺陷，需要排期修，但不构成契约漂移。

新增端点的流程：**先改 `contract.yaml` → 再写后端实现 → 最后接前端**。

## 1. 全局规则

### 1.1 响应格式

| 情况 | 响应体 |
|---|---|
| 成功·单资源 | 裸资源 JSON，如 `{"workId":"...","title":"楷书基础练习"}` |
| 成功·列表 | `{"content":[...],"page":0,"size":20,"total":0,"totalPages":0}` |
| 成功·无内容 | `204 No Content` |
| 失败 | `{"code":"<ERROR_CODE>","message":"<可读消息>"}` + 语义化 HTTP 状态码 |

**不得**再套 `success` / `data` 外壳。现有后端的 `dto/ApiResponse.java` 全项目零引用，
且成功消息本身就是乱码，按本规则删除；错误路径改用新的 `dto/ApiError.java`，
由 `exception/GlobalExceptionHandler.java` 统一产出。

### 1.2 状态码语义

`400` 参数非法 / `401` 未认证或令牌失效 / `403` 越权 / `404` 资源不存在 /
`409` 冲突（用户名重复等）/ `422` 业务校验失败 / `423` 账号锁定 / `429` 请求过于频繁 / `500` 服务端错误。

### 1.3 鉴权

- 认证用 `Authorization: Bearer <JWT>`。
- 表中「公开」的端点不需要令牌；「需登录」的需要任意有效令牌；
  `/api/admin/**` 还需要 `ADMIN` 或 `SUPER_ADMIN` 角色（表里标为「需登录」，
  但实际要求见各端点的 description）。

### 1.4 分页

列表端点一律返回 `{content,page,size,total,totalPages}`，`page` 从 0 开始。
`content` 是元素数组。**分页字段必须齐全**，契约测试会逐个断言。

### 1.5 路径

- 后端所有 Controller 统一 `@RequestMapping("/api")`。
- 前端 `API_BASE_URL` 是 `http://localhost:8083/api`，`api.js` 里的路径**不带** `/api` 前缀。
- `community.js` 里的路径同样不带前缀（base 里已经有了）。

## 2. 端点清单（53 个）

| Method | Path | operationId | 说明 | 鉴权 | 前端调用 |
|---|---|---|---|---|---|
| POST | `/api/frontend/auth/register` | `frontendAuthRegister` | 前端用户注册 | 公开 | `authAPI.register` |
| POST | `/api/frontend/auth/login` | `frontendAuthLogin` | 前端用户登录 | 公开 | `authAPI.login` |
| POST | `/api/backend/auth/login` | `backendAuthLogin` | 管理员登录 | 公开 | `authAPI.adminLogin` |
| GET | `/api/frontend/auth/profile` | `frontendAuthProfile` | 前端用户当前资料 | 需登录 | `authAPI.getProfile` |
| GET | `/api/backend/auth/profile` | `backendAuthProfile` | 管理员当前资料 | 需登录 | `authAPI.getProfile` |
| POST | `/api/frontend/auth/refresh` | `frontendAuthRefresh` | 前端用户刷新令牌 | 公开 | `authAPI.refreshToken` |
| POST | `/api/backend/auth/refresh` | `backendAuthRefresh` | 管理员刷新令牌 | 公开 | `authAPI.refreshToken` |
| POST | `/api/frontend/auth/logout` | `frontendAuthLogout` | 前端用户登出 | 需登录 | `authAPI.logout` |
| POST | `/api/backend/auth/logout` | `backendAuthLogout` | 管理员登出 | 需登录 | `authAPI.logout` |
| POST | `/api/auth/verification-code` | `authSendVerificationCode` | 发送邮箱验证码 | 公开 | `authAPI.sendVerificationCode` |
| PUT | `/api/users/profile` | `userUpdateProfile` | 更新当前用户资料 | 需登录 | `userAPI.updateProfile` |
| POST | `/api/users/avatar` | `userUploadAvatar` | 上传用户头像 | 需登录 | `userAPI.uploadAvatar` |
| PUT | `/api/users/password` | `userChangePassword` | 修改密码 | 需登录 | `userAPI.changePassword` |
| GET | `/api/users/{userId}` | `userGetById` | 获取指定用户资料 | 需登录 | `userAPI.getUserInfo` |
| POST | `/api/works` | `workCreate` | 发布作品 | 需登录 | `worksAPI.createWork` |
| GET | `/api/works` | `workList` | 作品列表 | 公开 | `worksAPI.getWorks` |
| GET | `/api/works/user/{userId}` | `workListByUser` | 某个用户的作品列表 | 需登录 | `worksAPI.getUserWorks` |
| GET | `/api/works/{workId}` | `workGetById` | 作品详情 | 需登录 | `worksAPI.getWorkDetail` |
| PUT | `/api/works/{workId}` | `workUpdate` | 更新作品 | 需登录 | `worksAPI.updateWork` |
| DELETE | `/api/works/{workId}` | `workDelete` | 删除作品 | 需登录 | `worksAPI.deleteWork` |
| POST | `/api/works/{workId}/image` | `workUpdateImage` | 更新作品图片 | 需登录 | `worksAPI.updateWorkImage` |
| GET | `/api/copybooks` | `copybookList` | 字帖列表 | 公开 | `copybookAPI.getCopybooks` |
| GET | `/api/copybooks/popular` | `copybookPopular` | 热门字帖 | 公开 | `copybookAPI.getPopularCopybooks` |
| GET | `/api/copybooks/statistics/user` | `copybookUserStatistics` | 当前用户的练习统计 | 需登录 | `copybookAPI.getUserStatistics` |
| GET | `/api/copybooks/practice/history` | `copybookPracticeHistory` | 当前用户的练习历史 | 需登录 | `copybookAPI.getPracticeHistory` ⚠ |
| GET | `/api/copybooks/{copybookId}` | `copybookGetById` | 字帖详情 | 公开 | `copybookAPI.getCopybookDetail` |
| POST | `/api/copybooks/{copybookId}/practice` | `copybookRecordPractice` | 记录一次练习 | 需登录 | `copybookAPI.recordPractice` |
| POST | `/api/community/comments` | `commentCreate` | 发表评论 | 需登录 | `communityAPI.createComment` |
| GET | `/api/community/comments/work/{workId}` | `commentListByWork` | 某个作品的评论列表 | 公开 | `communityAPI.getWorkComments` |
| GET | `/api/community/comments/user/{userId}` | `commentListByUser` | 某个用户的评论历史 | 需登录 | `communityAPI.getUserComments` |
| DELETE | `/api/community/comments/{commentId}` | `commentDelete` | 删除评论 | 需登录 | `communityAPI.deleteComment` |
| POST | `/api/admin/file-reviews` | `fileReviewCreate` | 提交作品审核 | 管理员 | `fileReviewsAPI.createReview` |
| POST | `/api/admin/file-reviews/batch` | `fileReviewBatch` | 批量审核作品 | 管理员 | `fileReviewsAPI.batchReviewWorks` |
| GET | `/api/admin/file-reviews/my-reviews` | `fileReviewMyReviews` | 我提交的审核记录 | 管理员 | `fileReviewsAPI.getMyReviews` |
| GET | `/api/admin/file-reviews/work/{workId}` | `fileReviewGetByWorkId` | 某个作品的审核记录 | 管理员 | `fileReviewsAPI.getReviewByWorkId` |
| GET | `/api/admin/file-reviews/{reviewId}` | `fileReviewGetById` | 审核记录详情 | 管理员 | `fileReviewsAPI.getReviewById` |
| PUT | `/api/admin/file-reviews/{reviewId}` | `fileReviewUpdate` | 更新审核结论 | 管理员 | `fileReviewsAPI.updateReview` |
| DELETE | `/api/admin/file-reviews/{reviewId}` | `fileReviewDelete` | 删除审核记录 | 管理员 | `fileReviewsAPI.deleteReview` |
| GET | `/api/discussions` | `discussionList` | 讨论列表 | 公开 | `getDiscussions` |
| POST | `/api/discussions` | `discussionCreate` | 发帖 | 需登录 | `createDiscussion` |
| GET | `/api/discussions/hot` | `discussionHot` | 热门讨论 | 公开 | `getHotDiscussions` |
| GET | `/api/discussions/featured` | `discussionFeatured` | 精华讨论 | 公开 | `getFeaturedDiscussions` |
| GET | `/api/discussions/pinned` | `discussionPinned` | 置顶讨论 | 公开 | `getPinnedDiscussions` |
| GET | `/api/discussions/search` | `discussionSearch` | 搜索讨论 | 公开 | `searchDiscussions` |
| GET | `/api/discussions/{id}` | `discussionGetById` | 讨论详情 | 公开 | `getDiscussion` |
| PUT | `/api/discussions/{id}` | `discussionUpdate` | 编辑帖子 | 需登录 | `updateDiscussion` |
| DELETE | `/api/discussions/{id}` | `discussionDelete` | 删除帖子 | 需登录 | `deleteDiscussion` |
| POST | `/api/discussions/{id}/like` | `discussionLike` | 点赞 / 取消点赞 | 需登录 | `likeDiscussion` |
| GET | `/api/discussions/{discussionId}/replies` | `discussionReplyList` | 某个讨论的回复列表 | 公开 | `getDiscussionReplies` |
| POST | `/api/discussions/{discussionId}/replies` | `discussionReplyCreate` | 回复讨论 | 需登录 | `replyToDiscussion` |
| PUT | `/api/discussions/replies/{replyId}` | `discussionReplyUpdate` | 编辑回复 | 需登录 | `updateDiscussionReply` |
| DELETE | `/api/discussions/replies/{replyId}` | `discussionReplyDelete` | 删除回复 | 需登录 | `deleteDiscussionReply` |
| GET | `/api/stats/overview` | `statsOverview` | 首页概览统计 | 公开 | `statsAPI.getOverview` ⚠ |

⚠ = 该端点**前端目前没有可用的调用入口**，P5 必须补上（见 §4）。

## 3. 已知陷阱

这些是契约与真实代码之间、**光看端点表看不出来**的地方。实现时必须按这里说的做。

### 3.1 Lombok 布尔字段会被 Jackson 剥掉 `is` 前缀

DTO 里写 `private boolean isFeatured;` 时，Lombok 生成的是 `isFeatured()`，
Jackson 会把属性名变成 **`featured`**；而实体上手写的 `getIsFeatured()` 得到的是
**`isFeatured`**。两边不一致。

契约统一按 **DTO 字段名**书写（`isFeatured`、`isLikedByCurrentUser`、`isVerified`、
`isTwoFactorEnabled`），**实现时必须用 `@JsonProperty` 或调整命名把 JSON 对齐**，
否则线上 JSON 与契约对不上，而契约测试会第一个炸出来。

（`Boolean` 包装类型不受影响 —— Lombok 生成 `getIsActive()`，属性名仍是 `isActive`。）

### 3.2 同名 DTO 家族的字段类型冲突

后端有一批**同名但类型不同**的 DTO，必须分别建模，不能混用：

| DTO | workId 类型 | 契约用哪个 |
|---|---|---|
| `CommentCreateDto` | `Long` | ✗ 不用 |
| `CommentCreateRequestDto` | `String` | ✓ `CommentCreateRequest` |
| `CommentUpdateDto` | — | ✗ 不用 |
| `CommentUpdateRequestDto` | — | ✓ |

契约里所有 ID 一律是 **string**（与前端、与 `Work.workId` / `Comment.commentId` 一致）。
`CommentCreateDto` 那套 `Long` 版本不进契约。

### 3.3 路径冲突

`GET /api/copybooks/popular` 与 `GET /api/copybooks/{copybookId}` 同级。
Spring 字面量路径优先于模板路径，行为正确，但**契约测试里必须显式断言二者互不吞掉**，
否则改路由时会无声退化。

`GET /api/discussions/search` / `hot` / `featured` / `pinned` 与
`GET /api/discussions/{id}` 也有同样的关系。

### 3.4 `dto/ApiResponse.java` 要删

它全项目零引用，成功消息还是乱码 `鎿嶄綔鎴愬姛`，且与「成功返回裸资源」冲突。
按规则三删除，错误路径改用新的 `dto/ApiError.java`。

### 3.5 前端目前读的是 `res.data.xxx`，与「裸资源」冲突

这是评审点名的 F-10，细节比想象的严重。契约规定裸资源，但前端这些地方还在读信封：

| 端点 | 前端现在的读法 | 契约要求 |
|---|---|---|
| `GET /api/discussions` | `response.success` + `response.data.content[]` + `response.data.totalElements` | `{content,page,size,total,totalPages}` |
| `GET /api/copybooks` | `response.data.copybooks`（另一处又读 `response.map`） | `{content,...}` |
| `GET /api/copybooks/popular` | `response.data.copybooks` | `{content,...}` |
| `GET /api/copybooks/{copybookId}` | `response.data.copybook` | 裸资源 |
| `GET /api/copybooks/statistics/user` | `response.data` | 裸资源 |
| `POST /api/copybooks/{copybookId}/practice` | `response.data`（另一处读裸 `id`/`createdAt`） | 裸资源 |
| `POST /api/{frontend,backend}/auth/refresh` | `data.accessToken` | `{accessToken,refreshToken,...}` |
| `GET /api/{frontend,backend}/auth/profile` | `response.user` | 裸资源 |
| `PUT /api/users/profile` | `response.user` | 裸资源 |

**P5 接线时必须逐个改掉**。改不掉就说明契约错了 —— 那时改契约，别偷偷在后端加信封。

## 4. 前端现存缺陷（P5 待修）

这些是本轮提取时挖出来的真实缺陷。检查器会把它们列为 **warning**，
不阻断构建，但必须排期修掉。

| 类型 | 位置 | 问题 |
|---|---|---|
| 绕过统一客户端 | `src/utils/api.js:94` | 401 刷新走的是裸 `fetch(${API_BASE_URL}/auth/refresh)`，绕过了统一客户端的错误处理与自动重试。**这个端点不在契约的 53 个里** —— P5 必须并入 `/frontend/auth/refresh` 或 `/backend/auth/refresh`（`getUserType()` 在同一模块可用）。 |
| 幽灵方法 | `src/stores/practiceStore.js:84` | `copybookAPI.addPracticeRecord(...)` —— api.js 里没有这个方法，运行时直接 TypeError。真实的方法叫 `copybookAPI.recordPractice`。 |
| 幽灵方法 | `src/stores/practiceStore.js:250` | `copybookAPI.getPracticeHistory(...)` —— api.js 里没有这个方法。契约已为 `GET /api/copybooks/practice/history` 预留端点，P5 需在 api.js 里补出这个方法。 |
| 幽灵方法 | `src/stores/practiceStore.js:287` | `copybookAPI.getPracticeStats(...)` —— api.js 里没有这个方法。对应端点是 `GET /api/copybooks/statistics/user`（api.js 里的 `getUserStatistics`）。 |
| store 动作名不符 | `src/stores/userStore.js` | 视图调 `userStore.updateProfile`，store 里定义的却是 `updateUserInfo` → 链路不可达。 |
| 参数形状不对 | `src/stores/userStore.js:344` | `PUT /api/users/password` 实际发出去的是**裸 JSON 字符串**，而契约要求对象 `{email,verificationCode,newPassword,confirmPassword}`。 |
| URL 拼错 | `src/stores/userStore.js` | `GET /api/works/user/{userId}` 实际请求的是 `/works/user/undefined`。 |

另有 **7 处 mock 降级分支**（api.js 里 5 处 `catch` 返回假数据 + store 里 2 处），
P5 接线时必须全部删掉 —— 否则接口没通的时候页面会显示假数据，**看起来是好的**。

## 5. 阶段 1 的差异清单（= 待实现清单）

`npm run contract:check` 当前输出：

```
contract  : 53 operation(s)
frontend  : 51 call site(s)
backend   : 0 controller route(s)
violations: 0
gaps      : 55
warnings  : 4
```

- **违规 0** —— 契约与现有代码没有矛盾，校验通过。
- **缺口 55** = 53 条「后端还没实现」（后端目前**零 Controller**）+ 2 条「前端还没接线」
  （`GET /api/copybooks/practice/history`、`GET /api/stats/overview`）。
- **警告 4** = §4 表里的前 4 条（1 处裸 fetch + 3 处幽灵方法）。

**这份缺口清单就是 P2–P6 的工作项。** 每完成一个域，缺口数下降，
`npm run contract:check` 的输出就是进度。到 P6 必须 `--strict` 也过。

## 6. 阶段划分与验收

| 阶段 | 覆盖 | 验收 |
|---|---|---|
| **P1 契约定稿**（本阶段） | `contract.yaml` + 本文件 + `check-contract.mjs` | 校验脚本通过，差异清单即待实现清单 |
| **P2 auth 域** | 端点 1–10 | 登录 → 带 Bearer → refresh → logout 全链路通；契约测试 10 项绿 |
| **P3 user + works 域** | 端点 11–21、53 | 上传作品 → 画廊可见 → 图片能取回；契约测试绿 |
| **P4 copybooks 域** | 端点 22–27 | 字帖列表/详情/记录练习/统计/历史；`copybooks` 既有 3 行数据可读 |
| **P5 comments + discussions + file-reviews 域** | 端点 28–52 | 发帖/回复/点赞/评论/审核；契约测试绿 |
| **P6 前端接线 + 删 mock** | §3.5、§4 | 六个视图无硬编码 mock；api.js 无 mock 降级分支 |
| **P7 全量验证** | — | 53 项契约测试全绿；`--strict` 零差异；前后端联调冒烟通过 |

## 7. 变更记录

- 2026-10-21 初稿（P1）。53 个端点全部按前端实际调用点 + 后端 DTO/实体字段推导，
  不是手写臆测。前端 51 个调用点与 spec §7 清单**逐条吻合**（method、path、路径参数名全对），
  差的正好是 spec 标注为「新增」的两个端点。
