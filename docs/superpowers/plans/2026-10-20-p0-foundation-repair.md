# P0 — 阶段 0：地基（编码修复 + 启动阻断 + 构建阻断） Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让后端从「完全无法编译」变成「能正常启动」，同时让前端从「构建失败」变成「构建通过」——这是后续所有阶段能开工的硬门槛。

**Architecture:** 先把 2025 年那次 UTF-8/GBK 编码事故造成的源码损坏彻底修好（这是当前一切编译失败的根因），再逐项补齐 Spring Boot 缺失的安全基座（`PasswordEncoder` / `SecurityFilterChain`）、统一三套 JWT 实现、停掉每次启动 DROP 四张表的 `spring.sql.init`，最后修掉前端 `CommunityView.vue` 的重复声明。

**Tech Stack:** Java 17 (Spring Boot 3.2.5)、Maven 3.9.6、Python 3（仅用于编码修复脚本）、Vue 3.5 + Vite 7、MySQL 8.0、Redis

**Spec:** `docs/superpowers/specs/2026-10-20-fullstack-contract-alignment-design.md`（§2 现状事实、§4 后端改造、§5 前端收敛、§8 阶段 0）

---

## Global Constraints

- **不要推送。** 远端 `https://github.com/Echovagant/Calligraphy-Practice.git` 是私有仓库，但本计划全程只在本地提交，不 `push`。
- **不引入新依赖。** `pom.xml` 保持原样（不加 Flyway / Lombok 之外的任何库）。本计划的新增代码只用 Spring Boot 3.2.5 已有的 starter（web / security / data-jpa / validation / test）。
- **不建 `controller` 包。** 阶段 0 只解决「能启动、能编译」，端点实现属于 P2–P4。
- **不改动数据库结构**，除 Task 4 中新建 `Copybook` 实体所需的 `db/01-align-copybook-id.sql`（手工执行一次）。
- **保留现有 19 张表和 `copybooks` 的 3 行数据**，不 DROP、不重建库。
- **前端 dev server 端口用 5174**（5173 已被另一个项目「城市智慧管理平台」占用），但阶段 0 只改 `vite.config.js` 的 proxy 目标端口，不改 dev 端口。
- **所有新增 Java 文件必须是 UTF-8 无 BOM**，且**不得再引入 GBK 编码事故**——写完后必须跑 `python scripts/mojibake.py detect`，退出码必须为 0。
- **提交粒度**：每个 Task 结束提交一次，提交信息用中文，**不加 `Co-Authored-By` trailer**。
- **不要动工作区里已有的 38 个已修改文件**（`git status` 中的 ` M`）。它们是本任务开始前就存在的行尾/编码改动，不属于本计划。提交时只 `git add` 本任务明确列出的文件。

---

## 背景：本计划要修的到底是什么

2025 年的一次批量编码事故把后端源码弄坏了。**当前 `mvn -o compile` 直接失败**，实测错误包括 `未结束的字符串文字`、`非法字符: '\ue178'`、`需要 class、interface、enum 或 record`。三处典型：

```
dto/DiscussionUpdateDto.java:7
    @Size(max = 200, message = "鏍囬闀垮害涓嶈兘瓒呰繃200涓涓绗?)
                                                  ↑ 引号被吃掉，字符串没闭合

dto/CommentDto.java:45
    // 瀹℃牳鑰呬俊鎭?    private String reviewerUsername;
                    ↑ 换行被吃掉，整条字段声明被注释吞了

config/JwtConfig.java:14
    private int refreshExpiration = 604800000; // 7澶?    private String header = "Authorization";
                                                   ↑ 同上，字段声明被吞
```

损坏是**有损**的（实测有字节被改写，如 `0xb8`→`0xbd`），所以**无法完全自动逆变换**。但其中三类损伤是可机械还原的，第四类需要语义判断——本计划 Task 1/2 精确覆盖这一点。

实测损伤规模（`scripts/mojibake.py detect` 的口径）：

| 损伤类型 | 规模 | 修复方式 |
|---|---|---|
| A 无损 CJK 片段（可逆） | 87 文件 / 1228 处 | 字节往返还原（自动） |
| B 字符串字面量丢失收尾引号 | 213 行 | `?` 还原为 `"`（自动） |
| C `//` 注释吞掉下一行代码 | 105 行 | `?` 还原为换行（自动） |
| D `?` 吞掉一个 ASCII 字符（数字等） | 约 400 行残留 | 需语义判断（Task 2） |

---

### Task 1: 乱码检测器与三类机械修复

**Files:**
- Create: `SpringBoot-Platform/scripts/mojibake.py`
- Create: `SpringBoot-Platform/scripts/README.md`

**Interfaces:**
- Consumes: 无（本计划第一个任务）
- Produces:
  - `python scripts/mojibake.py detect [--root DIR]` → 打印受污染文件统计，**干净时退出码 0**
  - `python scripts/mojibake.py repair [--root DIR] [--apply] [--report FILE]` → 应用规则 A/B/C，把无法自动判定的行写进报告
  - `python scripts/mojibake.py selftest [--root DIR]` → 自检，**必须退出码 0**
  - 供 Task 2 使用的报告文件 `--report` 指向的 Markdown，格式为 `UNRESOLVED <相对路径>:<行号>` + 该行内容

- [ ] **Step 1: 创建 `SpringBoot-Platform/scripts/mojibake.py`**

写入以下完整内容（这是本任务的核心交付物，规则 A/B/C 已实测验证）：

