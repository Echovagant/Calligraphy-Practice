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

- **违规 (violation)** —— 契约与代码「对不上」且无人计划过（前端调了契约里没有的端点、路径参数名不一致、后端实现了契约外的端点、同一个端点被实现了两次……）。**一律让构建失败。**
- **缺口 (gap)** —— 契约声明了但代码里还没有。**这就是待实现清单**，本身不算错误。
- **警告 (warning)** —— 已知缺陷，或「看起来实现了、其实没有」：前端调了 api.js 里
  不存在的方法（幽灵方法）、后端方法体只有一个常量 return / 抛
  `UnsupportedOperationException` / 是空方法体（桩实现）。

检查器的能力边界（**这些它查不出来**，别指望它）：

- 不校验响应状态码是否与实现一致 —— 那是**契约测试**的职责
- 不校验请求体 / 响应体的字段语义 —— 同上
- 不判断「某个 service 实现是不是该域的正解」—— 那是代码评审的职责

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

### 1.6 用户只有一张表

**前端用户和管理员都在 `users` 表**（实体 `model/User`），靠 `role` 区分：

| | role | 令牌 userType |
|---|---|---|
| 前端用户 | `USER` | `USER` |
| 管理员 | `ADMIN` / `SUPER_ADMIN` | `ADMIN` |

依据：`model/User.Role` 枚举本来就是 `{USER, ADMIN, SUPER_ADMIN}`；
`UserDetailsServiceImpl` 先查 `users` 表并按 `ROLE_<role>` 授权；
`AuthServiceImpl` 的注册/登录/刷新全部走 `users` 表。

**不要用** `frontend_users`（`model/FrontendUser`）、`backend_users`（`model/BackendUser`）、
`admin_users`（`entity/AdminUser`）这三条**平行的历史路径** ——
它们各自一套 service（`FrontendUserService` / `BackendUserService` / `AdminService`），
`BackendUserService` 更是**连实现类都没有**。本期一律不碰；
将来若要合并，是一次独立的重构。

## 2. 端点清单（56 个）

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
| GET | `/api/copybooks` | `copybookList` | 字帖列表（只含精选字帖） | 公开 | `copybookAPI.getCopybooks` |
| POST | `/api/copybooks` | `copybookCreate` | 保存一份自建字帖 | 需登录 | `copybookAPI.createCopybook` |
| GET | `/api/copybooks/mine` | `copybookListMine` | 我保存的自建字帖 | 需登录 | `copybookAPI.getMyCopybooks` |
| GET | `/api/copybooks/popular` | `copybookPopular` | 热门字帖 | 公开 | `copybookAPI.getPopularCopybooks` |
| GET | `/api/copybooks/statistics/user` | `copybookUserStatistics` | 当前用户的练习统计 | 需登录 | `copybookAPI.getUserStatistics` |
| GET | `/api/copybooks/practice/history` | `copybookPracticeHistory` | 当前用户的练习历史 | 需登录 | `copybookAPI.getPracticeHistory` |
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
| GET | `/api/admin/works/pending` | `adminPendingWorks` | 待审队列（待审核 / 等复审的作品） | 管理员 | `adminAPI.getPendingWorks` |
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
| GET | `/api/stats/overview` | `statsOverview` | 首页概览统计 | 公开 | `statsAPI.getOverview` |

以上端点全部有前端调用入口，无待接线项。

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
| ~~`CommentCreateDto`~~ | `Long` | ✗ 不用 —— **P5 已删**（裁剪后零引用） |
| `CommentCreateRequestDto` | `String` | ✓ `CommentCreateRequest` |
| ~~`CommentUpdateDto`~~ | — | ✗ 不用 —— **P5 已删** |
| ~~`CommentUpdateRequestDto`~~ | — | ✗ **契约里没有评论更新端点**，P5 裁剪后已删 |

契约里所有 ID 一律是 **string**（与前端、与 `Work.workId` / `Comment.commentId` 一致）。
`CommentCreateDto` 那套 `Long` 版本不进契约，且已在 P5 随接口裁剪一起删除。

### 3.3 路径冲突

