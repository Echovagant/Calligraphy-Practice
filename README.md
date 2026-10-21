# Calligraphy-Practice

在线书法练习平台：Vue 3 + Spring Boot 3.2 的全栈项目。

前端 `Practice-Platform/`（Vue 3 + Vite 7 + Element Plus + Pinia + fabric.js），
后端 `SpringBoot-Platform/`（Spring Boot 3.2.5 / JDK 17 / MySQL 8 / Redis）。

> 项目当前处于**契约对齐阶段**（spec §8 的 P0–P8）。**P0–P8 已完成** ——
> 契约里的 **54 个端点全部实现完毕**，前端也全部接到真实后端
> （`npm run contract:check` 现为 violations 0 / gaps 0 / warnings 0）。
> 回归基线：136 项后端测试 + 194 项活体检查全绿。
> 完整进度见 [`docs/api-contract.md`](docs/api-contract.md) §6。

---

## 0. 环境要求

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 17+ | 项目 `target 17`；本机装了 21 也能编 |
| Maven | 3.9+ | 本机 `D:\DEVTools\apache-maven-3.9.6` |
| Node | 20.19+ / 22.12+ | 用了 Vite 7 |
| MySQL | 8.0 | 本机 3306，库 `calligraphy_practice` |
| Redis | 6+ | 本机 6379，JWT 黑名单与会话要用 |

---

## 1. 先配置密钥（**必做**）

**密钥不在版本库里。** 仓库里没有任何真实密钥，应用的 JWT 签名密钥
一旦公开，任何人都能伪造 `ROLE_ADMIN` 的令牌绕过全部鉴权。

```powershell
cd SpringBoot-Platform
Copy-Item 'config\local-secrets.properties.example' 'config\local-secrets.properties'
```

然后编辑 `config/local-secrets.properties`，填两项：

```properties
JWT_SECRET=<至少 32 字符，建议 openssl rand -hex 32>
DB_PASSWORD=<本地 MySQL 密码>
```

`config/local-secrets.properties` **已被 .gitignore 忽略**，别提交它。

---

## 2. 启动

### 后端（8083）

```powershell
cd SpringBoot-Platform
.\run-local.ps1            # 启动（等价于 mvn -o spring-boot:run）
.\run-local.ps1 -Task test # 跑测试
```

`run-local.ps1` 会把 `config/local-secrets.properties` 读成**环境变量**注入当前进程
（**从不回显**），再执行 Maven。直接跑 `mvn spring-boot:run` 的话，
必须自己先设好 `JWT_SECRET` 与 `DB_PASSWORD` 两个环境变量，否则启动会失败：

```
IllegalStateException: JWT 密钥未配置。请设置环境变量 JWT_SECRET，
或用 run-local.ps1 启动（它会读 config/local-secrets.properties 并注入环境变量）。
```

### 前端（5174）

```powershell
cd Practice-Platform
npm install
npm run dev
```

dev server 跑在 **5174**（5173 被本机另一个项目占着）。
`vite.config.js` 把 `/api` 代理到 `http://localhost:8083`，
所以后端没起时前端会报连接错误而不是静默失败。

### 冒烟

```powershell
Invoke-WebRequest http://127.0.0.1:8083/actuator/health   # {"status":"UP"}
```

### 第一个管理员

**新装的库里没有任何管理员**（默认口令已被去掉，种子 SQL 又不会被执行）。
注册一个用户后把角色提上去即可，详见
[`SpringBoot-Platform/README.md`](SpringBoot-Platform/README.md) §3「第一个管理员怎么来」。

```sql
UPDATE users SET role='SUPER_ADMIN' WHERE username='你的用户名';
```

---

## 3. API 契约

- **权威文件**：[`Practice-Platform/openapi/contract.yaml`](Practice-Platform/openapi/contract.yaml)
  （OpenAPI 3.1，54 个 operation）
- **人读版**：[`docs/api-contract.md`](docs/api-contract.md)
- **一致性检查**：

  ```powershell
  cd Practice-Platform
  npm run contract:check          # 报告差异；漂移会让构建失败
  npm run contract:check:strict   # 任何差异都失败
  ```

  差异报告自动生成到 `Practice-Platform/openapi/contract-diff.md`。

**规则**：新增端点先改 `contract.yaml` → 再写后端实现 → 最后接前端。

---

## 4. 目录

```
Practice-Platform/     前端（Vue 3 + Vite）
  src/utils/api.js         唯一 HTTP 客户端
  src/api/community.js     讨论区 14 个端点
  openapi/contract.yaml    契约（唯一权威）
  scripts/check-contract.mjs  契约一致性检查
SpringBoot-Platform/   后端（Spring Boot）
  src/main/java/com/calligraphy/platform/
    controller/            HTTP 入口层
    service/ + service/impl/  业务层
    repository/            数据访问层
    model/ + entity/       JPA 实体
    dto/                   请求/响应体
    exception/             统一错误处理
    config/                安全、CORS、静态资源
  scripts/mojibake.py      编码污染检测与修复
  scripts/check_messages.py  DTO 校验消息一致性
  db/                      一次性 SQL 修正脚本
  run-local.ps1            本地启动（注入密钥）
  config/                  本地密钥（已 gitignore）
```

---

## 5. 已知问题

见 [`docs/api-contract.md`](docs/api-contract.md) §4「前端现存缺陷」与 §5「当前差异清单」。
`npm run contract:check` 的输出里，`gaps` 是**待实现清单**，`warnings` 是**已知缺陷**。