```python
#!/usr/bin/env python3
"""
Detect and repair UTF-8-read-as-GBK mojibake in Java sources.

Background
----------
Source files were UTF-8, got decoded as GBK (lossy), then re-encoded as UTF-8.
GBK trail bytes below 0x40 are invalid, so any ASCII byte in that range that
followed a GBK lead byte was replaced by '?'. That single marker explains all
four damage classes:

  A  lossless CJK run  -> reversible: run.encode('gbk').decode('utf-8')
  B  '?' ate a '"'     -> a string literal lost its closing quote
  C  '?' ate a '\\n'   -> a // comment swallowed the statement on the next line
  D  '?' ate an ASCII char (digit etc.) -> needs a byte-level search

Usage
-----
  python scripts/mojibake.py selftest
  python scripts/mojibake.py detect  [--root DIR]
  python scripts/mojibake.py repair  [--root DIR] [--apply] [--report FILE]
"""
import argparse
import io
import os
import re
import sys

CJK_RUN = re.compile(r"[\u3000-\u9fff\uff00-\uffef]+")
CJK_ONLY = re.compile(
    r"^[\u3000-\u9fff\u2018\u2019\u201c\u201d\u3001\u3002\uff01\uff0c"
    r"\uff1a\uff1b\uff08\uff09\u00b7\u2014\-\s]+$"
)
# a '?' immediately followed by one of these means a swallowed code statement
CODE_AFTER_Q = re.compile(
    r"^\s*(?:java\.|com\.|org\.|this\.|new\s|return\s|private\s|public\s"
    r"|protected\s|static\s|final\s|int\s|long\s|String\s|boolean\s|void\s"
    r"|List<|Map<|Optional<|@|import\s|package\s)"
)
# a '?' followed by one of these means a swallowed closing quote / delimiter
QUOTE_AFTER_Q = re.compile(r"^\s*[),;:'\"}\]]")

SRC_DIRS = ("src/main/java", "src/main/resources")
EXTENSIONS = (".java", ".sql", ".properties")


def odd_quotes(line):
    n, i = 0, 0
    while i < len(line):
        if line[i] == "\\":
            i += 2
            continue
        if line[i] == '"':
            n += 1
        i += 1
    return n % 2 == 0


def has_odd_quotes(line):
    return not odd_quotes(line)


def guarded_roundtrip(run):
    """Reverse the transform for a run that lost no bytes; None if not reversible."""
    try:
        fixed = run.encode("gbk").decode("utf-8")
    except (UnicodeEncodeError, UnicodeDecodeError):
        return None
    if fixed == run:
        return None
    # both sides must look like real CJK text, else it is a false positive
    if not CJK_ONLY.match(fixed) or not CJK_ONLY.match(run):
        return None
    return fixed


def restore_runs(text):
    """Rule A: restore every lossless CJK run."""
    return CJK_RUN.sub(lambda m: guarded_roundtrip(m.group(0)) or m.group(0), text)


def recover_single_char(line, qpos):
    """Find the character the '?' at qpos stands for.

    Encode the line to GBK, put each byte value in that slot, and keep the
    substitutions that make the whole line valid UTF-8.
    """
    try:
        head_b = line[:qpos].encode("gbk")
        tail_b = line[qpos + 1:].encode("gbk")
    except UnicodeEncodeError:
        return None
    results = []
    for b in range(0x00, 0x100):
        try:
            text = (head_b + bytes([b]) + tail_b).decode("utf-8")
        except UnicodeDecodeError:
            continue
        results.append(text)
    if not results:
        return None
    if len(results) == 1:
        return results[0]
    balanced = [t for t in results if odd_quotes(t)]
    if len(balanced) == 1:
        return balanced[0]
    return None  # ambiguous -> a human has to choose


def resolve_question_marks(line):
    """Rules B/C/D. Returns (repaired_line, [unresolved_offsets])."""
    out = []
    unresolved = []
    i = 0
    while i < len(line):
        ch = line[i]
        if ch != "?":
            out.append(ch)
            i += 1
            continue
        rest = line[i + 1:]
        if CODE_AFTER_Q.match(rest):
            out.append("\n")          # rule C: the '?' ate a newline
            i += 1
            continue
        if QUOTE_AFTER_Q.match(rest):
            out.append('"')            # rule B: the '?' ate a closing quote
            i += 1
            continue
        cand = recover_single_char(line, i)
        if cand is None:
            unresolved.append(i)
            out.append("?")
            i += 1
        else:
            out.append(cand)
            i += 1
    return "".join(out), unresolved


def java_files(root):
    for rel_dir in SRC_DIRS:
        base = os.path.join(root, rel_dir)
        if not os.path.isdir(base):
            continue
        for dirpath, dirnames, filenames in os.walk(base):
            dirnames[:] = [d for d in dirnames if d != "target"]
            for fn in sorted(filenames):
                if fn.endswith(EXTENSIONS):
                    yield os.path.join(dirpath, fn)


def process_text(text):
    """Returns (new_text, unresolved_lines)."""
    out_lines = []
    unresolved = []
    for lineno, line in enumerate(text.splitlines(), 1):
        restored = restore_runs(line)
        repaired, unres = resolve_question_marks(restored)
        if unres:
            unresolved.append((lineno, repaired))
        out_lines.append(repaired)
    new_text = "\n".join(out_lines) + ("\n" if text.endswith("\n") else "")
    return new_text, unresolved


def cmd_detect(args):
    root = args.root
    total_runs = 0
    total_odd = 0
    affected = []
    for path in java_files(root):
        with open(path, "rb") as fh:
            text = fh.read().decode("utf-8", errors="replace")
        runs = sum(1 for m in CJK_RUN.finditer(text) if guarded_roundtrip(m.group(0)))
        odd = sum(1 for l in text.splitlines() if has_odd_quotes(l))
        if runs or odd:
            total_runs += runs
            total_odd += odd
            affected.append((os.path.relpath(path, root), runs, odd))
    affected.sort(key=lambda x: (-x[1], -x[2]))
    print(f"files with mojibake  : {len(affected)}")
    print(f"reversible CJK runs   : {total_runs}")
    print(f"lines with odd quotes : {total_odd}")
    for rel, runs, odd in affected[:40]:
        print(f"  runs={runs:4d} odd={odd:4d}  {rel}")
    return 0 if not affected else 1


def cmd_repair(args):
    root = args.root
    report = io.StringIO()
    report.write("# Mojibake repair report\n\n")
    changed_files = 0
    unresolved_total = 0
    for path in java_files(root):
        with open(path, "rb") as fh:
            text = fh.read().decode("utf-8")
        new_text, unresolved = process_text(text)
        if new_text == text:
            continue
        changed_files += 1
        if args.apply:
            with open(path, "wb") as fh:
                fh.write(new_text.encode("utf-8"))
        rel = os.path.relpath(path, root)
        for lineno, line in unresolved:
            unresolved_total += 1
            report.write(f"UNRESOLVED {rel}:{lineno}\n    {line.strip()[:200]}\n\n")
    report.write(f"\nfiles changed    : {changed_files}\n")
    report.write(f"unresolved lines : {unresolved_total}\n")
    if args.report:
        with open(args.report, "w", encoding="utf-8") as fh:
            fh.write(report.getvalue())
    print(f"files changed    : {changed_files}")
    print(f"unresolved lines : {unresolved_total}")
    print("APPLIED" if args.apply else "DRY RUN (add --apply to write)")
    return 0


def cmd_selftest(args):
    failures = []

    # 1. false positives must not be flagged
    for word in ("\u804c\u4f4d", "\u72b6\u6001", "\u7528\u6237"):
        if guarded_roundtrip(word) is not None:
            failures.append(f"false positive on {word!r}")

    # 2. a real mangled run from the repo must be reversible
    sample = None
    for path in java_files(args.root):
        with open(path, "rb") as fh:
            text = fh.read().decode("utf-8", errors="replace")
        for m in CJK_RUN.finditer(text):
            if guarded_roundtrip(m.group(0)):
                sample = m.group(0)
                break
        if sample:
            break
    if sample is None:
        failures.append("no mangled run found in the repository")
    else:
        fixed = guarded_roundtrip(sample)
        if fixed is None or not CJK_ONLY.match(fixed):
            failures.append(f"roundtrip failed for {sample!r}")

    # 3. rule C: a // comment that swallowed a statement
    line = "// Builder \u7eb7?    public static class Builder {"
    fixed, unres = resolve_question_marks(line)
    if "public static class Builder {" not in fixed.split("\n")[-1]:
        failures.append(f"rule C failed: {fixed!r}")
    if unres:
        failures.append(f"rule C left unresolved: {unres}")

    # 4. rule B: a string literal that lost its closing quote
    line = '@Size(max = 200, message = "\u6807\u9898\u957f\u5ea6\u4e0d\u80fd\u8d85\u8fc7200\u4e2a\u5b57\u7b26?)'
    fixed, unres = resolve_question_marks(line)
    if has_odd_quotes(fixed):
        failures.append(f"rule B failed: {fixed!r}")
    if unres:
        failures.append(f"rule B left unresolved: {unres}")

    if failures:
        for f in failures:
            print("FAIL:", f)
        return 1
    print("selftest: all cases passed")
    return 0


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("command", choices=["detect", "repair", "selftest"])
    ap.add_argument("--root", default=".")
    ap.add_argument("--apply", action="store_true")
    ap.add_argument("--report")
    args = ap.parse_args()
    if args.command == "detect":
        return cmd_detect(args)
    if args.command == "repair":
        return cmd_repair(args)
    return cmd_selftest(args)


if __name__ == "__main__":
    sys.exit(main())
```