`GET /api/copybooks/popular` 与 `GET /api/copybooks/{copybookId}` 同级。
Spring 字面量路径优先于模板路径，行为正确，但**契约测试里必须显式断言二者互不吞掉**，
否则改路由时会无声退化。

`GET /api/discussions/search` / `hot` / `featured` / `pinned` 与
`GET /api/discussions/{id}` 也有同样的关系。

### 3.4 `dto/ApiResponse.java` 已删

它全项目零引用，成功消息还是乱码 `鎿嶄綔鎴愬姛`，且与「成功返回裸资源」冲突。
**P2 已删除**（`git rm`），错误路径改用新的 `dto/ApiError.java`，
由 `exception/GlobalExceptionHandler.java` 统一产出 `{code,message}`。

### 3.5 前端目前读的是 `res.data.xxx`，与「裸资源」冲突

这是评审点名的 F-10，细节比想象的严重。契约规定裸资源，但前端这些地方还在读信封：

| 端点 | 前端现在的读法 | 契约要求 | 状态 |
|---|---|---|---|
| `GET /api/discussions` | `response.success` + `response.data.content[]` + `response.data.totalElements` | `{content,page,size,total,totalPages}` | P5 待改 |
| `GET /api/copybooks` | `response.data.copybooks`（另一处又读 `response.map`） | `{content,...}` | P5 待改 |
| `GET /api/copybooks/popular` | `response.data.copybooks` | `{content,...}` | P5 待改 |
| `GET /api/copybooks/{copybookId}` | `response.data.copybook` | 裸资源 | P5 待改 |
| `GET /api/copybooks/statistics/user` | `response.data` | 裸资源 | P5 待改 |
| `POST /api/copybooks/{copybookId}/practice` | `response.data`（另一处读裸 `id`/`createdAt`） | 裸资源 | P5 待改 |
| `POST /api/{frontend,backend}/auth/refresh` | `const { data } = await ...` → `data.accessToken` | `{accessToken,refreshToken,...}` | **P2 已改** |
| `GET /api/{frontend,backend}/auth/profile` | `response.user` | 裸资源 | **P2 已改** |
| `POST /api/frontend/auth/register` | `this.userInfo = response`（整个信封） | `response.user` | **P2 已改** |
| `GET /api/works/user/{userId}` | `worksAPI.getUserWorks()` **没传 userId** → 实际请求 `/works/user/undefined` | 传对 userId + 分页信封 | **P3 已改** |
| `PUT /api/users/password` | 视图用两个位置参数调 store → 实际发出去的是**裸 JSON 字符串** | `{oldPassword,newPassword,confirmPassword}` | **P3 已改** |
| `PUT /api/users/profile` | `response.user` | 裸资源 | P5 待改 |

**P2/P3 改法**：`authAPI.refreshToken` 的返回值去掉了 `data` 包装，
`getProfile` 与 `register` 改成直接用裸用户对象；`getUserWorks` 补上 userId
并按分页信封读 `response.content`；改密码改成传对象。
**P5 接线时必须逐条改掉剩下的**。
改不掉就说明契约错了 —— 那时改契约，别偷偷在后端加信封。

### 3.6 响应里的主键一律叫 `id`（P3 修订）

前端从 API 响应里读的是 **`work.id` / `comment.id` / `user.id`**，
它的 mock 数组里也全是 `id:`，全项目**没有一处读 `workId`**
（`xxxId` 只出现在**路径参数名**里，那是另一回事 —— 路径参数名仍叫
`{workId}` / `{commentId}` / `{reviewId}`，前后端一致，检查器也会校验）。

⇒ P3 把各实体 schema 的主键字段统一改成 `id`：

| schema | 改前 | 改后 |
|---|---|---|
| `Work` | `workId` | `id` |
| `Comment` | `commentId` | `id` |
| `Discussion` | `discussionId` | `id` |
| `DiscussionReply` | `replyId` | `id` |
| `FileReview` | `reviewId` | `id` |
| `PracticeRecord` | `recordId` | `id` |
| `FrontendUser` / `BackendUser` | `userId` | `id` |

**其它字段一律不动** —— `userId` 作为「引用别人的 id」保留
（`Work.userId`、`Comment.workId`、`DiscussionReply.parentId`……）。

