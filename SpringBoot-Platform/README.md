# 书法练习平台 · 后端（Spring Boot 3.2.5）

在线书法练习平台的后端实现。JDK 17 / Spring Boot 3.2.5 / Spring Security /
Spring Data JPA / MySQL 8 / Redis / MapStruct / Lombok。

> **权威文档在仓库根目录**，本文件只讲后端自己。
> - 契约（唯一权威）：[`../Practice-Platform/openapi/contract.yaml`](../Practice-Platform/openapi/contract.yaml)，54 个 operation
> - 人读版契约：[`../docs/api-contract.md`](../docs/api-contract.md)
> - 完整运行指南：[`../README.md`](../README.md)

---

## 1. 先配置密钥（**必做**）

**密钥不在版本库里。** 仓库里没有真实密钥；JWT 签名密钥一旦公开，
任何人都能伪造 `ROLE_ADMIN` 的令牌绕过全部鉴权。

```powershell
Copy-Item 'config\local-secrets.properties.example' 'config\local-secrets.properties'
```

编辑 `config/local-secrets.properties`，填两项：

```properties
JWT_SECRET=<至少 32 字符，建议 openssl rand -hex 32>
DB_PASSWORD=<本地 MySQL 密码>
```

`config/local-secrets.properties` **已被 .gitignore 忽略**，别提交它。

密钥**没有默认值** —— 缺失或短于 32 位时应用**启动即失败**：

```
IllegalStateException: JWT 密钥未配置。请设置环境变量 JWT_SECRET，
或用 run-local.ps1 启动（它会读 config/local-secrets.properties 并注入环境变量）。
```

---

## 2. 启动与测试

```powershell
.\run-local.ps1             # 启动（等价于 mvn -o spring-boot:run），端口 8083
.\run-local.ps1 -Task test  # 跑测试（当前 130 项）
.\run-local.ps1 -Task compile
```

`run-local.ps1` 把 `config/local-secrets.properties` 读成**环境变量**注入当前进程
（**从不回显**），再执行 Maven。直接跑 `mvn spring-boot:run` 的话，
必须自己先设好 `JWT_SECRET` 与 `DB_PASSWORD`。

---

## 3. 数据库

| 项 | 值 |
|---|---|
| 库名 | `calligraphy_practice` |
| 地址 | `127.0.0.1:3306` |
| 连接配置 | `application.properties` 的 `spring.datasource.*`，密码走 `DB_PASSWORD` 环境变量 |
| 表结构演化 | `ddl-auto=update`（新增实体字段会自动加列） |

**一次性/修正性 SQL 放在 `db/`**，按编号递增，**幂等**，手工执行：

| 脚本 | 作用 |
|---|---|
| `db/01-align-copybook-id.sql` | `practice_records.copybook_id` 从 bigint 改成 varchar(36) |
| `db/02-unify-user-fk.sql` | 6 张域表的 user 外键统一指向 `users` |
| `db/03-practice-records-contract.sql` | `practice_records` 对齐契约的 `PracticeRecord` schema |
| `db/04-discussion-likes.sql` | 点赞去重表 `discussion_likes` |
| `db/05-copybook-sample-images.sql` | 3 行播种字帖的封面图改指向真实存在的 `/sample/copybook-N.png` |

> `src/main/resources/recreate_schema.sql` 是**历史遗留**的「部分重建」脚本，
> 它的 `users` / `works` 列已经落后于当前实体定义，**不要**拿它重建线上库。

### 第一个管理员怎么来

**新装的库里没有任何管理员** —— 这是刻意的：
`src/main/resources/dual_user_schema.sql` 里的默认超级管理员口令已经被换成占位符
（`REPLACE_WITH_BCRYPT_HASH_OF_YOUR_OWN_PASSWORD`），
而 `spring.sql.init.mode=never` 又意味着这个文件**根本不会被执行**
（表结构由 `ddl-auto=update` + `db/` 下的手工脚本维护）。

所以第一个管理员要自己建，两步：