- [ ] **Step 2: 运行自检，确认退出码 0**

Run: `cd SpringBoot-Platform && python scripts/mojibake.py selftest --root .`
Expected: 输出 `selftest: all cases passed`，退出码 0

若自检失败：**不要继续**，先修 `mojibake.py`。三个用例分别守住：中文假阳性、规则 A 可逆、规则 C 换行、规则 B 引号。

- [ ] **Step 3: 先跑 dry-run，确认影响面**

Run: `cd SpringBoot-Platform && python scripts/mojibake.py repair --root . --report ../docs/mojibake-unresolved.md`
Expected: 打印 `files changed : <N>`（N > 100）与 `unresolved lines : <M>`（M ≈ 400），**不写任何源文件**

- [ ] **Step 4: 应用机械修复**

Run: `cd SpringBoot-Platform && python scripts/mojibake.py repair --root . --apply --report ../docs/mojibake-unresolved.md`
Expected: `APPLIED`；`git status` 中有大量 ` M src/main/java/...`

- [ ] **Step 5: 复查 diff 规模，确认没有误伤正常中文**

Run: `git diff --stat -- SpringBoot-Platform/src/main/java | Select-Object -Last 5`
Expected: 只应看到 `.java` 文件的改动，**不应出现 `.class` / `target/`**

再抽查 3 个文件，确认正常中文没被改坏：
Run: `git diff -- SpringBoot-Platform/src/main/java/com/calligraphy/platform/service/UserDetailsServiceImpl.java`
Expected: 原本正确的 `// 鍏堝皾璇曚粠鏅...` 等注释变成正常中文，原本的 `涓嶅瓨鍦?` 变成 `不存在：`（收尾引号补回）

- [ ] **Step 6: 提交**

```bash
git add SpringBoot-Platform/scripts/mojibake.py SpringBoot-Platform/scripts/README.md
git commit -m "chore: 增加编码污染检测与机械修复脚本"
```

> 注意：**这一步只提交脚本本身，不要提交被修复的源码**。源码修复是 Task 2 的交付物，单独提交，便于回滚和审查。

---

### Task 2: 修复残余乱码，让后端编译通过

**Files:**
- Modify: Task 1 修复后仍含 `?` 或乱码的 `SpringBoot-Platform/src/main/java/**/*.java`（约 40 个文件）
- Create: `SpringBoot-Platform/scripts/check_messages.py`

**Interfaces:**
- Consumes: Task 1 的 `scripts/mojibake.py` 与 `docs/mojibake-unresolved.md`
- Produces: `python scripts/check_messages.py` —— 校验「`@Size(max = N)` 的 N 与 message 里出现的 N 必须一致」；`mvn -o compile` 退出码 0

- [ ] **Step 1: 跑编译，拿到当前剩余错误**

Run: `cd SpringBoot-Platform && mvn -o compile`
Expected: 失败。记录错误条数。此时错误应从最初的数百条大幅下降（规则 B/C 已修掉绝大部分）。

- [ ] **Step 2: 写一致性校验脚本 `scripts/check_messages.py`**

这是修复 D 类损伤的**自动 oracle**：JSR-303 注解里的数字必须和消息文本里的数字一致。

```python
#!/usr/bin/env python3
"""Check that JSR-303 validation messages agree with their annotation limits.

A repaired message is only correct if the number it mentions matches the
limit in the annotation, e.g.
    @Size(max = 200, message = "标题长度不能超过200个字符")
The 200 must appear in the message.

The report is written to a UTF-8 file rather than printed: the mangled text
still contains characters such as U+20AC that the Windows GBK console cannot
encode, and printing them raises UnicodeEncodeError and aborts the scan.
"""
import argparse
import os
import re
import sys

BASE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "java")
SIZE_RE = re.compile(r"@Size\s*\(\s*(?:min\s*=\s*(\d+)\s*,\s*)?max\s*=\s*(\d+)\s*,\s*message\s*=\s*\"([^\"]*)\"")
RANGE_RE = re.compile(r"@Size\s*\(\s*min\s*=\s*(\d+)\s*,\s*max\s*=\s*(\d+)\s*,\s*message\s*=\s*\"([^\"]*)\"")
NUM_RE = re.compile(r"\d+")


def check(path):
    problems = []
    with open(path, encoding="utf-8") as fh:
        for lineno, line in enumerate(fh, 1):
            m = RANGE_RE.search(line)
            if m:
                lo, hi, msg = m.group(1), m.group(2), m.group(3)
                nums = set(NUM_RE.findall(msg))
                if lo not in nums or hi not in nums:
                    problems.append((lineno, line.strip(), f"expected {lo} and {hi} in message"))
                continue
            m = SIZE_RE.search(line)
            if m:
                hi, msg = m.group(2), m.group(3)
                if hi not in set(NUM_RE.findall(msg)):
                    problems.append((lineno, line.strip(), f"expected {hi} in message"))
    return problems


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--report", default="message-check-report.txt")
    args = ap.parse_args()

    report = []
    total = 0
    for dirpath, dirnames, filenames in os.walk(BASE):
        for fn in sorted(filenames):
            if not fn.endswith(".java"):
                continue
            path = os.path.join(dirpath, fn)
            for lineno, line, why in check(path):
                total += 1
                report.append(f"{os.path.relpath(path, BASE)}:{lineno}: {why}")
                report.append(f"    {line[:180]}")
    report.append("")
    report.append(f"problems: {total}")

    with open(args.report, "w", encoding="utf-8") as fh:
        fh.write("\n".join(report))
    print(f"problems: {total}  (report written to {args.report})")
    return 1 if total else 0


if __name__ == "__main__":
    sys.exit(main())
```

- [ ] **Step 3: 跑校验脚本，拿到需要人工判断的清单**