后端实现上：`WorkDto` 等 DTO 的 Java 字段名没动（`workId`），
用 `@JsonProperty("id")` 把 JSON 名对上。

### 3.7 修改密码的请求体是「旧密码 + 新密码」

P3 把 `PUT /api/users/password` 的请求体定成
`{oldPassword, newPassword, confirmPassword}`（已登录用户改自己的密码）。

**不是**后端那个 `PasswordResetDto`（`email` + `verificationCode`）——
那是**忘记密码的重置**语义（邮箱 + 验证码），而契约的 54 个端点里
**根本没有重置密码的端点**；拿它当「修改密码」的请求体是不对的。
前端 `ProfileView` 的表单字段本来就正好是这三个。

### 3.8 账号锁定（423）在 Spring Security 里会被「洗掉类型」

**在 `UserDetailsServiceImpl` 里抛 `LockedException` 是走不到 423 的。**

Spring Security 6.2 的 `DaoAuthenticationProvider.retrieveUser` 有三个 catch：

| 捕获类型 | 处理 |
|---|---|
| `UsernameNotFoundException` | 原样抛出（顺带做计时攻击缓解） |
| `InternalAuthenticationServiceException` | 原样抛出 |
| `Exception`（兜底） | **包成 `InternalAuthenticationServiceException(msg, cause)`** |

`LockedException` 不属于前两类，会落进兜底分支 —— **消息保住了，类型没了**。
业务层拿到的已经是 `InternalAuthenticationServiceException`，只能按「用户名或密码错误」处理，
契约声明的 `423 ACCOUNT_LOCKED` 永远走不到。

所以锁定判定必须在 `authenticationManager.authenticate()` **之前**做
（`AuthServiceImpl` 里先查用户、再判锁定），并在兜底 catch 里从 `e.getCause()`
把 `LockedException` 还原出来作为双保险。

顺带修掉一个语义 bug：`User.isEnabled()` 早期把锁定也折了进去
（`ACTIVE.equals(status) && isAccountNonLocked()`），导致锁定账户在
`UserDetailsServiceImpl` 里被当成「账户不存在」。现已拆开 ——
禁用看 `status` 列，锁定看 `account_locked` 列（数据库里本来就是两列）。

## 4. 前端缺陷（**P6 已全部修掉**）

P1 提取时挖出来的真实缺陷，以及 P6 施工中新发现的问题，都已修完。
留这张表是为了说明「当时错在哪」—— 下面每一条都标注了修法。

