# 编码污染检测与机械修复脚本

`mojibake.py` 用于检测并机械修复后端源码里的 **UTF-8 被当成 GBK 解码** 造成的乱码污染。

## 背景

2025 年的一次批量转码事故把 `src/main/java` 下的 Java 源文件搞坏了：

1. 文件原本是 UTF-8；
2. 被按 GBK 解码（这一步有损）；
3. 再按 UTF-8 编码写回。

GBK 的双字节区里，后字节小于 `0x40` 是非法的，所以**任何跟在 GBK 前导字节后面的、
落在 `0x00-0x3F` 的 ASCII 字节**（引号、换行、数字、标点……）都被替换成了 `?`。

这一个 `?` 标记，解释了全部四类损坏：

| 规则 | 现象 | 处理方式 |
| --- | --- | --- |
| A | 整段中文变成 `鍏堝皾璇曚粠鏅...`，但没有丢字节 | 可逆：`run.encode('gbk').decode('utf-8')` 直接还原 |
| B | `?` 吃掉了字符串的收尾引号，字符串字面量不再闭合 | 补回 `"` |
| C | `?` 吃掉了换行，`//` 注释把**下一行的语句**吞掉了 | 补回换行 |
| D | `?` 吃掉了普通 ASCII 字符（数字等） | 逐字节搜索唯一解；不唯一就交给人工 |

A/B/C 三类可以机械地修好；D 类以及所有**无法唯一定夺**的行会被写进报告，由人工处理。
脚本**不会**猜测歧义内容。

## 用法

```bash
cd SpringBoot-Platform

# 自检：必须退出码 0
python scripts/mojibake.py selftest --root .

# 只检测，不修改。干净时退出码 0，发现污染时退出码 1
python scripts/mojibake.py detect --root .

# 试运行：打印影响面，并把无法自动判定的行写进报告
python scripts/mojibake.py repair --root . --report ../docs/mojibake-unresolved.md

# 真正落盘
python scripts/mojibake.py repair --root . --apply --report ../docs/mojibake-unresolved.md
```

### 参数

| 参数 | 说明 |
| --- | --- |
| `--root DIR` | 扫描根目录，默认为当前目录。脚本只处理 `<root>/src/main/java` 与 `<root>/src/main/resources` 下的 `.java` / `.sql` / `.properties` |
| `--apply` | 缺省是**试运行**，只统计不写文件；加上才写回源文件 |
| `--report FILE` | 把无法自动判定的行写进该 Markdown 文件（UTF-8） |

## 报告格式

```
UNRESOLVED <相对路径>:<行号>
    <该行内容>

```

行首是 `UNRESOLVED <相对路径>:<行号>`，下一行是该行内容（截断到 200 字符）。
文末附有 `files changed` 与 `unresolved lines` 两个汇总计数。

## 注意事项

- **控制台输出刻意保持纯 ASCII。** 本机 PowerShell 控制台是 GBK，Python 用 GBK 写
  stdout 时遇到欧元符号（码位 U+20AC）之类的字符会抛 `UnicodeEncodeError` 并**中断整个脚本**。
  所以中文明细只写进 UTF-8 的报告文件，stdout 只打印 ASCII 摘要。
- 修复会把源文件统一改写为 **LF** 换行。仓库启用了 `core.autocrlf=true`，
  Git 会在提交时归一化，所以这一步不会污染 diff。
- 脚本**只做机械修复**。报告中列出的行仍然需要人工判断后再改。
- 跑 `repair --apply` 之前建议先跑一次不带 `--apply` 的试运行，确认 `files changed`
  的规模符合预期。

## 自检覆盖了什么

`selftest` 里有四个用例，任何一个失败都会以非 0 退出码退出：

1. **中文假阳性**——`岗位` / `状态` / `用户` 这样的正常词不能被规则 A 误判为乱码；
2. **规则 A 可逆**——从仓库里真实取一段乱码，必须能还原回正常中文；
3. **规则 C 换行**——`//` 注释吞掉下一行语句时，要能把语句拆回独立的一行；
4. **规则 B 引号**——字符串字面量丢了收尾引号时，要把引号补回去。