Run: `cd SpringBoot-Platform && python scripts/check_messages.py`
Expected: 打印 `problems: <N>  (report written to message-check-report.txt)`。**详细清单在报告文件里**——控制台只输出 ASCII，因为残存乱码里含有 `€`（U+20AC）这类 GBK 控制台无法编码的字符，直接打印会抛 `UnicodeEncodeError` 并中断整个扫描。

- [ ] **Step 4: 按语义逐条修复残余的 `?`**

对照 `docs/mojibake-unresolved.md` 与编译器报错，**逐行**修复。判断依据按优先级：

1. **同文件/同仓库的完整同类消息**。例如 `dto/DiscussionUpdateDto.java` 的消息应与 `dto/FileReviewRequestDto.java` 里未被破坏的同模板消息一致。
2. **注解里的数字**。`@Size(max = 200, message = "…200…")` → 消息里必须是 200。
3. **字段名**。`private String title` → 消息说「标题」；`private String category` → 「分类」；`private String tags` → 「标签」。

常见的完整消息模板（照抄这些，不要自创）：

| 触发场景 | 消息全文 |
|---|---|
| `@Size(max = N, ...)` 长度限制 | `X长度不能超过N个字符` |
| `@Size(min = A, max = B, ...)` 区间 | `X长度必须在A-B个字符之间` |
| `@NotBlank(...)` 非空 | `X不能为空` |
| `@Schema(description = "...")` | 直接写字段中文名，如 `审核状态` / `审核人信息` / `标题` / `分类` / `标签` / `用户名` / `验证码` / `记住我` / `密码` |

修完后 `?` 应当**一个不剩**（`'?'` 只能是合法的问号，但本仓库这些消息里没有问号）。

- [ ] **Step 5: 编译，必须通过**

Run: `cd SpringBoot-Platform && mvn -o compile`
Expected: `BUILD SUCCESS`，退出码 0

如果还报错，**继续按错误信息修**，直到 BUILD SUCCESS。不要跳到下一个 Task。

- [ ] **Step 6: 消息一致性校验必须通过**

Run: `cd SpringBoot-Platform && python scripts/check_messages.py`
Expected: `problems: 0`，退出码 0

- [ ] **Step 7: 检测器必须报告干净**

Run: `cd SpringBoot-Platform && python scripts/mojibake.py detect --root .`
Expected: `files with mojibake : 0`，退出码 0

- [ ] **Step 8: 提交**

```bash
git add -A SpringBoot-Platform/src/main/java
git add SpringBoot-Platform/scripts/check_messages.py
git commit -m "fix: 修复 2025 年编码事故造成的后端源码损坏

87 个文件的 UTF-8/GBK 二次编码污染被还原，213 处未闭合字符串字面量
与 105 处被注释吞掉的代码声明全部修复，后端恢复可编译状态。"
```

---

### Task 3: 停用每次启动 DROP 四张表的 `spring.sql.init`

**Files:**
- Modify: `SpringBoot-Platform/src/main/resources/application.properties:34-36`

**Interfaces:**
- Consumes: 无
- Produces: 应用启动时**不再**重建 `users` / `works` / `comments` / `practice_records` 四张表

- [ ] **Step 1: 写一个会失败的验收测试**

先在数据库里放一个哨兵行，再启动应用，看它还在不在。

Run（插入哨兵）:
```bash
& 'E:\MySQL\MySQL Server 8.0\bin\mysql.exe' -h 127.0.0.1 -P 3306 -u root -e "USE calligraphy_practice; INSERT INTO system_config (config_key, config_value) VALUES ('__p0_sentinel__', 'alive') ON DUPLICATE KEY UPDATE config_value='alive';"
```

> 若 `system_config` 表没有 `config_key` / `config_value` 两列，先执行 `DESCRIBE system_config;` 看实际列名，用实际列名插入。

- [ ] **Step 2: 启动应用，验证哨兵被清掉（这是预期的失败）**

Run: `cd SpringBoot-Platform && mvn -o spring-boot:run` （另开一个终端）
等待启动完成（`Started CalligraphyPlatformApplication`），然后 Ctrl+C 停掉。

Run（检查哨兵）:
```bash
& 'E:\MySQL\MySQL Server 8.0\bin\mysql.exe' -h 127.0.0.1 -P 3306 -u root -e "USE calligraphy_practice; SELECT * FROM system_config WHERE config_key='__p0_sentinel__';"
```

Expected: **查不到该行**（因为 `spring.sql.init.mode=always` + `recreate_schema.sql` 的 `DROP TABLE` 把它删了）

- [ ] **Step 3: 关掉 `spring.sql.init`**

把 `SpringBoot-Platform/src/main/resources/application.properties` 的第 34-36 行：

```properties
# 初始化数据库
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:recreate_schema.sql
```

改为：

```properties
# 数据库结构由 JPA ddl-auto=update 与 db/ 下的手工脚本维护。
# 历史上这里用的是 mode=always + recreate_schema.sql，每次启动都会 DROP
# users/works/comments/practice_records 四张表，导致数据被清空。
# 建表脚本保留在 src/main/resources/ 下供参考，但不再自动执行。
spring.sql.init.mode=never
```

- [ ] **Step 4: 重跑哨兵验证**

重复 Step 1 的插入 → Step 2 的启动 → Step 2 的检查。
Expected: **哨兵行还在**（`__p0_sentinel__ | alive`）

- [ ] **Step 5: 清理哨兵**

Run: `& 'E:\MySQL\MySQL Server 8.0\bin\mysql.exe' -h 127.0.0.1 -P 3306 -u root -e "USE calligraphy_practice; DELETE FROM system_config WHERE config_key='__p0_sentinel__';"`

- [ ] **Step 6: 提交**

```bash
git add SpringBoot-Platform/src/main/resources/application.properties
git commit -m "fix: 停用 spring.sql.init，避免每次启动清空四张业务表"
```

---

### Task 4: 补上 `Copybook` 实体并对齐 `practice_records.copybook_id`

**Files:**
- Create: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/model/Copybook.java`
- Create: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/repository/CopybookRepository.java`
- Create: `SpringBoot-Platform/db/01-align-copybook-id.sql`
- Modify: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/model/PracticeRecord.java:41-42`
- Test: `SpringBoot-Platform/src/test/java/com/calligraphy/platform/model/CopybookRepositoryTest.java`

**Interfaces:**
- Consumes: 现有 `copybooks` 表（19 张表中唯一有数据的表，3 行）
- Produces: `CopybookRepository extends JpaRepository<Copybook, String>`；`Copybook` 的 `id` 为 `String`（对应 `char(36)`）

- [ ] **Step 1: 写失败的测试**

创建 `SpringBoot-Platform/src/test/java/com/calligraphy/platform/model/CopybookRepositoryTest.java`：

```java
package com.calligraphy.platform.model;