| 类型 | 位置 | 问题 → 修法 |
|---|---|---|
| ~~绕过统一客户端~~ | `src/utils/api.js:94` | **P2 已改**。401 刷新原本走裸 `fetch` 打 `/api/auth/refresh` —— 而**这个端点根本不在契约里**，自动续期路径必然 404。现按用户类型分流到带类型的 refresh 端点。裸 fetch 本身**保留**：它走 `post()` 会经 `request()`，而 `request()` 在 401 时又会调本函数，直接用会无限递归。 |
| 幽灵方法 | `practiceStore.js` | `copybookAPI.addPracticeRecord(...)` —— api.js 里没有这个方法，运行时直接 TypeError。→ 改调 `copybookAPI.recordPractice(copybookId, data)`（**两个参数**） |
| 幽灵方法 | `practiceStore.js` | `copybookAPI.getPracticeHistory(...)` —— api.js 里没有。→ P6 在 api.js 补出 `getPracticeHistory: (params) => get('/copybooks/practice/history', params)` |
| 幽灵方法 | `practiceStore.js` | `copybookAPI.getPracticeStats(...)` —— 不存在。→ 改调 `copybookAPI.getUserStatistics()`；同时 api.js 新增 `statsAPI.getOverview`（契约端点 53，前端原先没接） |
| **响应形状当数组用** | `practiceStore.js` | `response.map(...)` —— 但 `GET /api/copybooks` 返回分页信封，**没有 `.map`**，运行时 TypeError。→ 改 `response.content.map(...)`；`copybookStore` 的 5 个取数函数原本全在读旧的 `response.data.xxx`，一并改掉 |
| 字段名对不上 | `practiceStore.js` | 读 `record.practiceTime` / `record.charactersPracticed` / `record.copybook?.title` → 契约是 **`duration`（秒）** / **`characterCount`**，且**没有 `copybook` 嵌套字段** |
| 字段名对不上 | `practiceStore.js` | 读 `response.totalPracticeTime` / `totalWorks` / `totalCharactersPracticed` / `lastPracticeDate` → 契约是 `totalPractices` / `totalDuration` / `averageDuration` / `streakDays` / `characterCount` / `copybookCount` |
| store 动作名不符 | `userStore.js` | 视图调 `userStore.updateProfile`，store 里定义的却是 `updateUserInfo` → 链路不可达。→ store 动作改名 `updateProfile`；响应按契约读成**裸对象**（不是 `response.user`） |
| store 动作缺失 | `userStore.js` | 视图调 `userStore.fetchPractices` / `fetchMyWorks`，store 里**没有** → 补上，返回分页信封 |
| 参数形状不对 | `ProfileView.vue` | `PUT /api/users/password` 发的是**裸 JSON 字符串** → 改发 `{oldPassword,newPassword,confirmPassword}` |
| URL 拼错 | `MyWorksView.vue` | `GET /api/works/user/{userId}` 请求的是 `/works/user/undefined` → 从 `userInfo.id` 取真实 userId |
| **mock 降级（9 处）** | `api.js` 5 处 + store 4 处 | `catch` 里返回假数据 → **全部删掉**。最危险的是 `authAPI.sendVerificationCode`：接口没通时前端会拿到一个**编出来的验证码**（`code: '123456'`），用户能登录成功 |
| **204 被当成错误** | `api.js` | `request()` 无条件 `await response.json()` → `PUT /api/users/password`、`DELETE /api/works/{id}`、两个 logout 成功返回 204 时，**成功也会抛「服务器响应格式错误」** → 加 `if (response.status === 204) return null` |
| **公开端点被当成私有** | `api.js` | `publicPaths` 只有 login/register/verification-code → 契约标公开的 `GET /api/works`、`/api/copybooks*`、`/api/stats/overview`、`/api/discussions*`、`/api/community/comments/work/*` 全部会因没令牌而**跳登录页**，未登录用户连公开画廊都打不开 → 改成按契约的 `security: []` 列正则匹配 |
| **令牌存取格式不一致** | `userStore.js` / `api.js` | `utils/storage.js` 的 `setStorage` 会 `JSON.stringify`，令牌存成 `"eyJ..."`（带引号），而 `api.js` 读的是裸值 → 请求头变成 `Bearer "eyJ..."` → **所有鉴权请求 401**。→ store 改用 api.js 的 `setToken`/`setRefreshToken`（裸串），`getToken` 兼容两种格式 |
| **userType 同样被 JSON 编码** | `userStore.js` / `api.js` | `setStorage('userType','ADMIN')` → localStorage 里是 `"ADMIN"`，`getUserType()` 原样比较 `=== 'ADMIN'` 不成立 → 管理员的 401 自动续期会打到 `/frontend/auth/refresh`。→ 只用 api.js 的 `setUserType`（裸值），`getUserType` 兼容两种格式 |
| **刷新页面登录态丢失** | `App.vue` | 没人调 `userStore.initUserFromStorage()` → 刷新后 `userInfo` 为空、`isLoggedIn` 为 false。→ App.vue 的 `onMounted` 里调一次；顺带修 `userInfo.username` 未登录时直接取属性会抛错 |
| **管理员登录失败会污染 userType** | `userStore.js` | `adminLogin` 在**发请求之前**就把 userType 设成 BACKEND，失败不回滚 → 前端用户的令牌续期会打 `/backend/auth/refresh`。→ 改成登录成功后才切 |
| **Error 不带状态码** | `api.js` | `new Error(message)` 不带 status → 视图没法程序化区分 401/403/404，只能靠中文文案匹配。→ 统一抛带 `status` 与 `code` 的 Error |
| **查询参数把 undefined 变成字面量** | `api.js` | `new URLSearchParams({category: undefined})` → `category=undefined` → 后端收到一个值不是预期的参数。→ `get()` 先滤掉 undefined/null/空串 |
| **无端点的假功能** | `AdminView.vue` / `CommunityView.vue` | 用户列表 / 分类管理 / 系统设置 / 活动 / 学习小组 / 用户评价 —— 契约里**没有**这些端点。→ 整块删除或换成有端点的等价物（社区公告 → 置顶讨论），**不拿假数据凑功能** |

