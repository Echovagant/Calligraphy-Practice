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

[功能特性](#-功能特性) · [界面预览](#-界面预览) · [技术栈](#-技术栈) · [快速开始](#-快速开始)

</div>

---

## ✨ 功能特性

**🖌️ 毛笔练习** — 基于 fabric.js 的手写画布，笔刷大小 / 颜色 / 硬度可调，笔画带真实的提按粗细变化；宣纸底色配米字格 / 方格 / 横线三种格线，写完一键存为作品。

**📄 字帖生成** — 输入任意文字，选字体、布局、字号与纸张配色，实时生成方格 / 横线 / 空白三种版式，可下载与翻页；内置 3 个精选字帖，记录练习时长与字数。

**🖼️ 作品展示** — 公开画廊按分页展示已通过审核的作品，支持按标题 / 作者检索与排序；作者可上传、编辑、删除自己的作品，并查看浏览量与评论。

**💬 社区交流** — 讨论区支持发帖、回复、点赞、置顶与精华标记，按分类和标签聚合；附带热门讨论、活跃用户、热门标签侧栏，以及作品评论与后台审核。

**👤 个人中心** — 修改资料与头像，查看自己的作品与练习记录，记录带时长 / 字数 / 难度统计。

**🛡️ 管理后台** — 作品审核分「待审队列」与「已发布作品」，审核结论实时回写作品状态；另有审核记录、评论管理、用户查询与数据分析看板。

**🔐 安全与契约** — JWT 双令牌（access + refresh）配合 Redis 黑名单，角色变更即时生效；前后端以一份 OpenAPI 3.1 契约为准，任何一端的改动都会在构建期被拦下。

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
| **契约** | OpenAPI 3.1 单一真相源 · 54 个端点 · 自研一致性检查器 · 人读版见 [`docs/api-contract.md`](docs/api-contract.md) |
| **工程** | JUnit 5 + MockMvc 契约测试（136 项）· ESLint + Prettier · Maven 3.9 / Node 20.19+ |

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

**仓库里不含任何真实密钥** —— JWT 签名密钥一旦泄露，任何人都能伪造管理员令牌。

```powershell
cd SpringBoot-Platform
Copy-Item 'config\local-secrets.properties.example' 'config\local-secrets.properties'
```

编辑 `config\local-secrets.properties`，填两项（该文件已被 `.gitignore` 忽略，不会进版本库）：

```properties
JWT_SECRET=<至少 32 字符，建议 openssl rand -hex 32>
DB_PASSWORD=<你的 MySQL 密码>
```

### 4. 启动后端（8083）

```powershell
cd SpringBoot-Platform
.\run-local.ps1              # 启动；脚本会把上面的密钥读进环境变量注入进程（从不回显）
.\run-local.ps1 -Task test   # 跑测试
```

> 测试跑在独立的 `calligraphy_practice_test` 库上（首次会自动建库），不会动到你的开发数据。

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

## 📝 License

[Apache License 2.0](LICENSE)