import com.calligraphy.platform.repository.CopybookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CopybookRepositoryTest {

    @Autowired
    private CopybookRepository copybookRepository;

    @Test
    void existingCopybooksAreReadable() {
        assertEquals(3, copybookRepository.count(),
                "copybooks 表里已有 3 行播种数据，实体必须能读到它们");
    }

    @Test
    void copybookKeepsSeededValues() {
        Copybook copybook = copybookRepository.findAll().stream()
                .filter(c -> "楷书基础练习".equals(c.getTitle()))
                .findFirst()
                .orElse(null);
        assertNotNull(copybook, "应能按标题找到播种的字帖");
        assertEquals("beginner", copybook.getDifficulty());
        assertEquals("楷书", copybook.getCategory());
        assertEquals(100, copybook.getCharacterCount());
    }
}
```

- [ ] **Step 2: 跑测试，确认失败**

Run: `cd SpringBoot-Platform && mvn -o test -Dtest=CopybookRepositoryTest`
Expected: 编译失败，报 `找不到符号: 类 CopybookRepository`

- [ ] **Step 3: 创建 `Copybook` 实体**

创建 `SpringBoot-Platform/src/main/java/com/calligraphy/platform/model/Copybook.java`：

```java
package com.calligraphy.platform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 字帖。表结构已存在于数据库中（2025-10-31 播种了 3 行），
 * 但此前的源码里没有对应实体，实体缺失导致该表无法被访问。
 */
@Entity
@Table(name = "copybooks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Copybook {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 255, nullable = false)
    private String imageUrl;

    @Column(name = "difficulty", length = 20)
    private String difficulty;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "character_count")
    private Integer characterCount;

    @Column(name = "usage_count")
    private Integer usageCount;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 4: 创建 `CopybookRepository`**

创建 `SpringBoot-Platform/src/main/java/com/calligraphy/platform/repository/CopybookRepository.java`：

```java
package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.Copybook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CopybookRepository extends JpaRepository<Copybook, String> {

    List<Copybook> findByIsActiveTrueOrderByUsageCountDesc();

    List<Copybook> findByCategory(String category);
}
```

- [ ] **Step 5: 对齐 `practice_records.copybook_id` 的类型**

先执行手工脚本 `SpringBoot-Platform/db/01-align-copybook-id.sql`（内容如下，幂等）：

```sql
-- 把 practice_records.copybook_id 从 bigint 改成 varchar(36)，
-- 对齐 copybooks.id 的 char(36)。历史上这里类型不一致，
-- 导致 practice_records 无法通过 JPA 关联到 copybooks。
-- 幂等：列已经是 varchar(36) 时不做任何事。
USE calligraphy_practice;

SET @col_type = (
  SELECT DATA_TYPE FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'calligraphy_practice'
    AND TABLE_NAME = 'practice_records'
    AND COLUMN_NAME = 'copybook_id'
);

SET @sql = IF(@col_type = 'bigint',
  'ALTER TABLE practice_records MODIFY COLUMN copybook_id VARCHAR(36) NULL',
  'SELECT 1');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
```

Run: `& 'E:\MySQL\MySQL Server 8.0\bin\mysql.exe' -h 127.0.0.1 -P 3306 -u root -e "source SpringBoot-Platform/db/01-align-copybook-id.sql"`
Expected: 无报错

验证: `& 'E:\MySQL\MySQL Server 8.0\bin\mysql.exe' -h 127.0.0.1 -P 3306 -u root -e "SELECT COLUMN_NAME, DATA_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='calligraphy_practice' AND TABLE_NAME='practice_records' AND COLUMN_NAME='copybook_id';"`
Expected: `copybook_id | varchar`

- [ ] **Step 6: 把实体字段类型改成 `String`**

把 `SpringBoot-Platform/src/main/java/com/calligraphy/platform/model/PracticeRecord.java` 的第 41-42 行：

```java
    @Column(name = "copybook_id")
    private Long copybookId;
```

改为：

```java
    @Column(name = "copybook_id", length = 36)
    private String copybookId;
```

- [ ] **Step 7: 跑测试，确认通过**

Run: `cd SpringBoot-Platform && mvn -o test -Dtest=CopybookRepositoryTest`
Expected: `Tests run: 2, Failures: 0, Errors: 0`，`BUILD SUCCESS`

> 如果 `mvn test` 因为其他既有测试失败而整体红，**只看这个测试类的结果**；其它测试类的问题记到 Task 8 的待办里，不在本任务处理。

- [ ] **Step 8: 提交**

```bash
git add SpringBoot-Platform/src/main/java/com/calligraphy/platform/model/Copybook.java SpringBoot-Platform/src/main/java/com/calligraphy/platform/repository/CopybookRepository.java SpringBoot-Platform/src/main/java/com/calligraphy/platform/model/PracticeRecord.java SpringBoot-Platform/src/test/java/com/calligraphy/platform/model/CopybookRepositoryTest.java SpringBoot-Platform/db/01-align-copybook-id.sql
git commit -m "feat: 补上 Copybook 实体并对齐 practice_records.copybook_id 类型"
```

---

### Task 5: JWT 统一为一套

**Files:**
- Delete: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/security/JwtService.java`
- Delete: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/util/JwtTokenProvider.java`
- Delete: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/config/JwtConfig.java`
- Modify: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/security/JwtAuthenticationFilter.java`
- Modify: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/service/impl/AuthServiceImpl.java`（改注入类型）
- Test: `SpringBoot-Platform/src/test/java/com/calligraphy/platform/service/JwtServiceImplTest.java`

**Interfaces:**
- Consumes: `com.calligraphy.platform.service.JwtService`（接口，已存在，含 admin 相关方法）
- Produces: 唯一 JWT 实现 = `service/impl/JwtServiceImpl`；密钥唯一来源 = `application.properties` 的 `jwt.secret`

**背景说明（已核实）：** 仓库里有三套 JWT 实现，彼此不兼容：

| 实现 | 算法 | 状态 |
|---|---|---|
| `security/JwtService` | HS512 | 被 `AuthServiceImpl` + `JwtAuthenticationFilter` 使用 → **删除** |
| `service/impl/JwtServiceImpl` | HS256 + Redis 黑名单 | **保留**（admin 的 8 个方法已实现，见 `JwtServiceImpl.java:94-128,189-193`） |
| `util/JwtTokenProvider` | — | 只被两个待删的拦截器使用 → **删除** |

密钥也有三处：`application.properties` 的 `jwt.secret`（**保留**）、`constant/SecurityConstants.JWT_SECRET`（随拦截器一起删）、`config/JwtConfig` 的 `app.jwt.*`（properties 里根本没有这些键，字段永远取默认值 `mySecretKey` → **删除**）。

- [ ] **Step 1: 写失败的测试**

创建 `SpringBoot-Platform/src/test/java/com/calligraphy/platform/service/JwtServiceImplTest.java`：

```java
package com.calligraphy.platform.service;