**9 处 mock 降级分支已全部删除**（api.js 里 5 处 `catch` 返回假数据 + store 里 4 处：
`appStore.js` 的统计/公告/热门标签 3 处 + `practiceStore.js` 的推荐内容备选 1 处）。
现在全仓 grep `mock` / `模拟` / `假数据` / `picsum.photos` / `via.placeholder.com` **零命中**。

## 5. 当前差异清单

`npm run contract:check` 当前输出：

```
contract  : 56 operation(s)
frontend  : 56 call site(s)
backend   : 56 controller route(s)
violations: 0
gaps      : 0
warnings  : 0
```

**这一节原来是「待实现清单」**（P1 起草时 violations 0 / gaps 27 / warnings 9）。
P6 接完线之后已经清零，现在它只起「回归基线」的作用：

- **违规 0** —— 契约与现有代码没有矛盾。
- **缺口 0** —— 契约里的 56 个端点，前端 56 个调用点一个不差地对上了。
- **警告 0** —— 没有「前端有调用但后端无路由」，也没有「后端有路由但契约里没有」。

`--strict` 模式同样零差异，**这条命令是 CI 的门禁**：任何一边单方面改动都会被拦下。

**已完成的域**：P2 auth（端点 1–10）、P3 user + works + stats（端点 11–21、53）、
P4 copybooks + practice（端点 22–27）、
P5 comments + discussions + file-reviews（端点 28–52）、
P8 补待审队列（端点 54）、
P10 补云端字帖（端点 55、56）——
后端路由数从 0 变成 **56**，**56 个端点全部实现完毕**。

## 6. 阶段划分与验收

| 阶段 | 覆盖 | 验收 | 状态 |
|---|---|---|---|
| **P1 契约定稿** | `contract.yaml` + 本文件 + `check-contract.mjs` | 校验脚本通过，差异清单即待实现清单 | ✅ 完成 |
| **P2 auth 域** | 端点 1–10 | 登录 → 带 Bearer → refresh → logout 全链路通；契约测试 10 项绿 | ✅ 完成 |
| **P3 user + works + stats 域** | 端点 11–21、53 | 上传作品 → 可见 → 图片能取回；契约测试 12 项绿 | ✅ 完成 |
| **P4 copybooks 域** | 端点 22–27 | 字帖列表/详情/记录练习/统计/历史；`copybooks` 既有 3 行数据可读 | ✅ 完成（17 项契约测试 + 36 项活体） |
| **P5 comments + discussions + file-reviews 域** | 端点 28–52 | 发帖/回复/点赞/评论/审核；契约测试绿 | ✅ 完成（17 + 35 + 20 = 72 项契约测试 + 70 项活体） |
| **P6 前端接线 + 删 mock** | §3.5、§4 | 六个视图无硬编码 mock；api.js 无 mock 降级分支 | ✅ 完成（mock 零命中 + strict 零差异 + eslint 零问题） |
| **P7 全量验证** | — | 契约测试全绿；`--strict` 零差异；前后端联调冒烟通过 | ✅ 完成（130 测试 + 181 活体 + 浏览器真机） |
| **P8 补缺口 + UI 打磨** | 端点 54、`AdminView.vue`、`router/index.js` | 待审队列可用；硬刷新受保护页不再被踢去登录页 | ✅ 完成（136 测试 + 194 活体 + 浏览器真机 4 条守卫分支） |

## 7. 变更记录

- 2026-10-21 初稿（P1）。53 个端点全部按前端实际调用点 + 后端 DTO/实体字段推导，
  不是手写臆测。前端 51 个调用点与 spec §7 清单**逐条吻合**（method、path、路径参数名全对），
  差的正好是 spec 标注为「新增」的两个端点。
