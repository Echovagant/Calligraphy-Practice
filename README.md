<div align="center">

<img src="docs/screenshots/home.png" alt="书法练习平台首页" width="900" />

# 书法练习平台 · Calligraphy Practice

**在线书法练习与交流平台** —— 毛笔手感练习、字帖一键生成、作品展示、社区互助。

[![Vue](https://img.shields.io/badge/Vue-3.5-42b883?style=flat-square)](https://cn.vuejs.org/)
[![Vite](https://img.shields.io/badge/Vite-7-646cff?style=flat-square)](https://vite.dev/)
[![Element%20Plus](https://img.shields.io/badge/Element%20Plus-2.11-409eff?style=flat-square)](https://element-plus.org/)
[![Spring%20Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6db33f?style=flat-square)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479a1?style=flat-square)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-6%2B-d82c20?style=flat-square)](https://redis.io/)
[![License](https://img.shields.io/badge/license-Apache%202.0-8B4513?style=flat-square)](LICENSE)

[快速开始](#-快速开始) · [功能特性](#-功能特性) · [界面预览](#-界面预览) · [技术栈](#-技术栈)

</div>

---

## ✨ 功能特性

**🖌️ 毛笔练习** — 基于 fabric.js 的手写画布，笔刷大小 / 颜色 / 硬度可调，笔画带真实的提按粗细变化；宣纸底色、米字格 / 方格 / 横线三种格线随时切换；写完可一键存为作品。

**📄 字帖生成** — 输入任意文字（或直接填入《静夜思》示例），选字体、布局、字号与纸张配色，实时生成方格 / 横线 / 空白三种版式的字帖，支持下载与翻页；内置 3 个精选字帖（楷书基础 / 行书技法 / 草书鉴赏）可记录练习时长与字数。

**🖼️ 作品展示** — 公开画廊按分页展示已审核通过的作品，支持标题 / 作者检索与排序；作者可上传、编辑、删除自己的作品，并查看浏览量与评论。

**💬 社区交流** — 讨论区支持发帖、回复、点赞、置顶与精华标记，按分类和标签聚合；有「热门讨论」「本页活跃用户」「热门标签」侧栏，另有作品评论与后台审核。

**👤 个人中心** — 修改资料与头像、查看自己的作品与练习记录，练习记录带时长 / 字数 / 难度统计。

**🛡️ 管理后台** — 作品审核（**待审队列**与已发布作品分开）、审核记录留痕、评论管理、用户查询与数据分析看板；审核结论实时回写作品状态。

**🔐 安全与契约** — JWT 双令牌（access + refresh）+ Redis 黑名单，角色变更即时生效；前后端以一份 OpenAPI 3.1 契约为准，构建期自动校验零漂移。

---

## 🖼️ 界面预览

<table>
<tr>
<td width="50%"><img src="docs/screenshots/practice.png" alt="毛笔练习" /></td>
<td width="50%"><img src="docs/screenshots/copybook.png" alt="字帖生成" /></td>
</tr>
<tr>
<td align="center"><b>毛笔练习</b> —— 提按笔锋与格线背景</td>
<td align="center"><b>字帖生成</b> —— 方格版式实时预览</td>
</tr>
<tr>
<td width="50%"><img src="docs/screenshots/gallery.png" alt="作品展示" /></td>
<td width="50%"><img src="docs/screenshots/community.png" alt="社区交流" /></td>
</tr>
<tr>
<td align="center"><b>作品展示</b> —— 公开画廊</td>
<td align="center"><b>社区交流</b> —— 讨论区</td>
</tr>
<tr>
<td colspan="2"><img src="docs/screenshots/admin.png" alt="管理后台" /></td>
</tr>
<tr>
<td colspan="2" align="center"><b>管理后台</b> —— 作品审核与待审队列</td>
</tr>
</table>

---

## 🛠️ 技术栈

| | 技术选型 |
|---|---|
| **前端** | Vue 3.5 · Vite 7 · Vue Router 4 · Pinia 3 · Element Plus 2.11 · fabric.js 6 · Three.js |
| **后端** | Spring Boot 3.2.5 · Spring Security · Spring Data JPA（Jakarta）· JWT（jjwt）· MapStruct · Lombok |
| **存储** | MySQL 8.0（用户 / 作品 / 社区 / 审核）· Redis 6+（令牌黑名单、缓存）· 本地文件（图片、头像） |
| **工程** | OpenAPI 3.1 契约 + 自研一致性检查器 · JUnit 5 + MockMvc 契约测试 · ESLint + Prettier |

---

## 🚀 快速开始

### 1. 环境要求

| 组件 | 版本 |
|---|---|
| JDK | 17+ |
| Maven | 3.9+ |
| Node.js | 20.19+ / 22.12+ |
| MySQL | 8.0 |
| Redis | 6+ |

### 2. 准备数据库

```sql
CREATE DATABASE calligraphy_practice DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 3. 配置密钥（必做）

**仓库里不含任何真实密钥**，应用的 JWT 签名密钥一旦泄露，任何人都能伪造管理员令牌。

```powershell
cd SpringBoot-Platform
Copy-Item 'config\local-secrets.properties.example' 'config\local-secrets.properties'
```

编辑 `config\local-secrets.properties`，填两项：

```properties
JWT_SECRET=<至少 32 字符，建议 openssl rand -hex 32>
DB_PASSWORD=<你的 MySQL 密码>
```

该文件已被 `.gitignore` 忽略，不会进版本库。

### 4. 启动后端（8083）

```powershell
cd SpringBoot-Platform
.\run-local.ps1              # 启动；脚本会把上面的密钥读进环境变量注入进程（从不回显）
.\run-local.ps1 -Task test   # 跑测试
```

> 直接跑 `mvn spring-boot:run` 也行，但要自己先设好 `JWT_SECRET` 与 `DB_PASSWORD` 两个环境变量，否则启动会明确报错：
> `IllegalStateException: JWT 密钥未配置`。

### 5. 启动前端（5174）

```powershell
cd Practice-Platform
npm install
npm run dev
```

`vite.config.js` 已把 `/api` 代理到 `http://localhost:8083`，前后端一起跑即可联调。
打开 <http://localhost:5174/>。

### 6. 创建第一个管理员

新装的库里**没有任何管理员**（默认口令已移除）。注册一个账号后把角色提上去：

```sql
UPDATE users SET role='SUPER_ADMIN' WHERE username='你的用户名';
```

JWT 过滤器每次请求都从库里重读角色，**提权后立即生效，无需重新登录**。

---

## 📁 目录结构

```
.
├── Practice-Platform/          前端（Vue 3 + Vite）
│   ├── openapi/contract.yaml   API 契约 —— 唯一权威，54 个 operation
│   ├── scripts/                契约一致性检查器
│   └── src/
│       ├── views/              13 个业务页面
│       ├── components/         通用组件
│       ├── stores/             Pinia 状态
│       ├── utils/api.js        唯一 HTTP 客户端
│       └── router/             路由与权限守卫
├── SpringBoot-Platform/        后端（Spring Boot 3）
│   ├── src/main/java/com/calligraphy/platform/
│   │   ├── controller/         HTTP 入口
│   │   ├── service/impl/       业务实现
│   │   ├── repository/         数据访问
│   │   ├── model/ + entity/    JPA 实体
│   │   ├── dto/                请求 / 响应体
│   │   ├── security/           JWT 过滤器与鉴权
│   │   └── exception/          统一错误处理
│   ├── src/test/               136 项契约与单元测试
│   ├── db/                     数据库修正脚本
│   └── run-local.ps1           本地启动（注入密钥）
└── docs/
    ├── api-contract.md         人读版 API 契约
    └── screenshots/            README 配图
```

---

## 📄 API 契约

前后端以一份 **OpenAPI 3.1** 契约为唯一真相源，共 **54 个 operation**：

```powershell
cd Practice-Platform
npm run contract:check          # 输出差异报告
npm run contract:check:strict   # 任何差异都失败（构建门禁）
```

检查器会比对三处：契约里的端点、前端的调用点、后端的路由。任何一边单方面改动都会被 CI 拦下。

人读版契约见 [`docs/api-contract.md`](docs/api-contract.md)。

---

## 📝 License

[Apache License 2.0](LICENSE)