import com.calligraphy.platform.entity.AdminUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtServiceImplTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void generatedTokenCarriesIdentityClaims() {
        String token = jwtService.generateToken("user-1", "USER", "alice");
        assertNotNull(token);
        assertEquals("user-1", jwtService.extractUserId(token));
        assertEquals("USER", jwtService.extractUserType(token));
        assertEquals("alice", jwtService.extractUsername(token));
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void revokedTokenFailsValidation() {
        String token = jwtService.generateToken("user-2", "USER", "bob");
        assertTrue(jwtService.validateToken(token));
        jwtService.invalidateToken(token);
        assertFalse(jwtService.validateToken(token), "被吊销的 token 必须校验失败");
    }

    @Test
    void adminTokenRoundTrips() {
        // AdminUser.id 是 Long 且由数据库生成，roles 是 String（默认 "ROLE_ADMIN"），
        // 没有 role 枚举，也没有 setRole 方法。
        AdminUser admin = AdminUser.builder()
                .username("root")
                .roles("ROLE_SUPER_ADMIN")
                .build();

        String token = jwtService.generateAdminToken(admin);
        assertNotNull(token);
        assertTrue(jwtService.validateAdminToken(token));
        assertEquals("root", jwtService.getUsernameFromAdminToken(token));
        assertTrue(jwtService.getAdminJwtExpiration() > 0);
    }
}
```

- [ ] **Step 2: 跑测试**

Run: `cd SpringBoot-Platform && mvn -o test -Dtest=JwtServiceImplTest`
Expected: 失败（`JwtService` 接口在 `service` 包下存在，但 Spring 上下文里同时有 `security.JwtService` 两个 `@Service`，可能 `NoUniqueBeanDefinitionException`）

- [ ] **Step 3: 删掉两套多余实现**

```bash
git rm SpringBoot-Platform/src/main/java/com/calligraphy/platform/security/JwtService.java SpringBoot-Platform/src/main/java/com/calligraphy/platform/util/JwtTokenProvider.java SpringBoot-Platform/src/main/java/com/calligraphy/platform/config/JwtConfig.java
```

- [ ] **Step 4: 改 `JwtAuthenticationFilter` 的注入类型**

把 `SpringBoot-Platform/src/main/java/com/calligraphy/platform/security/JwtAuthenticationFilter.java` 的第 3 行与第 27 行：

```java
import com.calligraphy.platform.service.UserDetailsServiceImpl;
...
    private final JwtService jwtService;
```

改为：

```java
import com.calligraphy.platform.service.JwtService;
import com.calligraphy.platform.service.UserDetailsServiceImpl;
...
    private final JwtService jwtService;