- 2026-10-21 P4（copybooks 域）。后端路由 22 → 28，缺口 33 → 27。
  同时把 `PracticeRecordService` 从 58 个方法裁剪到 5 个（每个都有调用方），
  删掉零引用的 `PracticeRecordUpdateDto`。
  **练习记录的字段名对齐到契约**：`duration`（秒）、`characterCount`、`fileUrl`、
  主键 `id`（不是 `recordId`）—— 旧实体那套
  `practiceTime`（分钟）/ `charactersPracticed` 整体作废，
  数据库用 `SpringBoot-Platform/db/03-practice-records-contract.sql` 跟上。
- 2026-10-21 P5（社区域）。**53 个端点至此全部实现**，后端路由 28 → 53，
  缺口 27 → 2（剩下 2 条都是前端接线），桩警告 9 → 3（后端已无桩）。
  三个子域并行施工，文件归属互不重叠：
  - **comments（4 端点）**：`CommentService` 45 → 5 个方法；
    `containsSensitiveContent` **真实现**（敏感词清单来自配置
    `comment.moderation.sensitive-words`，唯一真相源在 `application.properties`），
    命中敏感词的评论落到 `PENDING` 不自动放行。
  - **discussions（14 端点）**：`DiscussionService` 37 → 14 个方法；
    挖出 **`DiscussionServiceImpl` 的 `@Service` 原本被注释掉了**（服务 Bean 根本不存在）；
    点赞去重新建 `discussion_likes` 表（`db/04-discussion-likes.sql`）。
  - **file-reviews（7 端点）**：`FileReviewService` 13 → 7 个方法；
    审核结果回写 `works.review_status`（并联动 `works.status`）。
    已知妥协：`file_reviews.reviewer_id` 的外键仍指向历史遗留的 `backend_users` 表，
    服务层为管理员补一条「影子审核员记录」来满足外键。

  顺带更正：§4 里写的「store 里 2 处 mock 降级」实际是 **4 处**
  （`appStore.js` 3 处 + `practiceStore.js` 1 处）。
- 2026-10-21 P6（前端接线 + 删 mock）。契约检查达到 **violations 0 / gaps 0 / warnings 0**
  （前端调用点 51 → 53，`--strict` 也零差异）。
  **9 处 mock 降级全部删除**（api.js 5 处 + store 4 处）——
  最危险的是 `authAPI.sendVerificationCode`：接口没通时前端会拿到一个
  **编出来的验证码**（`code: '123456'`），用户能登录成功。
  六个视图的硬编码假数据全部换成真实调用，**每个数据区都有加载中 / 空 / 错误三态**。
  施工中又挖出 6 个**阻断级前端缺陷**（都已修）：
  204 空响应体被当成错误、公开端点被当成私有而强制跳登录页、
  令牌与 userType 的存取格式不一致导致**所有鉴权请求 401**、
  刷新页面登录态丢失、Error 不带状态码、管理员登录失败会污染 userType。
  顺带记下三个**后端缺口**（P7 登记，暂不实现）：
  没有「待审核作品列表」端点、没有用户列表端点、没有评论全量列表端点。
- 2026-10-21 P7（全量验证与收口）。
  130 项后端测试（干净构建）+ 181 项活体检查 + CORS 预检 10/10 + 浏览器真机联调，
  全部通过，**测试数据零残留**。
  浏览器真机联调抓到一个**静态检查发现不了**的 bug：
  **刷新页面无限循环** —— `App.vue` 的 `onMounted` 调 `initUserFromStorage()`，
  而它在「没有令牌」时调 `this.logout()`，后者去打一个**需要令牌**的
  `/auth/logout`，api.js 因无令牌硬跳登录页 → 页面重载 → 又走这里 ⇒ 死循环。
  已改为只清本地状态。
  同时整份重写了 `SpringBoot-Platform/README.md`（原版库名、默认用户、
  API 路径全是事故前的形态，照着跑一定跑不通）。