```powershell
# 1. 正常注册一个用户（前端注册页，或 POST /api/frontend/auth/register）
# 2. 把它的角色提上去
& "E:\MySQL\MySQL Server 8.0\bin\mysql.exe" -h 127.0.0.1 -P 3306 -uroot -p123456 `
  calligraphy_practice -e "UPDATE users SET role='SUPER_ADMIN' WHERE username='你的用户名';"
```

提完角色后用 `/admin/login` 登录即可（`POST /api/backend/auth/login`）。
注意 `users.role` 的枚举是 `USER / ADMIN / SUPER_ADMIN`，管理员与前端用户**同表**，
靠 role 区分。

---

## 4. 端点（54 个，全部已实现）

以 `openapi/contract.yaml` 为准，这里只列分组与鉴权：

| 分组 | 端点数 | 鉴权 |
|---|---|---|
| `/api/frontend/auth/*`、`/api/backend/auth/*`、`/api/auth/verification-code` | 10 | 登录/注册/验证码公开，其余要令牌 |
| `/api/users/*` | 5 | 要令牌（`GET /api/users/{id}` 需令牌） |
| `/api/works*` | 7 | `GET /api/works` 公开，其余要令牌 |
| `/api/copybooks*` | 6 | 列表/热门/详情公开；统计/历史/记录练习要令牌 |
| `/api/community/comments*` | 4 | 按作品查公开，其余要令牌 |
| `/api/discussions*` | 14 | 浏览公开，写操作要令牌 |
| `/api/admin/file-reviews*` | 7 | **需要 ADMIN / SUPER_ADMIN** |
| `/api/admin/works/pending` | 1 | 待审队列，**需要 ADMIN / SUPER_ADMIN** |
| `/api/stats/overview` | 1 | 公开 |

**响应形状**（见契约 §1.1）：

- 成功 + 单资源 → 裸对象，**没有 `data` 信封**
- 成功 + 列表 → `{content, page, size, total, totalPages}`
- 失败 → `{code, message}`

**主键字段一律叫 `id`**（不是 `workId` / `commentId`）。

---

## 5. 代码结构

```
src/main/java/com/calligraphy/platform/
  controller/            HTTP 入口层
  service/ + service/impl/   业务层（接口与实现分离）
  repository/            数据访问层
  model/ + entity/       JPA 实体
  dto/                   请求/响应体
  mapper/                MapStruct 映射
  exception/             统一错误处理（GlobalExceptionHandler）
  config/                安全、CORS、静态资源、JWT 属性
  security/              JWT 过滤器与鉴权入口点
  converter/             JPA AttributeConverter
```

**三条容易踩的约定**：

1. **响应形状**只有三种（裸资源 / 分页信封 / `{code,message}`），
   `GlobalExceptionHandler` 已经把异常映射好了，控制器**不要**自己拼错误体。
2. **LAZY 关联**：凡是映射 DTO 时会读关联字段的服务实现，类上加
   `@Transactional(readOnly = true)`，写方法自己加 `@Transactional`。
   否则事务外访问会抛 `LazyInitializationException`。
3. **接口裁剪**：契约里没有对应端点的 service 方法，**连同实现一起删掉**，
   不要留着当死代码。留着的桩要么真实现，要么删掉。

---

## 6. 数据库迁移与运维脚本

| 脚本 | 作用 |
|---|---|
| `scripts/mojibake.py` | 编码污染检测与修复（`detect` / `repair` / `selftest`） |
| `scripts/check_messages.py` | DTO 校验消息一致性检查 |
| `db/*.sql` | 见 §3 |

---

## 7. 注意事项

1. 生产环境务必换掉 `JWT_SECRET`，并用 HTTPS。
2. `config/local-secrets.properties` 不要提交。
3. `/api/admin/**` 的角色门禁在 `SecurityConfig` 里（`hasAnyRole("ADMIN","SUPER_ADMIN")`），
   **没有**用注解式鉴权 —— 加新管理端点时不需要加 `@PreAuthorize`。
4. 契约是唯一真相源：新增端点先改 `openapi/contract.yaml` → 再写后端 → 最后接前端，
   并跑 `npm run contract:check`（漂移会让前端构建失败）。