```

> `security.JwtService` 删除后，`JwtAuthenticationFilter` 里的 `JwtService` 简名会指向 `service.JwtService`；显式写 import 更保险。

- [ ] **Step 5: 改 `AuthServiceImpl` 的注入类型**

`Grep` 出 `AuthServiceImpl.java` 里对 `security.JwtService` 的 import 与字段声明，改为：

```java
import com.calligraphy.platform.service.JwtService;
```

（若该类在 `com.calligraphy.platform.service.impl` 包下，`service.JwtService` 也可能需要全限定名——按编译报错调整。）

- [ ] **Step 6: 跑测试，确认通过**

Run: `cd SpringBoot-Platform && mvn -o test -Dtest=JwtServiceImplTest`
Expected: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 7: 确认没有残留引用**

Run: `Grep` 模式 `security\.JwtService|util\.JwtTokenProvider|config\.JwtConfig` 于 `SpringBoot-Platform/src`
Expected: 0 命中

- [ ] **Step 8: 提交**

```bash
git add -A SpringBoot-Platform/src/main/java
git commit -m "refactor: JWT 实现统一为 service/impl/JwtServiceImpl，删除另外两套"
```

---

### Task 6: 补上 SecurityConfig、PasswordEncoder，清掉死拦截器

**Files:**
- Create: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/config/SecurityConfig.java`
- Modify: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/security/JwtAuthenticationFilter.java`（去掉 `@Component`）
- Delete: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/interceptor/JwtAuthenticationInterceptor.java`
- Delete: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/security/JwtTokenValidationInterceptor.java`
- Delete: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/constant/SecurityConstants.java`
- Delete: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/config/WebMvcConfig.java`
- Modify: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/config/WebConfig.java`
- Modify: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/config/MailConfig.java`
- Test: `SpringBoot-Platform/src/test/java/com/calligraphy/platform/config/SecurityConfigTest.java`

**Interfaces:**
- Consumes: Task 5 产出的唯一 `service.JwtService`；`UserDetailsServiceImpl`（ authorities 形如 `ROLE_USER` / `ROLE_ADMIN` / `ROLE_SUPER_ADMIN`）
- Produces: 能启动的 Spring 上下文；`/api/frontend/auth/login` 等公开路径放行，其余需 JWT

**这是当前的启动阻断：** `AuthServiceImpl:42`、`AdminServiceImpl:34`、`UserServiceImpl:40` 三处注入 `PasswordEncoder`，但全仓库没有 `@Bean PasswordEncoder`；Spring Boot 3.2.5 **不提供**该自动配置（`UserDetailsServiceAutoConfiguration` 只有 `inMemoryUserDetailsManager`，且因为 `UserDetailsServiceImpl` 存在而被 `MissingAlternative` 条件跳过）→ 启动即 `NoSuchBeanDefinitionException`。

- [ ] **Step 1: 写失败的测试**

创建 `SpringBoot-Platform/src/test/java/com/calligraphy/platform/config/SecurityConfigTest.java`：

```java
package com.calligraphy.platform.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoadsWithPasswordEncoder() {
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder.encode("secret").startsWith("$2"));
    }

    @Test
    void protectedPathWithoutTokenReturns401() {
        mockMvc.perform(get("/api/works"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicPathIsNotRejectedBySecurity() {
        // /api/frontend/auth/login 在放行列表里。本阶段还没有实现任何 Controller，
        // 所以请求应当穿过安全过滤链后由 DispatcherServlet 返回 404，而不是被拦成 401。
        mockMvc.perform(get("/api/frontend/auth/login"))
                .andExpect(status().isNotFound());
    }
}
```

- [ ] **Step 2: 跑测试，确认失败**

Run: `cd SpringBoot-Platform && mvn -o test -Dtest=SecurityConfigTest`
Expected: 失败，报 `NoSuchBeanDefinitionException: No qualifying bean of type 'PasswordEncoder'`

- [ ] **Step 3: 创建 `config/SecurityConfig.java`**

创建 `SpringBoot-Platform/src/main/java/com/calligraphy/platform/config/SecurityConfig.java`：

```java
package com.calligraphy.platform.config;

import com.calligraphy.platform.security.CustomAccessDeniedHandler;
import com.calligraphy.platform.security.JwtAuthenticationEntryPoint;
import com.calligraphy.platform.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 基座。
 *
 * 修复前的状态：仓库里没有任何 SecurityFilterChain，Spring Boot 会退回默认链
 * （anyRequest().authenticated() + formLogin + httpBasic），既没有放行规则，
 * 也没有 PasswordEncoder bean，导致上下文启动失败。
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    /** 无需认证即可访问的路径。 */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/frontend/auth/register",
            "/api/frontend/auth/login",
            "/api/backend/auth/login",
            "/api/auth/verification-code",
            "/uploads/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/health"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5174"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

- [ ] **Step 4: 去掉 `JwtAuthenticationFilter` 的 `@Component`**

它是 `@Component` 会被 Spring Boot 当普通 servlet filter 注册，order = `LOWEST_PRECEDENCE`，**晚于 security 过滤链**执行，填的 `SecurityContextHolder` 对鉴权判定无效。改成由 `SecurityFilterChain` 显式装配。

把 `SpringBoot-Platform/src/main/java/com/calligraphy/platform/security/JwtAuthenticationFilter.java` 的第 11 行与第 21 行：

```java
import org.springframework.stereotype.Component;
...
@Component
```

改为：

```java
// 由 SecurityConfig 显式装配为 Spring Security 过滤器；
// 若保留 @Component 会被当作普通 servlet filter 注册（order=LOWEST_PRECEDENCE），
// 执行时机晚于 security 过滤链，SecurityContext 对鉴权判定无效。
```

（即：删掉 `import ...Component;` 这一行，并把 `@Component` 注解删除。）

- [ ] **Step 5: 删掉两个死拦截器与重复的 SecurityConstants**

```bash
git rm SpringBoot-Platform/src/main/java/com/calligraphy/platform/interceptor/JwtAuthenticationInterceptor.java SpringBoot-Platform/src/main/java/com/calligraphy/platform/security/JwtTokenValidationInterceptor.java SpringBoot-Platform/src/main/java/com/calligraphy/platform/constant/SecurityConstants.java
```

> 这两个拦截器都标了 `@Component` 但从未被 `addInterceptors` 注册过，是纯死代码；JWT 职责归 filter。

- [ ] **Step 6: 删除重复的 `WebMvcConfig`，收紧 `WebConfig` 的 CORS**

`config/WebConfig.java` 与 `config/WebMvcConfig.java` 是两份**完全相同**的 `WebMvcConfigurer`。

```bash
git rm SpringBoot-Platform/src/main/java/com/calligraphy/platform/config/WebMvcConfig.java
```

然后把 `SpringBoot-Platform/src/main/java/com/calligraphy/platform/config/WebConfig.java` 整体替换为：

```java
package com.calligraphy.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:5174")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

> 注意：原来用的是 `allowedOriginPatterns("*")` 配 `allowCredentials(true)`，等于对任意来源开放带凭证的请求。收紧为两个明确的本地开发地址。`SecurityConfig` 里也有一份 CORS 源，两者作用面不同（一个管 MVC 映射、一个管过滤器链），保留即可。

- [ ] **Step 7: 修 `MailConfig` 的硬编码占位符**

`config/MailConfig.java` 硬编码了 `smtp.example.com` / `your-email@example.com`，**绕过**了 `application.properties` 里的 `spring.mail.*`，导致邮件功能不可能工作。替换为：

```java
package com.calligraphy.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 邮件发送器。
 *
 * 之前这里硬编码了 smtp.example.com 占位符，绕过了 application.properties 里的
 * spring.mail.* 配置，导致无论怎么配 SMTP 都发不出邮件。改为完全由配置注入。
 */
@Configuration
public class MailConfig {

    @Value("${spring.mail.host:}")
    private String host;

    @Value("${spring.mail.port:587}")
    private int port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");

        return mailSender;
    }
}
```

- [ ] **Step 8: 跑测试，确认通过**

Run: `cd SpringBoot-Platform && mvn -o test -Dtest=SecurityConfigTest`
Expected: `Tests run: 3, Failures: 0, Errors: 0`，`BUILD SUCCESS`

- [ ] **Step 9: 启动应用，确认能起来**

Run: `cd SpringBoot-Platform && mvn -o spring-boot:run`
Expected: 日志出现 `Started CalligraphyPlatformApplication in X seconds` 与 `Tomcat started on port 8083`，**没有** `NoSuchBeanDefinitionException` / `UnsatisfiedDependencyException`

用 `Ctrl+C` 停掉。

- [ ] **Step 10: 提交**

```bash
git add -A SpringBoot-Platform/src/main/java
git commit -m "feat: 补上 SecurityConfig 与 PasswordEncoder，清理死拦截器和重复 CORS 配置"
```

---

### Task 7: 修掉前端构建阻断

**Files:**
- Modify: `Practice-Platform/src/views/CommunityView.vue:344` 与 `:595`

**Interfaces:**
- Consumes: 无
- Produces: `npm run build` 退出码 0

- [ ] **Step 1: 确认构建失败**

Run: `cd Practice-Platform && npm run build`
Expected: 失败，报
```
[vite:vue] [vue/compiler-sfc] Identifier 'handleLike' has already been declared. (330:6)
```

- [ ] **Step 2: 读两处 `handleLike` 的实现**

Run: `Read` `Practice-Platform/src/views/CommunityView.vue`，offset 340，limit 20；再读 offset 590，limit 30。
两处分别是同步版与异步版，**异步版是正确的那一个**（要 await 点赞接口）。

- [ ] **Step 3: 删掉同步版，保留异步版**

删除 `Practice-Platform/src/views/CommunityView.vue` 中第 344 行开始的同步版 `handleLike` 整个函数体（只保留第 595 行起的异步版）。

如果两版实现逻辑有差异（不只是 async 的区别），**以异步版为准**并把同步版独有且必要的逻辑合并进去。

- [ ] **Step 4: 确认没有其它重复声明**

Run: `Grep` 模式 `const handleLike|function handleLike` 于 `Practice-Platform/src/views/CommunityView.vue`
Expected: 恰好 1 命中

- [ ] **Step 5: 跑构建**

Run: `cd Practice-Platform && npm run build`
Expected: `✓ built in Xs`，退出码 0

- [ ] **Step 6: 提交**

```bash
git add Practice-Platform/src/views/CommunityView.vue
git commit -m "fix: 移除 CommunityView 中重复声明的 handleLike，恢复前端构建"
```

---

### Task 8: 仓库卫生与 Maven 镜像

**Files:**
- Create: `.gitignore`（仓库根）
- Delete: `SpringBoot-Platform/FixChineseComments.ps1`、`FixEncoding.ps1`、`FixImports.ps1`、`FixImportsBasic.ps1`、`FixImportsSimple.ps1`、`RemoveBom.ps1`
- Delete: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/util/AnnotationCleanup.java`、`BomRemover.java`、`EncodingFixer.java`
- Delete: `SpringBoot-Platform/AnnotationCleanup.java`、`SpringBoot-Platform/AnnotationCleanup.class`
- Delete: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/test/TestClass.java`、`SimpleApplication.java`
- Delete: `SpringBoot-Platform/src/main/java/com/calligraphy/platform/Test.java`
- Delete: `SpringBoot-Platform/src/main/resources/templates/admin/*.html`（8 个 Thymeleaf 页）
- Modify: `SpringBoot-Platform/src/main/resources/application.properties`（删除指向不存在包的日志配置）
- Modify: `SpringBoot-Platform/vite.config.js`（proxy 端口 8080 → 8083）
- Modify: `~/.m2/settings.xml`（**不在仓库内**，配阿里云镜像）

**Interfaces:**
- Consumes: Task 1–7 的成果
- Produces: 干净的 git 索引（不再跟踪 `target/classes`）、本地 Maven 走国内镜像、proxy 指向正确的后端端口

- [ ] **Step 1: 新建根 `.gitignore`**

创建仓库根目录的 `.gitignore`：

```gitignore
# Maven 构建产物
SpringBoot-Platform/target/
*.class

# 运行日志
*.log
app.log*

# 编码修复工具的输出报告
SpringBoot-Platform/message-check-report.txt
docs/mojibake-unresolved.md

# IDE
.idea/
*.iml
.vscode/settings.json

# OS
.DS_Store
Thumbs.db
```

- [ ] **Step 2: 把 `target/classes` 从 git 索引里摘掉（保留磁盘文件）**

Run:
```bash
git rm -r --cached SpringBoot-Platform/target
```
Expected: 列出被摘掉的 `.class` 文件；磁盘上的文件仍在

- [ ] **Step 3: 删掉一次性修复脚本与无 package 声明的工具类**

```bash
git rm SpringBoot-Platform/FixChineseComments.ps1 SpringBoot-Platform/FixEncoding.ps1 SpringBoot-Platform/FixImports.ps1 SpringBoot-Platform/FixImportsBasic.ps1 SpringBoot-Platform/FixImportsSimple.ps1 SpringBoot-Platform/RemoveBom.ps1
git rm SpringBoot-Platform/src/main/java/com/calligraphy/platform/util/AnnotationCleanup.java SpringBoot-Platform/src/main/java/com/calligraphy/platform/util/BomRemover.java SpringBoot-Platform/src/main/java/com/calligraphy/platform/util/EncodingFixer.java
git rm SpringBoot-Platform/AnnotationCleanup.java SpringBoot-Platform/AnnotationCleanup.class
```

> `util/` 下这三个类**都没有 package 声明**，且 `main()` 会就地批量改写 `src/main/java`——留在仓库里是个隐患。

- [ ] **Step 4: 删掉测试脚手架与死 Thymeleaf 页面**

```bash
git rm SpringBoot-Platform/src/main/java/com/calligraphy/platform/test/TestClass.java SpringBoot-Platform/src/main/java/com/calligraphy/platform/test/SimpleApplication.java SpringBoot-Platform/src/main/java/com/calligraphy/platform/Test.java
git rm SpringBoot-Platform/src/main/resources/templates/admin/login.html SpringBoot-Platform/src/main/resources/templates/admin/dashboard.html SpringBoot-Platform/src/main/resources/templates/admin/users.html SpringBoot-Platform/src/main/resources/templates/admin/users-add.html SpringBoot-Platform/src/main/resources/templates/admin/users-edit.html SpringBoot-Platform/src/main/resources/templates/admin/settings.html SpringBoot-Platform/src/main/resources/templates/admin/simple-test.html
```

> `test/SimpleApplication.java` 带 `@SpringBootApplication` 且被主启动类扫到，还带着唯一的 `@RestController`（`GET /test`）——删掉后整个仓库就没有任何 Controller 了，这正是 P2–P4 要补的东西。

- [ ] **Step 5: 修 `application.properties` 里指向不存在包的日志配置**

删除这一行：
```properties
logging.level.com.calligraphy.platform.controller=INFO
```

- [ ] **Step 6: 修 `vite.config.js` 的 proxy 端口**

把 `Practice-Platform/vite.config.js` 里的 proxy target 从 `http://localhost:8080` 改为 `http://localhost:8083`（后端实际端口）。同时把 dev server 端口固定为 5174：

```js
server: {
  host: '0.0.0.0',
  port: 5174,
  open: true,
  proxy: {
    '/api': {
      target: 'http://localhost:8083',
      changeOrigin: true
    }
  }
}
```

> 5173 已被另一个项目占用。改端口后，若 CORS 报错，检查 Task 6 的 `SecurityConfig` / `WebConfig` 是否都已包含 `http://localhost:5174`（本计划已包含）。

- [ ] **Step 7: 配 Maven 阿里云镜像（仓库外）**

`~/.m2/settings.xml` 的 `<mirrors>` 整段目前是注释状态，直连中央仓库在国内很慢。把该段替换为：

```xml
  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven Mirror</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
```

验证：
Run: `cd SpringBoot-Platform && mvn -o dependency:resolve -q`
Expected: 无报错（`-o` 离线模式说明缓存够用；去掉 `-o` 则会走镜像）

- [ ] **Step 8: 确认索引干净**

Run: `git ls-files | Select-String -Pattern 'target/|\.class$|FixEncoding|FixImports|RemoveBom|AnnotationCleanup'`
Expected: 0 命中

- [ ] **Step 9: 全量验收**

Run: `cd SpringBoot-Platform && mvn -o clean test`
Expected: `BUILD SUCCESS`

Run: `cd Practice-Platform && npm run build`
Expected: 退出码 0

Run: `cd SpringBoot-Platform && python scripts/mojibake.py detect --root .`
Expected: `files with mojibake : 0`，退出码 0

Run: `cd SpringBoot-Platform && mvn -o spring-boot:run`
Expected: 正常启动，`Tomcat started on port 8083`

- [ ] **Step 10: 提交**

```bash
git add -A .gitignore SpringBoot-Platform Practice-Platform/vite.config.js
git commit -m "chore: 清理构建产物入库、一次性修复脚本与死代码，proxy 指向 8083"
```

> `git add -A .gitignore SpringBoot-Platform Practice-Platform/vite.config.js` 只覆盖这三处的改动，**不会**误提交工作区里那 38 个既有修改文件。

---

## 阶段 0 完成标准

全部满足才算 P0 完成：

| # | 检查 | 命令 | 期望 |
|---|---|---|---|
| 1 | 后端能编译 | `cd SpringBoot-Platform && mvn -o clean compile` | BUILD SUCCESS |
| 2 | 后端能启动 | `cd SpringBoot-Platform && mvn -o spring-boot:run` | `Tomcat started on port 8083` |
| 3 | 后端测试通过 | `cd SpringBoot-Platform && mvn -o test` | BUILD SUCCESS |
| 4 | 无编码污染 | `cd SpringBoot-Platform && python scripts/mojibake.py detect --root .` | 0 命中，退出码 0 |
| 5 | 消息数字一致 | `cd SpringBoot-Platform && python scripts/check_messages.py` | `problems: 0` |
| 6 | 前端能构建 | `cd Practice-Platform && npm run build` | 退出码 0 |
| 7 | 数据不被清空 | 见 Task 3 Step 4 | 哨兵行仍在 |
| 8 | 索引干净 | `git ls-files \| Select-String 'target/\|\.class$'` | 0 命中 |

## 下一阶段

P0 完成后进入 **P1（阶段 1：契约定稿）**——把 53 个端点写成 `Practice-Platform/openapi/contract.yaml` + `docs/api-contract.md`，并实现 `scripts/check-contract.mjs` 的双向比对脚本。P1 完成前不写任何 Controller。