- 2026-10-21 P8（补后端缺口 + UI 打磨 + 修路由守卫竞态）。
  - **补端点 54 `GET /api/admin/works/pending`（`adminPendingWorks`）**。
    P6 登记的三个后端缺口里，只有「待审核作品列表」是**「UI 已有功能但根本跑不起来」**：
    公开画廊 `GET /api/works` 只返回已发布且已审核通过的作品，而刚上传的作品是
    DRAFT+PENDING，**永远不在列表里** —— 后台的「作品审核」列表等于废的。
    待审定义沿用实体 `Work.canBeReviewed()`（PENDING + NEEDS_REVISION），不另造口径。
    `AdminView` 的作品列表加了「待审队列 / 已发布作品」切换，按 scope 选端点。
  - **修路由守卫竞态**：硬刷新 `/admin` 会被踢去 `/admin/login` ——
    守卫在 `initUserFromStorage()` 完成前就判断 `isLoggedIn`，那时 Pinia 还是空的。
    改法：`userStore` 加 `initialized` 状态并给初始化加**幂等 + 并发闸门**，
    守卫 `await userStore.initUserFromStorage()` 之后再判权限。
    真机验过四条分支：管理员硬刷新 `/admin` 原地不动、
    普通用户硬刷新 `/my-works` 原地不动、已登录访问 `/login` 弹回首页、
    匿名访问 `/admin` 带 `redirect` 跳 `/admin/login`。
  - **UI 打磨**：主色锁死 `#8B4513` 并映射到 Element Plus 的 `--el-color-primary` 系；
    语义色拉进墨色家族（成功=黛绿、警告=赭黄、危险=朱砂）；
    页面底色改宣纸色 `#f4efe2`；13 处 `transition: all` 换成明确属性；
    首页 emoji 换手绘水墨 SVG；「平台数据」3 张卡改印章数字样式。
  - 回归：136 项后端测试 + 194 项活体检查 + 契约 54/54/54 零差异 + eslint 零问题。
- 2026-10-23 P9（**全量审计 + 安全与正确性修复**）。契约路径层面一直是干净的
  （54/54/54、零差异），但契约**不校验方法体是否真干活**，这一轮补上了这一层。
  审计方式：逐端点追 Service 实现方法体、逐视图追 UI→接口映射，
  再用活体接口复核每一条结论。
  - **修复的「界面有、后端没有 / 不生效」**：
    - `GET /api/works/user/{userId}` **忽略 page/size** —— 以前 `findByUserId` 返回全量
      List 再塞进 `PageImpl`，`content` 装的是全部作品，而 `size`/`totalPages` 按 pageable 算，
      **信封自相矛盾**（实测 `?size=1` 返回 3 条）。改为仓储层 `Page<Work> findByUserId(String, Pageable)`。
    - 作品**浏览量恒为 0**：类级 `@Transactional(readOnly=true)` 下「改实体再 save」
      不会 flush（FlushMode.MANUAL），**响应里显示 1、库里永远是 0**。
      改为独立的 `@Modifying` 批量 UPDATE，并把自增后的真实值写回响应。
    - 公开作品列表**没有排序**，翻页可能重复或漏掉。改为 `createdAt DESC, id DESC`。
  - **修复的越权与鉴权**：
    - `POST /api/works/{workId}/image` **完全没有归属校验** —— 任意登录用户可改他人作品主图。
    - 越权改/删作品抛 JDK 的 `SecurityException`，全局异常处理没有这个分支，
      掉进兜底变成 **500**，而契约声明的是 403。改抛 Spring 的 `AccessDeniedException`，
      并给 `SecurityException` 补一条 403 兜底。
    - 管理员判定查的是 `backend_users` 表 —— 那张表只有「建过审核记录」的管理员才有行，
      结果 `users.role=ADMIN` 的**真管理员反而改不了别人的作品**。统一改判 `users.role`。
    - 两个 logout 端点在 `PUBLIC_ENDPOINTS` 里放行匿名，与契约「需登录」不符。
    - JWT 的 `userType` 声明**从未被校验** —— 前端登录拿到的令牌照样能访问 `/api/admin/**`。
      现在按路径族校验：访问 `/api/backend/**`、`/api/admin/**` 要求令牌 `userType=ADMIN`。
    - `GET /api/users/{userId}` 对任意登录用户暴露他人的 `lastLoginIp` /
      `failedLoginAttempts` / `lockedUntil`。现在仅本人与管理员可见。
    - 批量审核不校验作品是否可审；删除审核记录不校验归属，且不回滚 `works.review_status`。
  - **修复的假成功**：
    - `POST /api/auth/verification-code` 把码存进 Redis 就返回「已发送」，
      而 `spring.mail.*` 是占位配置（`smtp.example.com`），**邮件永远发不出去**；
      同时 `register` **从不校验**这个码。现在：填了验证码就必须与 Redis 里的码一致
      （一次性消费，用过即删），不匹配直接拒绝注册；不填验证码仍可注册，保持未验证状态。
    - `POST /api/works/{workId}/image` 以前**上传两次**（Controller 一次、Service 一次），
      磁盘上多一份孤儿文件，响应里的 `fileUrl` 指的根本不是作品主图。现在只上传一次，
      响应 URL 与入库 `image_url` 一致。
  - **前端修复**：42 处 Element Plus 1.x 的 `el-icon-*` 旧写法（2.x 已移除该 API，
    **图标一个都不渲染**）改为真实图标；Element Plus 改按需引入（首屏 JS gzip 522.86 → 193.42 kB，
    CSS 346.46 → 49.70 kB）；13 处纯 div 点击元素补可访问性与键盘操作；
    练习页格子默认值 `cross` 绘制代码不认、导致画布默认无格子，已修并补「田字格」。
  - **回归**：后端测试 136 → **173 项全绿**（新增 37 项，且逐条验证过「修复前是红的」）；
    契约 `--strict` 仍 54/54/54 零差异；eslint 零问题；活体复核分页 / 浏览量落库 /
    登出 401 / 换图 403 / 令牌类型 403 / 敏感字段脱敏 / 验证码校验全部通过。
  - **契约增量**：`workUpdateImage` 的 responses 增加 `'403'`（复用已有 `components/responses/Forbidden`）。
- 2026-10-23 P10（**把半实现的功能补成真功能**）。P9 审计出四类「界面有、后端只做了一半」的能力，
  本轮全部补齐，端点数 54 → 56：
  - **云端字帖**（新增端点 55 `POST /api/copybooks`、端点 56 `GET /api/copybooks/mine`）。
    以前「字帖生成」的结果只落在浏览器 localStorage，换设备就没了，界面却像存下来了。
    `copybooks` 表补自建字帖所需字段（`db/07-copybook-custom-columns.sql`，新增列全部可空
    或带默认值，老库启动不受影响）；公开字帖列表与热门榜**排除自建字帖**，
    自建字帖只出现在「我的字帖」里。
  - **点赞态**。`Discussion` 增加 `liked`，服务端按当前登录用户填充 —— 以前服务端不返回这个字段，
    前端「已赞」高亮只在当前会话内有效，一刷新就丢。填充时**批量查** `discussion_likes`
    一次取齐当前页（逐条查等于又造一个 N+1）。
  - **账号级偏好**。`FrontendUserUpdate` 增加 `preferences`（JSON 字符串）。`users.preferences` 列
    本来就存在、也一直被返回，但**从来没有写入路径** —— 个人资料页的「保存设置」只落在浏览器本地。
    现在写入并加 `@ValidJson` 校验（非法 JSON 直接 400）。
  - **服务端搜索 / 排序 / 筛选**。`GET /api/works`（keyword / sort / category）、
    `GET /api/works/user/{userId}`（keyword / sort）、`GET /api/admin/works/pending`（keyword）、
    `GET /api/copybooks/practice/history`（from / to / category / keyword）。
    以前这四处都是在前端对当前页数据做过滤，搜不到不在当前页的作品。
    筛选与排序全部**在数据库层**完成；排序都带 `id` 作次级键以保证翻页顺序稳定。
  - **回归**：后端测试 173 → **229 项全绿**（新增 4 个契约测试类），并逐条做过变异验证
    （故意改坏实现，对应用例转红，还原后转绿）；契约 `--strict` **56/56/56 零差异**；
    eslint 零问题；浏览器真机复验：云端字帖入库、偏好落账号并刷新回读、
    点赞态跨刷新保留、画廊搜索带 `keyword` 且结果由服务端筛选。
  - **顺带修掉的真缺陷**：保存偏好时若昵称为空，会被后端按契约的 `@NotBlank` 拒掉整个请求
    （400「昵称不能为空」）—— 于是**没设过昵称的账号永远存不了偏好**，而提示看不出原因。
    改为昵称为空时回落用户名。

